package com.studycafe.domain;
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
