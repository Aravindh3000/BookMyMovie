package com.example.BookMyMovie.dtos.events;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisUpdateEvent {
    private Long showId;
    private List<Long> seatIds;
    private String newStatus; // BOOKED
}