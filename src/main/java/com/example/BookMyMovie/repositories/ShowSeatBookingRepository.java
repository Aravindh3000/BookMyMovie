package com.example.BookMyMovie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BookMyMovie.models.ShowSeatBooking;

public interface ShowSeatBookingRepository extends JpaRepository<ShowSeatBooking, Long>{
}
