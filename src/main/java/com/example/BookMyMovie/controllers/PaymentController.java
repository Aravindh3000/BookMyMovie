package com.example.BookMyMovie.controllers;

import com.example.BookMyMovie.dtos.events.PaymentEvent;
import com.example.BookMyMovie.kafka.producer.EventProducer;
import com.example.BookMyMovie.models.Booking;
import com.example.BookMyMovie.models.BookingStatus;
import com.example.BookMyMovie.models.Payment;
import com.example.BookMyMovie.models.PaymentMode;
import com.example.BookMyMovie.models.PaymentStatus;
import com.example.BookMyMovie.repositories.BookingRepository;
import com.example.BookMyMovie.repositories.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.google.gson.JsonSyntaxException;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	@Value("${stripe.secretkey}")
	private String stripeApiKey;

	@Value("${stripe.webhook-secret}")
	private String endpointSecret;

	private final PaymentRepository paymentRepository;
	private final BookingRepository bookingRepository;

	private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
	private final EventProducer producer;

	public PaymentController(PaymentRepository paymentRepository, BookingRepository bookingRepository, EventProducer producer) {
		this.paymentRepository = paymentRepository;
		this.bookingRepository = bookingRepository;
		this.producer = producer;
	}

	@PostConstruct
	public void init() {
		Stripe.apiKey = stripeApiKey;
	}

	// DTO for incoming request
	@Getter
	@Setter
	public static class CheckoutRequest {
		public Long bookingId;
		public BigDecimal amount;
	}

	/**
	 * STEP 1: Create Stripe Checkout Session (no success URL)
	 */
	@PostMapping("/create-checkout-session")
	public Map<String, Object> createCheckoutSession(@RequestBody CheckoutRequest request) throws Exception {

		System.out.println("Checkout session for " + request.getBookingId() + " " + request.getAmount());
		Booking booking = bookingRepository.findById(request.bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found"));

		// 1️⃣ Create a pending payment entry
		Payment payment = new Payment();
		payment.setBooking(booking);
		payment.setAmount(request.amount);
		payment.setMode(PaymentMode.CREDIT_CARD);
		payment.setStatus(PaymentStatus.PENDING);
		payment = paymentRepository.save(payment);

		Long amountInPaise = request.amount.multiply(BigDecimal.valueOf(100)).longValue(); // INR → paise
		String currency = "inr";

		// 2️⃣ Build Stripe session (no redirect URLs, purely webhook-based)
		SessionCreateParams params = SessionCreateParams.builder()
				.setMode(SessionCreateParams.Mode.PAYMENT)
				.setSuccessUrl("https://example.com/success") // placeholder
				.setCancelUrl("https://example.com/cancel")
				.addLineItem(
						SessionCreateParams.LineItem.builder()
								.setQuantity(1L)
								.setPriceData(
										SessionCreateParams.LineItem.PriceData
												.builder()
												.setCurrency(currency)
												.setUnitAmount(amountInPaise)
												.setProductData(
														SessionCreateParams.LineItem.PriceData.ProductData
																.builder()
																.setName("Movie Ticket Booking")
																.build())
												.build())
								.build())
				.putMetadata("bookingId", request.bookingId.toString())
				.putMetadata("paymentId", payment.getId().toString())
				// Also attach metadata to the PaymentIntent for better webhook handling
				.setPaymentIntentData(
						SessionCreateParams.PaymentIntentData.builder()
								.putMetadata("bookingId", request.bookingId.toString())
								.putMetadata("paymentId", payment.getId().toString())
								.build())
				.build();

		Session session = Session.create(params);

		// Save Stripe reference ID (for tracking)
		payment.setReferenceId(session.getId());
		paymentRepository.save(payment);

		Map<String, Object> response = new HashMap<>();
		response.put("checkoutUrl", session.getUrl());
		response.put("sessionId", session.getId());
		return response;
	}

	/**
	 * STEP 2: Webhook handler for Stripe events (safe + backend only)
	 */
	@PostMapping("/webhook")
	public ResponseEntity<String> handleStripeWebhook(
			@RequestHeader("Stripe-Signature") String sigHeader,
			@RequestBody String payload) {

		Event event = null;
		
		try {
			event = ApiResource.GSON.fromJson(payload, Event.class);
		} catch (JsonSyntaxException e) {
			logger.error("Failed to parse webhook payload", e);
			return ResponseEntity.badRequest().build();
		}

		EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
		StripeObject stripeObject = null;

		if (dataObjectDeserializer.getObject().isPresent()) {
			stripeObject = dataObjectDeserializer.getObject().get();
		} else {
			// NOTE: Deserialization failed, probably due to an API version mismatch.
			// Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
			// instructions on how to handle this case, or return an error here.
			try {
				stripeObject = dataObjectDeserializer.deserializeUnsafe();
				logger.info(
						"Unsafely deserialized webhook payload for event: {}. Expected API version '{}' but received API version '{}'",
						event.getId(), Stripe.API_VERSION, event.getApiVersion());
			} catch (EventDataObjectDeserializationException e) {
				logger.error("Failed to deserialize webhook payload", e);
				return ResponseEntity.badRequest().build();
			}
		}

		String eventType = event.getType();
		Map<String, String> metadata = null;

		switch (eventType) {
            // case "payment_intent.created":
            case "payment_intent.payment_failed":
            case "payment_intent.requires_action":
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                metadata = paymentIntent.getMetadata();
                System.out.println("Payment Intent metadata: " + metadata);
				String paymentId = metadata.get("paymentId");
				System.out.println("Payment ID from metadata: " + paymentId);
				if (paymentId != null) {
					handleSuccessfulPaymentUsingEvents(paymentId);
				} else {
					System.out.println("No paymentId found in metadata");
				}
				break;


            default:
                throw new RuntimeException("Unhandled event type: " + eventType);
        }

		return ResponseEntity.ok("");
	}

	private void handleSuccessfulPayment(String paymentId) {
		if (paymentId == null) {
			System.out.println("Payment ID is null, cannot process payment");
			return;
		}

		try {
			System.out.println("Processing successful payment for ID: " + paymentId);
			Payment payment = paymentRepository.findById(Long.valueOf(paymentId)).orElse(null);

			if (payment != null) {
				System.out.println("Found payment: " + payment.getId() + ", current status: "
						+ payment.getStatus());

				payment.setStatus(PaymentStatus.SUCCESS);
				paymentRepository.save(payment);
				System.out.println("Payment status updated to SUCCESS");

				Booking booking = payment.getBooking();
				if (booking != null) {
					System.out.println("Found booking: " + booking.getId() + ", current status: "
							+ booking.getStatus());
					booking.setStatus(BookingStatus.CONFIRMED);
					bookingRepository.save(booking);
					System.out.println("Booking status updated to CONFIRMED");
				} else {
					System.out.println("No booking found for payment ID: " + paymentId);
				}

				System.out.println("✅ Payment and Booking updated successfully for ID: " + paymentId);
			} else {
				System.out.println("❌ No payment found for ID: " + paymentId);
			}
		} catch (Exception e) {
			System.out.println("❌ Error processing payment ID " + paymentId + ": " + e.getMessage());
			e.printStackTrace();
		}
	
	}

	private void handleSuccessfulPaymentUsingEvents(String paymentId) {
		if (paymentId == null) {
			System.out.println("Payment ID is null, cannot process payment");
			return;
		}

		try {
			Payment payment = paymentRepository.findById(Long.valueOf(paymentId))
					.orElse(null);
	
			if (payment == null) {
				logger.error("Payment not found for id {}", paymentId);
				return;
			}
	
			Long bookingId = payment.getBooking().getId();
	
			// ✅ Publish PaymentEvent asynchronously
			PaymentEvent evt = new PaymentEvent(
					bookingId,
					payment.getId(),
					"SUCCESS"
			);
	
			producer.publishPaymentEvent(evt);
	
			logger.info("Published PaymentEvent to Kafka: {}", evt);
	
		} catch (Exception e) {
			logger.error("Payment handling error: {}", e.getMessage(), e);
		}
	
	}

}
