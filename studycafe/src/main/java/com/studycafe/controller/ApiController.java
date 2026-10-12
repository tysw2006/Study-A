package com.studycafe.controller;

import com.studycafe.domain.*;
import com.studycafe.dto.*;
import com.studycafe.service.*;
import com.studycafe.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final AuthService authService;
    private final ReservationService reservationService;
    private final SeatRepository seatRepository;
    private final StudyRoomRepository roomRepository;
    private final EmailService emailService; // [추가] 이메일 인증 서비스 주입

    // ==========================================
    // [인증 관련 API]
    // ==========================================

    // 1. 이메일 인증번호 발송 API
    @PostMapping("/auth/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("이메일을 입력해주세요.");
        }

        try {
            emailService.sendVerificationCode(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // 2. 이메일 인증번호 검증 API
    @PostMapping("/auth/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        try {
            emailService.verifyCode(email, code);
            return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. 회원가입 API (이메일 인증 여부 검증 포함)
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        // 이메일 인증 완료 여부 확인
        if (!emailService.isEmailVerified(req.getEmail())) {
            return ResponseEntity.badRequest().body("이메일 인증을 먼저 완료해주세요.");
        }

        try {
            Object result = authService.register(req);
            // 가입 완료 시 인증 정보 메모리 정리
            emailService.clearVerification(req.getEmail());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    // ==========================================
    // [좌석 및 예약 관련 API]
    // ==========================================

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

    // ==========================================
    // [관리자 API]
    // ==========================================

    @GetMapping("/admin/reservations")
    public ResponseEntity<?> getAllReservations(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null || !user.getRole().name().equals("ADMIN")) return ResponseEntity.status(403).build();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Reservation r : reservationService.getAllReservations()) {
            Map<String, Object> map = new HashMap<>();
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