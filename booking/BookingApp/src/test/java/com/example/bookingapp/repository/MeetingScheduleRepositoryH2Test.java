package com.example.bookingapp.repository;

import com.example.bookingapp.response.IBookingHistoryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class MeetingScheduleRepositoryH2Test {
    @Autowired
    MeetingScheduleRepository meetingScheduleRepository;

    @Test
    @Sql("/dataBookingHistory.sql")
    void test_findMeetingByUser() {
        Long personId = 1L;
        Long meetingId = 100L;
        IBookingHistoryResponse actual = meetingScheduleRepository.findMeetingByUser(personId, meetingId);
        assertEquals("Meeting 1", actual.getTitle());
        assertEquals(2, actual.getStatusMeeting());
    }
}