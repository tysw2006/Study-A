package com.studycafe.service;
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
        LocalDateTime now = LocalDateTime.now();
        
        // 중복 예약 및 1인 1자리 검증
        List<Reservation> active = reservationRepository.findByStatusAndEndTimeAfter(ReservationStatus.ACTIVE, now);
        boolean isUsed = active.stream().anyMatch(r -> r.getReservationType() == ReservationType.SEAT && r.getSeat().getId().equals(req.getSeatId()));
        if (isUsed) {
            throw new RuntimeException("이미 누군가 이용 중인 좌석입니다.");
        }
        boolean hasActiveReservation = active.stream().anyMatch(r -> r.getUser().getId().equals(userId));
        if (hasActiveReservation) {
            throw new RuntimeException("이미 예약하신 내역이 존재합니다. (1인 1자리 규칙)");
        }
        
        Reservation res = Reservation.builder()
                .user(user)
                .seat(seat)
                .startTime(now)
                .endTime(now.plusHours(req.getHours()))
                .status(ReservationStatus.ACTIVE)
                .reservationType(ReservationType.SEAT)
                .totalPrice(req.getTotalPrice() != null ? req.getTotalPrice() : 0)
                .build();
        return reservationRepository.save(res);
    }
    
    @Transactional
    public Reservation reserveRoom(Long userId, ReservationRequest req) {
        User user = userRepository.findById(userId).orElseThrow();
        StudyRoom room = roomRepository.findById(req.getRoomId()).orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        // 중복 예약 및 1인 1자리 검증
        List<Reservation> active = reservationRepository.findByStatusAndEndTimeAfter(ReservationStatus.ACTIVE, now);
        boolean isUsed = active.stream().anyMatch(r -> r.getReservationType() == ReservationType.ROOM && r.getStudyRoom().getId().equals(req.getRoomId()));
        if (isUsed) {
            throw new RuntimeException("이미 누군가 이용 중인 스터디 룸입니다.");
        }
        boolean hasActiveReservation = active.stream().anyMatch(r -> r.getUser().getId().equals(userId));
        if (hasActiveReservation) {
            throw new RuntimeException("이미 예약하신 내역이 존재합니다. (1인 1자리 규칙)");
        }

        Reservation res = Reservation.builder()
                .user(user)
                .studyRoom(room)
                .startTime(now)
                .endTime(now.plusHours(req.getHours()))
                .status(ReservationStatus.ACTIVE)
                .reservationType(ReservationType.ROOM)
                .totalPrice(req.getTotalPrice() != null ? req.getTotalPrice() : 0)
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
        return reservationRepository.findByStatusAndEndTimeAfter(ReservationStatus.ACTIVE, LocalDateTime.now());
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }
}
