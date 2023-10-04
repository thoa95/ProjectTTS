package com.example.bookingapp.service;

import com.example.bookingapp.response.SeatRegistrationStatisticsResponse;

import java.time.LocalDate;

public interface SeatRegistrationService {
    void cancleSeatRegistration(Long id, Long personId);
    SeatRegistrationStatisticsResponse statisticSeatRegistration(Long roomId, LocalDate date, Integer eachMinute);

}
