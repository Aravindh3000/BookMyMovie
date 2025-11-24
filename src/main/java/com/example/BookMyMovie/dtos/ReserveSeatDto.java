package com.example.BookMyMovie.dtos;

import java.util.List;

import lombok.Data;

@Data
public class ReserveSeatDto {
    private String userEmail;
    private Long showId;
    private List<Long> showSeats;
}
