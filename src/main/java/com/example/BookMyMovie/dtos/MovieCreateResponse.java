package com.example.BookMyMovie.dtos;

import java.time.LocalDateTime;

public class MovieCreateResponse {
    
    private Long movieId;
    private String title;
    private int durationMinutes;
    private LocalDateTime createdAt;

    // Constructors
    public MovieCreateResponse() {}

    public MovieCreateResponse(Long movieId, String title, int durationMinutes, LocalDateTime createdAt) {
        this.movieId = movieId;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
