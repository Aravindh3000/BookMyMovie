package com.example.BookMyMovie.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "show_seat_bookings")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatBooking extends BaseEntity {

    @ManyToOne()
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne()
    @JoinColumn(name = "show_seat_id", nullable = false)
    private ShowSeat showSeat;
}

