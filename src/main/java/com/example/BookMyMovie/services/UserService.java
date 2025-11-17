package com.example.BookMyMovie.services;

import com.example.BookMyMovie.dtos.LoginRequestDto;
import com.example.BookMyMovie.dtos.LoginResponseDto;
import com.example.BookMyMovie.dtos.ReserveResponseDto;
import com.example.BookMyMovie.dtos.ReserveSeatDto;
import com.example.BookMyMovie.dtos.UserCreateRequest;
import com.example.BookMyMovie.dtos.UserRefreshTokenDto;
import com.example.BookMyMovie.dtos.UserResponse;
import com.example.BookMyMovie.dtos.UserUpdateRequest;
import com.example.BookMyMovie.exceptions.BadRequestException;
import com.example.BookMyMovie.exceptions.UserNotFoundException;
import com.example.BookMyMovie.models.ShowSeat;
import com.example.BookMyMovie.models.ShowSeatBooking;
import com.example.BookMyMovie.models.Booking;
import com.example.BookMyMovie.models.BookingStatus;
import com.example.BookMyMovie.models.SeatStatus;
import com.example.BookMyMovie.models.User;
import com.example.BookMyMovie.models.UserRole;
import com.example.BookMyMovie.repositories.BookingRepository;
import com.example.BookMyMovie.repositories.ShowSeatBookingRepository;
import com.example.BookMyMovie.repositories.ShowSeatRepository;
import com.example.BookMyMovie.repositories.UserRepository;
import com.example.BookMyMovie.utils.JwtUtil;

import jakarta.persistence.OptimisticLockException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ShowSeatRepository showSeatRepository;
    private final ShowSeatBookingRepository showSeatBookingRepository;
    private final BookingRepository bookingRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            ShowSeatRepository showSeatRepository, ShowSeatBookingRepository showSeatBookingRepository,
            BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.showSeatRepository = showSeatRepository;
        this.showSeatBookingRepository = showSeatBookingRepository;
        this.bookingRepository = bookingRepository;
    }

    public UserResponse create(UserCreateRequest req) {

        boolean existingUser = userRepository.existsByEmailOrPhone(req.getEmail(), req.getPhone());
        if (existingUser) {
            throw new BadRequestException("User already exists with this email or phone");
        }

        User user = new User();

        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(UserRole.valueOf(req.getRole()));
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public LoginResponseDto login(LoginRequestDto loginReq) {

        User user = userRepository.findByEmail(loginReq.getEmail())
                .orElseThrow(() -> new UserNotFoundException(loginReq.getEmail()));

        LoginResponseDto loginResponseDto = new LoginResponseDto();

        if (!passwordEncoder.matches(loginReq.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", user.getEmail());
        payload.put("role", user.getRole());

        Map<String, String> tokens = generateToken(user, payload);

        loginResponseDto.setAccessToken(tokens.get("accessToken"));
        loginResponseDto.setRefreshToken(tokens.get("refreshToken"));
        loginResponseDto.setExpiresIn(jwtUtil.getJwtAccessTokenValidity());

        return loginResponseDto;
    }

    public LoginResponseDto refreshAccessToken(UserRefreshTokenDto refreshTokenDto) {
        try {
            String refreshToken = refreshTokenDto.getRefreshToken();
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                throw new BadRequestException("Expired token");
            }

            String email = jwtUtil.extractUsername(refreshToken);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadRequestException("Invalid Refresh Token"));

            Map<String, Object> tokenPayload = new HashMap<>();

            tokenPayload.put("role", refreshTokenDto.getRole());
            tokenPayload.put("email", String.valueOf(user.getEmail()));

            Map<String, String> tokens = generateToken(user, tokenPayload);

            LoginResponseDto loginResponseDto = new LoginResponseDto();
            loginResponseDto.setAccessToken(tokens.get("accessToken"));
            loginResponseDto.setRefreshToken(tokens.get("refreshToken"));
            loginResponseDto.setExpiresIn(jwtUtil.getJwtAccessTokenValidity());

            return loginResponseDto;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Map<String, String> generateToken(User user, Map<String, Object> payload) {
        String accessToken = jwtUtil.createAccessToken(payload, user.getEmail());
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return toResponse(user);
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserResponse update(Long id, UserUpdateRequest req) {
        User existing = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (req.getName() != null)
            existing.setName(req.getName());
        if (req.getPhone() != null)
            existing.setPhone(req.getPhone());
        if (req.getEmail() != null)
            existing.setEmail(req.getEmail());
        if (req.getPassword() != null)
            existing.setPassword(passwordEncoder.encode(req.getPassword()));

        return toResponse(userRepository.save(existing));
    }

    private UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setName(user.getName());
        res.setPhone(user.getPhone());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole().name());
        return res;
    }

    @Transactional
    public ReserveResponseDto reserveShowSeat(ReserveSeatDto reserveReq) {

        List<ShowSeat> showSeats = showSeatRepository.findAllByIdInAndStatus(reserveReq.getShowSeats(),
                SeatStatus.AVAILABLE);

        if (showSeats.size() != reserveReq.getShowSeats().size()) {
            throw new BadRequestException("Some of the seats are already booked");
        }

        User user = userRepository.findByEmail(reserveReq.getUserEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found for given id"));

        try {

            // create booking record and show seat booking with calc amount
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setAmount(showSeats.stream().map(x -> x.getPrice()).reduce(BigDecimal.ZERO, BigDecimal::add));
            booking.setStatus(BookingStatus.PENDING);
            booking = bookingRepository.save(booking);

            for (ShowSeat showSeat : showSeats) {
                ShowSeatBooking showSeatBooking = new ShowSeatBooking();
                showSeatBooking.setBooking(booking);
                showSeatBooking.setShowSeat(showSeat);
                
                showSeatBookingRepository.save(showSeatBooking);

                // Change status to Reserved
                showSeat.setStatus(SeatStatus.RESERVED);
            }

            showSeatRepository.saveAll(showSeats);
            
            ReserveResponseDto res = new ReserveResponseDto();
            res.setBookingId(booking.getId());
            res.setTotalAmount(booking.getAmount());
            res.setSeats(showSeats.stream().map(x -> x.getId()).toList());

            return res;
        } catch (OptimisticLockException e) {
            throw new BadRequestException("Ticket Already booked");
        }
    }
}