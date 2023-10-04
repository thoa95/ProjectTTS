package com.example.bookingapp.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryResponseImpl implements IBookingHistoryResponse {
    private String roomName;
    private String title;
    private Timestamp startTime;
    private Timestamp endTime;
    private Timestamp reservationTime;
    private Integer statusMeeting;
    private Long meetingId;

    @Override
    public String getRoomName() {
        return roomName;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Timestamp getStartTime() {
        return startTime;
    }

    @Override
    public Timestamp getEndTime() {
        return endTime;
    }

    @Override
    public Timestamp getReservationTime() {
        return reservationTime;
    }

    @Override
    public Integer getStatusMeeting() {
        return statusMeeting;
    }

    @Override
    public Long getMeetingId() {
        return meetingId;
    }

}
