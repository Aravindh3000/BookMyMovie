package com.example.BookMyMovie.dtos.events;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatEvent {
    private Long showId;
    private List<Long> seatIds;
    private String status;
    private Long lockedBy;
}
