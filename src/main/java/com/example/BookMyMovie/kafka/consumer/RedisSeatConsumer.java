package com.example.BookMyMovie.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.example.BookMyMovie.Redis.RedisSeatService;
import com.example.BookMyMovie.dtos.events.RedisUpdateEvent;
// import com.example.BookMyMovie.dtos.events.SeatEvent;
// import com.example.BookMyMovie.websocket.SeatWebSocketNotifier;

@Component
@RequiredArgsConstructor
public class RedisSeatConsumer {

    private final RedisSeatService redisSeatService;
    private final Logger log = LoggerFactory.getLogger(RedisSeatConsumer.class);
    // private final SeatWebSocketNotifier notifier;
    
    @KafkaListener(topics = "redis-update-events", groupId = "redis-updater-group")
    public void onRedisUpdateEvent(RedisUpdateEvent evt) {
        log.info("Received redis-update-event: {}", evt);

        // 1) Patch cached seat to BOOKED (or evt.newStatus)
        redisSeatService.patchSeatStatusInCache(evt.getShowId(), evt.getSeatIds(), evt.getNewStatus().equals("CONFIRMED") ? "BOOKED" : "AVAILABLE");

        // 2) Release temporary lock (if any)
        redisSeatService.releaseSeats(evt.getShowId(), evt.getSeatIds());

        // 3) Publish seat-event so websocket & others get notified
        // notifier.broadcastSeatEvent(new SeatEvent(evt.getShowId(), evt.getSeatIds(), evt.getNewStatus() == "CONFIRMED" ? "BOOKED" : "AVAILABLE", null));
    }
}
