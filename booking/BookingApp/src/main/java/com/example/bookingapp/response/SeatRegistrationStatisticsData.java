package com.example.bookingapp.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatRegistrationStatisticsData {
    private Long roomId;
    private LocalDate date;
    private List<iSeatRegistrationStatisticsInterval> timeIntervals;

}