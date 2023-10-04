package com.example.bookingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatRegistrationDTO {
    private Long id;
    private Long roomId;
    private Long personId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime seatRegistrationTime;
    private Integer seatRegistrationStatus;
}
