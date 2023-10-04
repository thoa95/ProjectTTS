package com.example.bookingapp.service;

import com.example.bookingapp.request.MeetingScheduleRequest;
import com.example.bookingapp.request.MeetingScheduleRequestUpdate;
import com.example.bookingapp.response.MeetingResponseUpdate;
import com.example.bookingapp.response.MeetingScheduleResponse;

public interface MeetingScheduleService {
    MeetingScheduleResponse bookMeetingSchedule(MeetingScheduleRequest meetingRequest);
    MeetingResponseUpdate updateMeeting(Long id, Long personId, MeetingScheduleRequestUpdate req);
    void cancelMeeting(Long meetingId, Long personId);

}
