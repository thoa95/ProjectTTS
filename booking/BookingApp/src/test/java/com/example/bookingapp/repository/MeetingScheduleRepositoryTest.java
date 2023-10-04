package com.example.bookingapp.repository;

import com.example.bookingapp.constant.StatusMeetingSchedule;
import com.example.bookingapp.model.MeetingSchedule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MeetingScheduleRepositoryTest {
    @Autowired
    MeetingScheduleRepository meetingScheduleRepository;

    @Test
    @Sql(value = "/createMeetingSchedule.sql")
    void test_bookedList_withEmptyMeetings() {
        Long roomId = 1L;
        Integer statusMeeting = StatusMeetingSchedule.SCHEDULED;
        List<MeetingSchedule> actual = meetingScheduleRepository.bookedList(roomId, statusMeeting);
        assertTrue(actual.size() >= 1);
    }
}