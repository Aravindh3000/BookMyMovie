package com.example.BookMyMovie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BookMyMovie.models.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long>{
    
}
