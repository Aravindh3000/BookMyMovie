package com.example.BookMyMovie.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRefreshTokenDto {
    @NotBlank
    private String refreshToken;

    @NotBlank
    @Pattern(regexp = "USER|ADMIN", message = "Role must be USER, ADMIN")
    private String role;
}
