package com.example.BookMyMovie.controllers;

import com.example.BookMyMovie.dtos.*;
import com.example.BookMyMovie.services.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/theatre")
    public ResponseEntity<TheatreCreateResponse> createTheatreWithScreenAndSeats(
            @Valid @RequestBody TheatreCreateRequest request) {

        try {
            TheatreCreateResponse response = adminService.createTheatreWithScreenAndSeats(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/movie")
    public ResponseEntity<MovieCreateResponse> createMovie(@Valid @RequestBody MovieCreateRequest request) {
        try {
            MovieCreateResponse response = adminService.createMovie(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/show")
    public ResponseEntity<ShowCreateResponse> createShow(@Valid @RequestBody ShowCreateRequest request) {
        ShowCreateResponse response = adminService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/shows/multiple")
    public ResponseEntity<List<ShowCreateResponse>> createMultipleShows(
            @Valid @RequestBody MultipleShowsCreateRequest request) {
        try {
            List<ShowCreateResponse> responses = adminService.createMultipleShows(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
