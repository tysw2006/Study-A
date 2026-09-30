package com.studycafe.controller;
import com.studycafe.domain.*;
import com.studycafe.dto.*;
import com.studycafe.service.*;
import com.studycafe.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    private final AuthService authService;
    private final ReservationService reservationService;
    private final SeatRepository seatRepository;
    private final StudyRoomRepository roomRepository;

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

    @PostMapping("/auth/withdraw")
    public ResponseEntity<?> withdraw(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) return ResponseEntity.status(401).build();
        authService.withdraw(user.getId());
        session.invalidate();
        return ResponseEntity.ok("Withdrawn");
    }
    @GetMapping("/auth/me")
    public ResponseEntity<?> me(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(user);
    }
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
    @PostMapping("/reservations/seat")
    public ResponseEntity<?> reserveSeat(@RequestBody ReservationRequest req, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(reservationService.reserveSeat(user.getId(), req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/reservations/room")
    public ResponseEntity<?> reserveRoom(@RequestBody ReservationRequest req, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(reservationService.reserveRoom(user.getId(), req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    @GetMapping("/admin/reservations")
    public ResponseEntity<?> getAllReservations(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null || !user.getRole().name().equals("ADMIN")) return ResponseEntity.status(403).build();
        
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (Reservation r : reservationService.getAllReservations()) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", r.getId());
            map.put("userName", r.getUser().getName());
            map.put("userEmail", r.getUser().getEmail());
            map.put("reservationType", r.getReservationType());
            map.put("target", r.getReservationType().name().equals("SEAT") ? r.getSeat().getSeatNumber() : r.getStudyRoom().getRoomName());
            map.put("status", r.getStatus());
            map.put("startTime", r.getStartTime());
            map.put("endTime", r.getEndTime());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }
}
