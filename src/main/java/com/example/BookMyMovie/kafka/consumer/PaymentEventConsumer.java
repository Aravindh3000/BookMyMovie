package com.example.BookMyMovie.kafka.consumer;

import com.example.BookMyMovie.dtos.events.PaymentEvent;
import com.example.BookMyMovie.dtos.events.BookingEvent;
import com.example.BookMyMovie.kafka.producer.EventProducer;
import com.example.BookMyMovie.models.Payment;
import com.example.BookMyMovie.models.PaymentStatus;
import com.example.BookMyMovie.repositories.PaymentRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final PaymentRepository paymentRepository;
    private final EventProducer producer;

    @KafkaListener(topics = "payment-events", groupId = "payment-group")
    public void handlePaymentEvent(PaymentEvent evt) {
        log.info("Received PaymentEvent: {}", evt);

        Payment payment = paymentRepository.findById(evt.getPaymentId()).orElse(null);
        if (payment == null) {
            log.error("Payment not found for id {}", evt.getPaymentId());
            return;
        }

        // Update DB
        if ("SUCCESS".equals(evt.getStatus())) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);

        BookingEvent bookingEvent = new BookingEvent(
                evt.getBookingId(),
                evt.getStatus().equals("SUCCESS") ? "CONFIRMED" : "FAILED");

        log.info("PaymentEvent processed and BookingEvent published: {}", bookingEvent);
        producer.publishBookingEvent(bookingEvent);
    }
}
