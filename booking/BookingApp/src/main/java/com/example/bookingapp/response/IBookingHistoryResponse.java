package com.example.bookingapp.response;

import java.sql.Timestamp;

public interface IBookingHistoryResponse {
    String getRoomName();

    String getTitle();

    Timestamp getStartTime();

    Timestamp getEndTime();

    Timestamp getReservationTime();

    Integer getStatusMeeting();

    Long getMeetingId();

}
