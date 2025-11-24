package com.example.BookMyMovie.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.BookMyMovie.dtos.ReserveResponseDto;
import com.example.BookMyMovie.dtos.ReserveSeatDto;
import com.example.BookMyMovie.dtos.ShowSeatDto;
import com.example.BookMyMovie.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService; 
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/testUser")
    public ResponseEntity<String> testUser() {
        return ResponseEntity.ok("UserController");
    }

    @PostMapping("/reserveSeat")
    public ResponseEntity<ReserveResponseDto> bookTickets(@RequestBody ReserveSeatDto reserveSeatDto) {
        
        ReserveResponseDto reserveResponseDto = userService.reserveSeatsUsingRedis(reserveSeatDto.getShowId(), reserveSeatDto.getShowSeats(), reserveSeatDto.getUserEmail());

        return ResponseEntity.ok(reserveResponseDto);
    }

    @GetMapping("/getShowSeats/{showId}")
    public ResponseEntity<List<ShowSeatDto>> getShowSeats(@PathVariable Long showId) {
        List<ShowSeatDto> showSeatDtos = userService.getShowSeatsUsingRedisV1(showId);
        return ResponseEntity.ok(showSeatDtos);
    }
    
}
