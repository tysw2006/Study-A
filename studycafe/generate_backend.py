import os

base_dir = "src/main/java/com/studycafe"
dirs = ["domain", "repository", "service", "controller", "dto", "config"]

for d in dirs:
    os.makedirs(os.path.join(base_dir, d), exist_ok=True)

# ----------------- ENUMS -----------------
with open(os.path.join(base_dir, "domain", "Role.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.domain;

public enum Role {
    USER, ADMIN
}
""")

with open(os.path.join(base_dir, "domain", "ReservationStatus.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.domain;

public enum ReservationStatus {
    ACTIVE, CANCELLED, COMPLETED
}
""")

with open(os.path.join(base_dir, "domain", "ReservationType.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.domain;

public enum ReservationType {
    SEAT, ROOM
}
""")

# ----------------- ENTITIES -----------------
with open(os.path.join(base_dir, "domain", "User.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
""")

with open(os.path.join(base_dir, "domain", "Seat.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "seat_number", unique = true, nullable = false)
    private String seatNumber;
    
    @Column(name = "is_active")
    private Boolean isActive;
}
""")

with open(os.path.join(base_dir, "domain", "StudyRoom.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudyRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_name", unique = true, nullable = false)
    private String roomName;
    
    @Column(nullable = false)
    private Integer capacity;
    
    @Column(name = "is_active")
    private Boolean isActive;
}
""")

with open(os.path.join(base_dir, "domain", "Reservation.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_room_id")
    private StudyRoom studyRoom;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_type", nullable = false)
    private ReservationType reservationType;
}
""")

# ----------------- REPOSITORIES -----------------
with open(os.path.join(base_dir, "repository", "UserRepository.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.repository;

import com.studycafe.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
""")

with open(os.path.join(base_dir, "repository", "SeatRepository.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.repository;

import com.studycafe.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
""")

with open(os.path.join(base_dir, "repository", "StudyRoomRepository.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.repository;

import com.studycafe.domain.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {
}
""")

with open(os.path.join(base_dir, "repository", "ReservationRepository.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.repository;

import com.studycafe.domain.Reservation;
import com.studycafe.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);
    List<Reservation> findByUserIdOrderByStartTimeDesc(Long userId);
    
    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND r.endTime > :now")
    List<Reservation> findAllActiveReservations(@Param("now") LocalDateTime now);
    
    @Query("SELECT r FROM Reservation r WHERE r.seat.id = :seatId AND r.status = 'ACTIVE' AND r.endTime > :now")
    List<Reservation> findActiveBySeatId(@Param("seatId") Long seatId, @Param("now") LocalDateTime now);
}
""")

# ----------------- DTOs -----------------
with open(os.path.join(base_dir, "dto", "AuthRequest.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
    private String name;
}
""")

with open(os.path.join(base_dir, "dto", "ReservationRequest.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationRequest {
    private Long seatId;
    private Long roomId;
    private Integer hours;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
""")

# ----------------- SERVICES -----------------
with open(os.path.join(base_dir, "service", "AuthService.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.service;

import com.studycafe.domain.Role;
import com.studycafe.domain.User;
import com.studycafe.dto.AuthRequest;
import com.studycafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    @Transactional
    public User register(AuthRequest req) {
        if(userRepository.findByEmail(req.getEmail()).isPresent()){
            throw new RuntimeException("Email already exists");
        }
        User user = User.builder()
                .email(req.getEmail())
                .password(req.getPassword()) // 실제로는 BCrypt 등 암호화 필요
                .name(req.getName())
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    public User login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    }
}
""")

with open(os.path.join(base_dir, "service", "ReservationService.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.service;

import com.studycafe.domain.*;
import com.studycafe.dto.ReservationRequest;
import com.studycafe.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final StudyRoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional
    public Reservation reserveSeat(Long userId, ReservationRequest req) {
        User user = userRepository.findById(userId).orElseThrow();
        Seat seat = seatRepository.findById(req.getSeatId()).orElseThrow();
        
        // 중복 예약 체크 생략 (단순화)
        LocalDateTime now = LocalDateTime.now();
        Reservation res = Reservation.builder()
                .user(user)
                .seat(seat)
                .startTime(now)
                .endTime(now.plusHours(req.getHours()))
                .status(ReservationStatus.ACTIVE)
                .reservationType(ReservationType.SEAT)
                .build();
        return reservationRepository.save(res);
    }
    
    @Transactional
    public Reservation reserveRoom(Long userId, ReservationRequest req) {
        User user = userRepository.findById(userId).orElseThrow();
        StudyRoom room = roomRepository.findById(req.getRoomId()).orElseThrow();
        
        Reservation res = Reservation.builder()
                .user(user)
                .studyRoom(room)
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .status(ReservationStatus.ACTIVE)
                .reservationType(ReservationType.ROOM)
                .build();
        return reservationRepository.save(res);
    }

    @Transactional
    public void cancelReservation(Long resId, Long userId) {
        Reservation res = reservationRepository.findById(resId).orElseThrow();
        if(!res.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }
        res.setStatus(ReservationStatus.CANCELLED);
    }
    
    public List<Reservation> getMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByStartTimeDesc(userId);
    }
    
    public List<Reservation> getActiveReservations() {
        return reservationRepository.findAllActiveReservations(LocalDateTime.now());
    }
}
""")

# ----------------- CONTROLLERS -----------------
with open(os.path.join(base_dir, "controller", "PageController.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
""")

with open(os.path.join(base_dir, "controller", "ApiController.java"), "w", encoding="utf-8") as f:
    f.write("""package com.studycafe.controller;

import com.studycafe.domain.*;
import com.studycafe.dto.*;
import com.studycafe.service.*;
import com.studycafe.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    
    private final AuthService authService;
    private final ReservationService reservationService;
    private final SeatRepository seatRepository;
    private final StudyRoomRepository roomRepository;

    // --- AUTH ---
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }
    
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req, HttpSession session) {
        try {
            User user = authService.login(req.getEmail(), req.getPassword());
            session.setAttribute("user", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out");
    }
    
    @GetMapping("/auth/me")
    public ResponseEntity<?> me(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(user);
    }

    // --- SEATS & ROOMS ---
    @GetMapping("/seats")
    public ResponseEntity<List<Seat>> getSeats() {
        return ResponseEntity.ok(seatRepository.findAll());
    }
    
    @GetMapping("/rooms")
    public ResponseEntity<List<StudyRoom>> getRooms() {
        return ResponseEntity.ok(roomRepository.findAll());
    }
    
    @GetMapping("/active-status")
    public ResponseEntity<List<Reservation>> getActiveStatus() {
        return ResponseEntity.ok(reservationService.getActiveReservations());
    }

    // --- RESERVATIONS ---
    @PostMapping("/reservations/seat")
    public ResponseEntity<?> reserveSeat(@RequestBody ReservationRequest req, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(reservationService.reserveSeat(user.getId(), req));
    }
    
    @PostMapping("/reservations/room")
    public ResponseEntity<?> reserveRoom(@RequestBody ReservationRequest req, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(reservationService.reserveRoom(user.getId(), req));
    }
    
    @GetMapping("/reservations/my")
    public ResponseEntity<?> getMyReservations(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(reservationService.getMyReservations(user.getId()));
    }
    
    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) return ResponseEntity.status(401).build();
        reservationService.cancelReservation(id, user.getId());
        return ResponseEntity.ok("Cancelled");
    }
}
""")

print("Backend files generated successfully.")
