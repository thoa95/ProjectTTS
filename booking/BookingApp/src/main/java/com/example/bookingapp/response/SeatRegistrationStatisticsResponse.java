package com.example.bookingapp.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatRegistrationStatisticsResponse {
    private SeatRegistrationStatisticsData data;
    private String errorMessage;
}
