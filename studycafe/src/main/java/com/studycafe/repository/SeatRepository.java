package com.studycafe.repository;
import com.studycafe.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SeatRepository extends JpaRepository<Seat, Long> {}
