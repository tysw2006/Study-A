package com.studycafe.repository;
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
    
    List<Reservation> findByStatusAndEndTimeAfter(ReservationStatus status, LocalDateTime now);
    void deleteByUser(com.studycafe.domain.User user);
}
