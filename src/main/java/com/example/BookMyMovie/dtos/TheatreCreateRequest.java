package com.example.BookMyMovie.dtos;

import com.example.BookMyMovie.models.SeatClass;
import com.example.BookMyMovie.models.SeatCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class TheatreCreateRequest {
    
    @NotBlank(message = "Theatre name is required")
    private String theatreName;
    
    @NotBlank(message = "Theatre address is required")
    private String theatreAddress;
    
    @NotBlank(message = "Screen name is required")
    private String screenName;
    
    @NotNull(message = "Screen condition is required")
    private SeatCondition screenCondition;
    
    @NotEmpty(message = "Seat configurations are required")
    private List<SeatConfigurationDto> seatConfigurations;

    // Constructors
    public TheatreCreateRequest() {}

    public TheatreCreateRequest(String theatreName, String theatreAddress, String screenName, 
                              SeatCondition screenCondition, List<SeatConfigurationDto> seatConfigurations) {
        this.theatreName = theatreName;
        this.theatreAddress = theatreAddress;
        this.screenName = screenName;
        this.screenCondition = screenCondition;
        this.seatConfigurations = seatConfigurations;
    }

    // Getters and Setters
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

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public SeatCondition getScreenCondition() {
        return screenCondition;
    }

    public void setScreenCondition(SeatCondition screenCondition) {
        this.screenCondition = screenCondition;
    }

    public List<SeatConfigurationDto> getSeatConfigurations() {
        return seatConfigurations;
    }

    public void setSeatConfigurations(List<SeatConfigurationDto> seatConfigurations) {
        this.seatConfigurations = seatConfigurations;
    }

    // Inner class for seat configuration
    public static class SeatConfigurationDto {
        
        @NotNull(message = "Seat class is required")
        private SeatClass seatClass;
        
        @Positive(message = "Number of rows must be positive")
        private int rows;
        
        @Positive(message = "Number of columns must be positive")
        private int columns;
        
        @NotNull(message = "Seat condition is required")
        private SeatCondition seatCondition;

        // Constructors
        public SeatConfigurationDto() {}

        public SeatConfigurationDto(SeatClass seatClass, int rows, int columns, SeatCondition seatCondition) {
            this.seatClass = seatClass;
            this.rows = rows;
            this.columns = columns;
            this.seatCondition = seatCondition;
        }

        // Getters and Setters
        public SeatClass getSeatClass() {
            return seatClass;
        }

        public void setSeatClass(SeatClass seatClass) {
            this.seatClass = seatClass;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public int getColumns() {
            return columns;
        }

        public void setColumns(int columns) {
            this.columns = columns;
        }

        public SeatCondition getSeatCondition() {
            return seatCondition;
        }

        public void setSeatCondition(SeatCondition seatCondition) {
            this.seatCondition = seatCondition;
        }
    }
}
