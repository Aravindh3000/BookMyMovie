package com.example.BookMyMovie.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class MultipleShowsCreateRequest {
    
    @NotBlank(message = "Movie title is required")
    private String movieTitle;
    
    @NotBlank(message = "Theatre name is required")
    private String theatreName;
    
    @NotBlank(message = "Screen name is required")
    private String screenName;
    
    @NotEmpty(message = "Show times are required")
    private List<ShowTimeDto> showTimes;
    
    @NotEmpty(message = "Show seats configuration is required")
    private List<ShowCreateRequest.ShowSeatConfigurationDto> showSeatConfigurations;

    // Constructors
    public MultipleShowsCreateRequest() {}

    public MultipleShowsCreateRequest(String movieTitle, String theatreName, String screenName, 
                                    List<ShowTimeDto> showTimes, 
                                    List<ShowCreateRequest.ShowSeatConfigurationDto> showSeatConfigurations) {
        this.movieTitle = movieTitle;
        this.theatreName = theatreName;
        this.screenName = screenName;
        this.showTimes = showTimes;
        this.showSeatConfigurations = showSeatConfigurations;
    }

    // Getters and Setters
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

    public List<ShowTimeDto> getShowTimes() {
        return showTimes;
    }

    public void setShowTimes(List<ShowTimeDto> showTimes) {
        this.showTimes = showTimes;
    }

    public List<ShowCreateRequest.ShowSeatConfigurationDto> getShowSeatConfigurations() {
        return showSeatConfigurations;
    }

    public void setShowSeatConfigurations(List<ShowCreateRequest.ShowSeatConfigurationDto> showSeatConfigurations) {
        this.showSeatConfigurations = showSeatConfigurations;
    }

    // Inner class for show time
    public static class ShowTimeDto {
        
        @NotNull(message = "Start time is required")
        private LocalDateTime startTime;

        // Constructors
        public ShowTimeDto() {}

        public ShowTimeDto(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        // Getters and Setters
        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }
    }
}
