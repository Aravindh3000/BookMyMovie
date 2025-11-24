package com.example.BookMyMovie.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowSeatDto {
    private Long id;
    private String seatCondition;
    private String seatNumber;
    private String seatStatus;
    private BigDecimal price;
}
