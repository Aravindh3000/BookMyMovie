package com.example.BookMyMovie.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class MovieCreateRequest {
    
    @NotBlank(message = "Movie title is required")
    private String title;
    
    @Positive(message = "Duration must be positive")
    private int durationMinutes;

    // Constructors
    public MovieCreateRequest() {}

    public MovieCreateRequest(String title, int durationMinutes) {
        this.title = title;
        this.durationMinutes = durationMinutes;
    }

    // Getters and Setters
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
}
