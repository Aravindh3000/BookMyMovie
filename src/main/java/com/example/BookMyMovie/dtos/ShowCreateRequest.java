package com.example.BookMyMovie.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ShowCreateRequest {
    
    @NotBlank(message = "Movie title is required")
    private String movieTitle;
    
    @NotBlank(message = "Theatre name is required")
    private String theatreName;
    
    @NotBlank(message = "Screen name is required")
    private String screenName;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotEmpty(message = "Show seats configuration is required")
    private List<ShowSeatConfigurationDto> showSeatConfigurations;

    // Constructors
    public ShowCreateRequest() {}

    public ShowCreateRequest(String movieTitle, String theatreName, String screenName, 
                           LocalDateTime startTime, List<ShowSeatConfigurationDto> showSeatConfigurations) {
        this.movieTitle = movieTitle;
        this.theatreName = theatreName;
        this.screenName = screenName;
        this.startTime = startTime;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public List<ShowSeatConfigurationDto> getShowSeatConfigurations() {
        return showSeatConfigurations;
    }

    public void setShowSeatConfigurations(List<ShowSeatConfigurationDto> showSeatConfigurations) {
        this.showSeatConfigurations = showSeatConfigurations;
    }

    // Inner class for show seat configuration
    public static class ShowSeatConfigurationDto {
        
        @NotNull(message = "Seat class is required")
        private com.example.BookMyMovie.models.SeatClass seatClass;
        
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private BigDecimal price;

        // Constructors
        public ShowSeatConfigurationDto() {}

        public ShowSeatConfigurationDto(com.example.BookMyMovie.models.SeatClass seatClass, BigDecimal price) {
            this.seatClass = seatClass;
            this.price = price;
        }

        // Getters and Setters
        public com.example.BookMyMovie.models.SeatClass getSeatClass() {
            return seatClass;
        }

        public void setSeatClass(com.example.BookMyMovie.models.SeatClass seatClass) {
            this.seatClass = seatClass;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
