package com.example.bookingapp.response;

import java.time.LocalTime;

public interface iSeatRegistrationStatisticsInterval {
    Integer getTotalSeatRegistrations();

    LocalTime getStartTime();

    LocalTime getEndTime();
}