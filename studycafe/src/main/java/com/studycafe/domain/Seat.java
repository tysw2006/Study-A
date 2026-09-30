package com.studycafe.domain;
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
