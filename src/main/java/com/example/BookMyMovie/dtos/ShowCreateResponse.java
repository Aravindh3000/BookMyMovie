package com.example.BookMyMovie.dtos;

import java.time.LocalDateTime;

public class ShowCreateResponse {
    
    private Long showId;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalSeatsAssigned;
    private LocalDateTime createdAt;

    // Constructors
    public ShowCreateResponse() {}

    public ShowCreateResponse(Long showId, String movieTitle, String theatreName, String screenName, 
                            LocalDateTime startTime, LocalDateTime endTime, int totalSeatsAssigned, LocalDateTime createdAt) {
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.theatreName = theatreName;
        this.screenName = screenName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalSeatsAssigned = totalSeatsAssigned;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getTotalSeatsAssigned() {
        return totalSeatsAssigned;
    }

    public void setTotalSeatsAssigned(int totalSeatsAssigned) {
        this.totalSeatsAssigned = totalSeatsAssigned;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
