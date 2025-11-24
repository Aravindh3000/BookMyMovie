package com.example.BookMyMovie.Redis;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisSeatService {

    private static final Logger log = LoggerFactory.getLogger(RedisSeatService.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private String getKey(Long showId) {
        return "show:" + showId + ":seats";
    }

    public void cacheShowSeats(Long showId, String seatJson) {
        String key = getKey(showId);

        log.info("🔵 Caching ShowSeats → Redis Key: {}, Expiry: 5min", key);
        log.debug("📦 Cached Value (JSON): {}", seatJson);

        redisTemplate
                .opsForValue()
                .set(key, seatJson, 5, TimeUnit.MINUTES);
    }

    public String getCachedSeats(Long showId) {
        String key = getKey(showId);

        String cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            log.info("🟢 Redis HIT for key: {}", key);
            log.debug("📤 Cached Seats JSON: {}", cached);
        } else {
            log.warn("🔴 Redis MISS for key: {}", key);
        }

        return cached;
    }

    /** Lock seat */
    public boolean lockSeat(Long showId, Long seatId, Long userId) {

        String key = "lock:show:" + showId + ":seat:" + seatId;

        log.info("Trying to lock Seat {} for User {} (Show {})", seatId, userId, showId);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, userId.toString(), 5, TimeUnit.MINUTES);

        if (Boolean.TRUE.equals(success)) {

            log.info("Seat {} LOCKED successfully by User {}", seatId, userId);

            return true;
        }

        log.warn("Seat {} lock FAILED", seatId);
        return false;
    }


    public void releaseSeat(Long showId, Long seatId) {
        String key = "show:" + showId + ":seat:" + seatId + ":lockedBy";

        redisTemplate.delete(key);

        log.info("🔵 Seat {} unlocked and key {} removed from Redis", seatId, key);
    }

    public String getLockedBy(Long showId, Long seatId) {
        String key = "show:" + showId + ":seat:" + seatId + ":lockedBy";

        String val = redisTemplate.opsForValue().get(key);

        log.debug("Seat {} is locked by: {}", seatId, val);

        return val;
    }
}
