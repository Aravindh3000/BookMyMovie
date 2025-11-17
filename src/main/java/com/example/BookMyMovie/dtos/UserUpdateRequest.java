package com.example.BookMyMovie.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10,15}$", message = "phone must be 10-15 digits")
    private String phone;

    @NotBlank
    @Email
    private String email;

    @Size(min = 8, message = "password must be at least 8 characters")
    private String password;

}


