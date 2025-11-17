package com.example.BookMyMovie.services;

import com.example.BookMyMovie.dtos.*;
import com.example.BookMyMovie.exceptions.BadRequestException;
import com.example.BookMyMovie.models.*;
import com.example.BookMyMovie.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AdminService {

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    public TheatreCreateResponse createTheatreWithScreenAndSeats(TheatreCreateRequest request) {
        // Create Theatre
        Theatre theatre = new Theatre();
        theatre.setName(request.getTheatreName());
        theatre.setAddress(request.getTheatreAddress());
        theatre = theatreRepository.save(theatre);

        // Create Screen
        Screen screen = new Screen();
        screen.setName(request.getScreenName());
        screen.setTheatre(theatre);
        screen.setCondition(request.getScreenCondition());
        screen = screenRepository.save(screen);

        // Create Seats
        int totalSeatsCreated = 0;
        List<Seat> seats = new ArrayList<>();

        for (TheatreCreateRequest.SeatConfigurationDto config : request.getSeatConfigurations()) {
            for (int row = 1; row <= config.getRows(); row++) {
                for (int col = 1; col <= config.getColumns(); col++) {
                    Seat seat = new Seat();
                    seat.setNumber(generateSeatNumber(row, col));
                    seat.setSeatClass(config.getSeatClass());
                    seat.setCondition(config.getSeatCondition());
                    seat.setScreen(screen);
                    seats.add(seat);
                    totalSeatsCreated++;
                }
            }
        }

        // Save all seats in batch
        seatRepository.saveAll(seats);

        // Create response
        TheatreCreateResponse response = new TheatreCreateResponse();
        response.setTheatreId(theatre.getId());
        response.setTheatreName(theatre.getName());
        response.setTheatreAddress(theatre.getAddress());
        response.setScreenId(screen.getId());
        response.setScreenName(screen.getName());
        response.setTotalSeatsCreated(totalSeatsCreated);
        response.setCreatedAt(LocalDateTime.now());

        return response;
    }

    private String generateSeatNumber(int row, int col) {
        // Generate seat number like A1, A2, B1, B2, etc.
        char rowChar = (char) ('A' + row - 1);
        return rowChar + String.valueOf(col);
    }

    // Movie Management Methods
    public MovieCreateResponse createMovie(MovieCreateRequest request) {
        System.out.println("service movie");
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie = movieRepository.save(movie);

        MovieCreateResponse response = new MovieCreateResponse();
        response.setMovieId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setDurationMinutes(movie.getDurationMinutes());
        response.setCreatedAt(LocalDateTime.now());

        return response;
    }

    // Show Management Methods
    public ShowCreateResponse createShow(ShowCreateRequest request) {

        // Find movie by title
        Movie movie = movieRepository.findByTitle(request.getMovieTitle())
                .orElseThrow(() -> new RuntimeException("Movie not found with title: " + request.getMovieTitle()));

        // Find theatre and screen
        Theatre theatre = theatreRepository.findByName(request.getTheatreName())
                .orElseThrow(() -> new BadRequestException("Theatre not found with name: " + request.getTheatreName()));

        Screen screen = screenRepository.findByNameAndTheatre(request.getScreenName(), theatre)
                .orElseThrow(() -> new BadRequestException("Screen not found with name: " + request.getScreenName()
                        + " in theatre: " + request.getTheatreName()));

        // Calculate end time based on movie duration
        LocalDateTime endTime = request.getStartTime().plusMinutes(movie.getDurationMinutes());

        // Create show
        Show show = new Show();
        show.setStartTime(request.getStartTime());
        show.setEndTime(endTime);
        show.setMovie(movie);
        show.setScreen(screen);
        show = showRepository.save(show);

        // Assign show seats
        int totalSeatsAssigned = assignShowSeats(show, screen, request.getShowSeatConfigurations());

        // Create response
        ShowCreateResponse response = new ShowCreateResponse();
        response.setShowId(show.getId());
        response.setMovieTitle(movie.getTitle());
        response.setTheatreName(theatre.getName());
        response.setScreenName(screen.getName());
        response.setStartTime(show.getStartTime());
        response.setEndTime(show.getEndTime());
        response.setTotalSeatsAssigned(totalSeatsAssigned);
        response.setCreatedAt(LocalDateTime.now());

        return response;

    }

    public List<ShowCreateResponse> createMultipleShows(MultipleShowsCreateRequest request) {
        // Find movie by title
        Movie movie = movieRepository.findByTitle(request.getMovieTitle())
                .orElseThrow(() -> new BadRequestException("Movie not found with title: " + request.getMovieTitle()));

        // Find theatre and screen
        Theatre theatre = theatreRepository.findByName(request.getTheatreName())
                .orElseThrow(() -> new BadRequestException("Theatre not found with name: " + request.getTheatreName()));

        Screen screen = screenRepository.findByNameAndTheatre(request.getScreenName(), theatre)
                .orElseThrow(() -> new BadRequestException("Screen not found with name: " + request.getScreenName()
                        + " in theatre: " + request.getTheatreName()));

        List<ShowCreateResponse> responses = new ArrayList<>();

        for (MultipleShowsCreateRequest.ShowTimeDto showTime : request.getShowTimes()) {
            // Calculate end time based on movie duration
            LocalDateTime endTime = showTime.getStartTime().plusMinutes(movie.getDurationMinutes());

            // Create show
            Show show = new Show();
            show.setStartTime(showTime.getStartTime());
            show.setEndTime(endTime);
            show.setMovie(movie);
            show.setScreen(screen);
            show = showRepository.save(show);

            // Assign show seats
            int totalSeatsAssigned = assignShowSeats(show, screen, request.getShowSeatConfigurations());

            // Create response
            ShowCreateResponse response = new ShowCreateResponse();
            response.setShowId(show.getId());
            response.setMovieTitle(movie.getTitle());
            response.setTheatreName(theatre.getName());
            response.setScreenName(screen.getName());
            response.setStartTime(show.getStartTime());
            response.setEndTime(show.getEndTime());
            response.setTotalSeatsAssigned(totalSeatsAssigned);
            response.setCreatedAt(LocalDateTime.now());

            responses.add(response);
        }

        return responses;
    }

    private int assignShowSeats(Show show, Screen screen,
            List<ShowCreateRequest.ShowSeatConfigurationDto> seatConfigurations) {
        List<ShowSeat> showSeats = new ArrayList<>();
        int totalSeatsAssigned = 0;

        // Get all available seats for this screen
        List<Seat> availableSeats = seatRepository.findByScreenAndCondition(screen, SeatCondition.AVAILABLE);

        for (ShowCreateRequest.ShowSeatConfigurationDto config : seatConfigurations) {
            System.out.println("Processing seat class: " + config.getSeatClass() + " with price: " + config.getPrice());
            // Filter seats by seat class
            List<Seat> seatsForClass = availableSeats.stream()
                    .filter(seat -> seat.getSeatClass() == config.getSeatClass())
                    .toList();

            // Create show seats for each seat
            for (Seat seat : seatsForClass) {
                ShowSeat showSeat = new ShowSeat();
                showSeat.setShow(show);
                showSeat.setSeat(seat);
                showSeat.setStatus(SeatStatus.AVAILABLE);
                showSeat.setPrice(config.getPrice());
                showSeats.add(showSeat);
                totalSeatsAssigned++;
            }
        }

        // Save all show seats in batch
        showSeatRepository.saveAll(showSeats);

        return totalSeatsAssigned;
    }
}
