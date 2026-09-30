package com.studycafe.dto;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ReservationRequest {
    private Long seatId;
    private Long roomId;
    private Integer hours;
    private Integer totalPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
