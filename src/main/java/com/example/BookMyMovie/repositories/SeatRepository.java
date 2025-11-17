package com.example.BookMyMovie.repositories;

import com.example.BookMyMovie.models.Screen;
import com.example.BookMyMovie.models.Seat;
import com.example.BookMyMovie.models.SeatCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreenAndCondition(Screen screen, SeatCondition condition);
}
