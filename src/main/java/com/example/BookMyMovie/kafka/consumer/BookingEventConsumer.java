package com.example.BookMyMovie.kafka.consumer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.BookMyMovie.dtos.events.BookingEvent;
import com.example.BookMyMovie.dtos.events.RedisUpdateEvent;
import com.example.BookMyMovie.exceptions.BadRequestException;
import com.example.BookMyMovie.kafka.producer.EventProducer;
import com.example.BookMyMovie.models.Booking;
import com.example.BookMyMovie.models.BookingStatus;
import com.example.BookMyMovie.models.SeatStatus;
import com.example.BookMyMovie.models.ShowSeat;
import com.example.BookMyMovie.models.ShowSeatBooking;
import com.example.BookMyMovie.repositories.BookingRepository;
import com.example.BookMyMovie.repositories.ShowSeatBookingRepository;
import com.example.BookMyMovie.repositories.ShowSeatRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);
    private final EventProducer producer;
    private final BookingRepository bookingRepository;
    private final ShowSeatBookingRepository showSeatBookingRepository;
    private final ShowSeatRepository showSeatRepository;
    @KafkaListener(topics = "booking-events", groupId = "booking-group")
    public void onBookingEvent(BookingEvent evt) {
        log.info("Handling booking-event: {}", evt);

        // Get Booking From BookingId
        Booking booking = bookingRepository.findById(evt.getBookingId())
                .orElseThrow(() -> new BadRequestException("Booking not found"));
        booking.setStatus(evt.getStatus().equals("CONFIRMED") ? BookingStatus.CONFIRMED : BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking status updated to {}", booking.getStatus());

        // Get ShowSeat Booking
        List<ShowSeatBooking> showSeatBookings = showSeatBookingRepository.findAllByBooking(booking);

        // Get ShowSeat From ShowSeatBooking
        List<ShowSeat> showSeats = showSeatBookings.stream().map(ShowSeatBooking::getShowSeat).toList();

        // Update ShowSeat Status
        List<ShowSeat> updatedShowSeats = new ArrayList<>();
        showSeats.forEach(showSeat -> {
            showSeat.setStatus(evt.getStatus().equals("CONFIRMED") ? SeatStatus.BOOKED : SeatStatus.AVAILABLE);
            updatedShowSeats.add(showSeat);
        });
        showSeatRepository.saveAll(updatedShowSeats);

        // Publish Redis Update Event
        producer.publishRedisUpdateEvent(new RedisUpdateEvent(showSeats.get(0).getShow().getId(), showSeats.stream().map(ShowSeat::getId).toList(), evt.getStatus()));
    }
}
