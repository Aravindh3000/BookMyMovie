package com.example.BookMyMovie.dtos;

import lombok.Data;

@Data
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}
