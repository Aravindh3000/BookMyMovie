package com.example.BookMyMovie.repositories;

import com.example.BookMyMovie.models.SeatStatus;
import com.example.BookMyMovie.models.ShowSeat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findAllByIdInAndStatus(List<Long> showSeatIds, SeatStatus status);
}
