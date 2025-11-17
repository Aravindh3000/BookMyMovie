package com.example.BookMyMovie.dtos;

import java.time.LocalDateTime;

public class TheatreCreateResponse {
    
    private Long theatreId;
    private String theatreName;
    private String theatreAddress;
    private Long screenId;
    private String screenName;
    private int totalSeatsCreated;
    private LocalDateTime createdAt;

    // Constructors
    public TheatreCreateResponse() {}

    public TheatreCreateResponse(Long theatreId, String theatreName, String theatreAddress, 
                               Long screenId, String screenName, int totalSeatsCreated, LocalDateTime createdAt) {
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.theatreAddress = theatreAddress;
        this.screenId = screenId;
        this.screenName = screenName;
        this.totalSeatsCreated = totalSeatsCreated;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getTheatreId() {
        return theatreId;
    }

    public void setTheatreId(Long theatreId) {
        this.theatreId = theatreId;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getTheatreAddress() {
        return theatreAddress;
    }

    public void setTheatreAddress(String theatreAddress) {
        this.theatreAddress = theatreAddress;
    }

    public Long getScreenId() {
        return screenId;
    }

    public void setScreenId(Long screenId) {
        this.screenId = screenId;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public int getTotalSeatsCreated() {
        return totalSeatsCreated;
    }

    public void setTotalSeatsCreated(int totalSeatsCreated) {
        this.totalSeatsCreated = totalSeatsCreated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
