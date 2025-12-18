package com.example.BookMyMovie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BookMyMovie.models.ShowSeatBooking;
import com.example.BookMyMovie.models.Booking;
import java.util.List;

public interface ShowSeatBookingRepository extends JpaRepository<ShowSeatBooking, Long>{
    List<ShowSeatBooking> findAllByBooking(Booking booking);
}
