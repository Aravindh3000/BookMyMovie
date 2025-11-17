package com.example.BookMyMovie.controllers;

import com.example.BookMyMovie.dtos.LoginRequestDto;
import com.example.BookMyMovie.dtos.LoginResponseDto;
import com.example.BookMyMovie.dtos.UserCreateRequest;
import com.example.BookMyMovie.dtos.UserRefreshTokenDto;
import com.example.BookMyMovie.dtos.UserResponse;
import com.example.BookMyMovie.dtos.UserUpdateRequest;
import com.example.BookMyMovie.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/path")
    public String getMethodName() {
        return "hello world";
    }
    

    @PostMapping("/register")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest req) {
        UserResponse created = userService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> postMethodName(@Valid @RequestBody LoginRequestDto req) {
        //TODO: process POST request
        
        return ResponseEntity.status(HttpStatus.OK).body(userService.login(req));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<LoginResponseDto> refreshToken(@Valid @RequestBody UserRefreshTokenDto userRefreshTokenDto) {
        //TODO: process POST request
        return ResponseEntity.status(HttpStatus.OK).body(userService.refreshAccessToken(userRefreshTokenDto));
    }
    
    

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest req) {
        return ResponseEntity.ok(userService.update(id, req));
    }
}

