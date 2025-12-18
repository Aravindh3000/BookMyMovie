package com.example.BookMyMovie.Redis;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.BookMyMovie.dtos.ShowSeatDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RedisSeatService {

    private static final Logger log = LoggerFactory.getLogger(RedisSeatService.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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

    public void releaseSeats(Long showId, List<Long> seatIds) {
        for (Long seatId : seatIds) {
            String key = "lock:show:" + showId + ":seat:" + seatId;
            redisTemplate.delete(key);
            log.info("🔵 Seats {} unlocked and key {} removed from Redis", seatIds, key);
        }
    }

    public String getLockedBy(Long showId, Long seatId) {
        String key = "show:" + showId + ":seat:" + seatId + ":lockedBy";

        String val = redisTemplate.opsForValue().get(key);

        log.debug("Seat {} is locked by: {}", seatId, val);

        return val;
    }

    /**
     * Patch a single seat's status inside cached show seats JSON.
     * Preserves remaining TTL (if any).
     *
     * Returns true if patch applied, false if no cached JSON exists or seat not found.
     */
     public boolean patchSeatStatusInCache(Long showId, List<Long> seatIds, String newStatus) {
        String key = getKey(showId);
        String cached = redisTemplate.opsForValue().get(key);
        if (cached == null) {
            log.warn("No cached seat list for show {}, cannot patch", seatIds);
            return false;
        }
        System.out.println("New seatstatus is " + newStatus + seatIds);
        try {
            List<ShowSeatDto> list = objectMapper.readValue(cached, new TypeReference<List<ShowSeatDto>>() {});
            boolean changed = false;
            for (ShowSeatDto dto : list) {
                if (seatIds.contains(dto.getId())) {
                    dto.setSeatStatus(newStatus);
                    changed = true;
                }
            }
            if (!changed) {
                log.warn("Seats {} not found in cached JSON for show {}", seatIds, showId);
                return false;
            }

            String updatedJson = objectMapper.writeValueAsString(list);

            // preserve TTL
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS); // -2: not exist, -1: no expiry
            if (ttl == null) ttl = -2L;

            if (ttl == -2L) {
                // key disappeared: write without TTL (or choose default)
                redisTemplate.opsForValue().set(key, updatedJson);
                log.info("Patched cached seat and set without TTL for {}", key);
            } else if (ttl == -1L) {
                // no expiry
                redisTemplate.opsForValue().set(key, updatedJson);
                log.info("Patched cached seat and preserved no-expiry for {}", key);
            } else {
                // preserve remaining TTL
                redisTemplate.opsForValue().set(key, updatedJson, ttl, TimeUnit.SECONDS);
                log.info("Patched cached seat and preserved TTL={}s for {}", ttl, key);
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to patch cached seat status for show {} and seats {}", showId, seatIds, e);
            return false;
        }
    }
}