package com.example.BookMyMovie.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "seats")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Seat extends BaseEntity {
    
    @Column(nullable = false)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatClass seatClass; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatCondition condition; 

    @ManyToOne
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;
}