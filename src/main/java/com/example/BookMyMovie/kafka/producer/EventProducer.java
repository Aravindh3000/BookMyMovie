package com.example.BookMyMovie.kafka.producer;

import java.util.stream.Collectors;

import com.example.BookMyMovie.dtos.events.PaymentEvent;
import com.example.BookMyMovie.dtos.events.RedisUpdateEvent;
import com.example.BookMyMovie.dtos.events.BookingEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventProducer {

    private static final Logger log = LoggerFactory.getLogger(EventProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentEvent(PaymentEvent e) {
        log.info("Publishing PaymentEvent: {}", e);
        kafkaTemplate.send("payment-events", e.getPaymentId().toString(), e);
    }

    public void publishBookingEvent(BookingEvent e) {
        log.info("Publishing BookingEvent: {}", e);
        kafkaTemplate.send("booking-events", e.getBookingId().toString(), e);
    }

    public void publishRedisUpdateEvent(RedisUpdateEvent redisUpdateEvent) {
        log.info("Publishing RedisUpdateEvent: {}", redisUpdateEvent);
        kafkaTemplate.send("redis-update-events", redisUpdateEvent.getShowId().toString() + "-" + redisUpdateEvent.getSeatIds().stream().map(Object::toString).collect(Collectors.joining("-")), redisUpdateEvent);
    }
}
