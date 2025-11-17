package com.example.BookMyMovie.dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ReserveResponseDto {
    private Long bookingId;
    private BigDecimal totalAmount;
    private List<Long> seats;
}
