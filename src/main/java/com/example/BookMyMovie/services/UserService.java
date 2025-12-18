package com.example.BookMyMovie.services;

import com.example.BookMyMovie.Redis.RedisSeatService;
import com.example.BookMyMovie.dtos.LoginRequestDto;
import com.example.BookMyMovie.dtos.LoginResponseDto;
import com.example.BookMyMovie.dtos.ReserveResponseDto;
import com.example.BookMyMovie.dtos.ReserveSeatDto;
import com.example.BookMyMovie.dtos.ShowSeatDto;
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
import com.example.BookMyMovie.models.Show;
import com.example.BookMyMovie.models.User;
import com.example.BookMyMovie.models.UserRole;
import com.example.BookMyMovie.repositories.BookingRepository;
import com.example.BookMyMovie.repositories.ShowRepository;
import com.example.BookMyMovie.repositories.ShowSeatBookingRepository;
import com.example.BookMyMovie.repositories.ShowSeatRepository;
import com.example.BookMyMovie.repositories.UserRepository;
import com.example.BookMyMovie.utils.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.OptimisticLockException;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ShowSeatRepository showSeatRepository;
    private final ShowSeatBookingRepository showSeatBookingRepository;
    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisSeatService redisSeatService;
    private final ObjectMapper objectMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            ShowSeatRepository showSeatRepository, ShowSeatBookingRepository showSeatBookingRepository,
            BookingRepository bookingRepository, ShowRepository showRepository,
            RedisTemplate<String, String> redisTemplate, RedisSeatService redisSeatService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.showSeatRepository = showSeatRepository;
        this.showSeatBookingRepository = showSeatBookingRepository;
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.redisTemplate = redisTemplate;
        this.redisSeatService = redisSeatService;
        this.objectMapper = objectMapper;
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

    public ReserveResponseDto reserveSeatsUsingRedis(Long showId, List<Long> seatIds, String userEmail) {

        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new BadRequestException("User Not Found"));

            // 1️⃣ Ensure cached seats exist (else load from DB)
            String cached = redisSeatService.getCachedSeats(showId);
            if (cached == null) {
                loadShowSeatsToRedis(showId);
                cached = redisSeatService.getCachedSeats(showId);
            }

            // 2️⃣ Validate seatIds belong to show
            List<ShowSeatDto> seatList = objectMapper.readValue(
                    cached, new TypeReference<List<ShowSeatDto>>() {
                    });

            Set<Long> validSeatIds = seatList.stream()
                    .map(ShowSeatDto::getId)
                    .collect(Collectors.toSet());

            // List<ShowSeatDto> selectedSeatsDto = new ArrayList<>();

            for (Long seatId : seatIds) {
                if (!validSeatIds.contains(seatId)) {
                    throw new BadRequestException("Seat " + seatId + " does not belong to show " + showId);
                }

            }

            // 3️⃣ Try locking seats one-by-one
            for (Long seatId : seatIds) {
                boolean locked = redisSeatService.lockSeat(showId, seatId, user.getId());
                if (!locked) {
                    throw new RuntimeException("Seat " + seatId + " already locked!");
                }
            }

            Booking booking = new Booking();
            booking.setUser(user);
            booking.setAmount(seatList.stream().filter(x -> seatIds.contains(x.getId())).map(x -> x.getPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            booking.setStatus(BookingStatus.PENDING);
            booking = bookingRepository.save(booking);

            List<ShowSeat> showSeats = showSeatRepository.findAllByIdInAndStatus(seatIds,
                    SeatStatus.AVAILABLE);

            if (showSeats.size() != seatIds.size()) {
                throw new BadRequestException("Some of the seats are already booked");
            }

            List<ShowSeatBooking> bookings = new ArrayList<>();

            for (ShowSeat showSeat : showSeats) {
                ShowSeatBooking showSeatBooking = new ShowSeatBooking();
                showSeatBooking.setBooking(booking);
                showSeatBooking.setShowSeat(showSeat);
                bookings.add(showSeatBooking);
            }

            showSeatBookingRepository.saveAll(bookings);

            ReserveResponseDto res = new ReserveResponseDto();
            res.setBookingId(booking.getId());
            res.setTotalAmount(booking.getAmount());
            res.setSeats(seatIds);

            return res;
        } catch (JsonProcessingException e) {
            System.err.println("❗ JSON Error while processing seats for showId = " + showId);
            e.printStackTrace();
            throw new RuntimeException("Failed to parse JSON for Redis cache", e);
        }
    }

    public List<ShowSeatDto> getShowSeats(Long showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new BadRequestException("Show not found"));
        List<ShowSeatDto> showSeatDtos = new ArrayList<>();
        // Get list of show seats

        List<ShowSeat> showSeats = showSeatRepository.findAllByShow(show);

        for (ShowSeat showSeat : showSeats) {
            ShowSeatDto showSeatDto = new ShowSeatDto();
            showSeatDto.setId(showSeat.getId());
            showSeatDto.setSeatNumber(showSeat.getSeat().getNumber());
            showSeatDto.setSeatCondition(showSeat.getSeat().getCondition().name());
            showSeatDto.setSeatStatus(showSeat.getStatus().name());
            showSeatDto.setPrice(showSeat.getPrice());
            showSeatDtos.add(showSeatDto);
        }
        return showSeatDtos;
    }

    public List<ShowSeatDto> getShowSeatsUsingRedisV1(Long showId) {
        try {
            // Fetch cached seats (DB snapshot refreshed every 5 min)
            String cachedJson = redisSeatService.getCachedSeats(showId);

            if (cachedJson != null) {

                List<ShowSeatDto> seats = objectMapper.readValue(
                        cachedJson,
                        new TypeReference<List<ShowSeatDto>>() {
                        });

                // Overlay locks
                for (ShowSeatDto seat : seats) {

                    String lockKey = "lock:show:" + showId + ":seat:" + seat.getId();
                    String lockedBy = redisTemplate.opsForValue().get(lockKey);

                    if (lockedBy != null) {
                        seat.setSeatStatus(SeatStatus.RESERVED.name()); // or LOCKED
                    }
                }

                return seats;
            } else {
                return loadShowSeatsToRedis(showId);
            }
        } catch (JsonProcessingException e) {
            System.err.println("❗ JSON Error while processing seats for showId = " + showId);
            e.printStackTrace();
            throw new RuntimeException("Failed to parse JSON for Redis cache", e);
        }

    }

    public List<ShowSeatDto> loadShowSeatsToRedis(Long showId) {

        try {
            Show show = showRepository.findById(showId)
                    .orElseThrow(() -> new BadRequestException("Show not found"));
            List<ShowSeat> showSeats = showSeatRepository.findAllByShow(show);
            System.out.println("📚 DB returned " + showSeats.size() + " seats");
            List<ShowSeatDto> dtos = toDtos(showSeats);
            String json = objectMapper.writeValueAsString(dtos);
            redisSeatService.cacheShowSeats(showId, json);

            System.out.println("🟩 Cached " + dtos.size() + " seats to Redis with TTL=5 min");
            System.out.println("📥 Stored JSON size = " + json.length() + " chars");

            return dtos;
        } catch (JsonProcessingException e) {
            System.err.println("❗ JSON Error while processing seats for showId = " + showId);
            e.printStackTrace();
            throw new RuntimeException("Failed to parse JSON for Redis cache", e);
        }
    }

    public List<ShowSeatDto> toDtos(List<ShowSeat> showSeats) {
        List<ShowSeatDto> showSeatDtos = new ArrayList<>();
        // Get list of show seats
        for (ShowSeat showSeat : showSeats) {
            ShowSeatDto showSeatDto = new ShowSeatDto();
            showSeatDto.setId(showSeat.getId());
            showSeatDto.setSeatNumber(showSeat.getSeat().getNumber());
            showSeatDto.setSeatCondition(showSeat.getSeat().getCondition().name());
            showSeatDto.setSeatStatus(showSeat.getStatus().name());
            showSeatDto.setPrice(showSeat.getPrice());
            showSeatDtos.add(showSeatDto);
        }
        return showSeatDtos;
    }

}