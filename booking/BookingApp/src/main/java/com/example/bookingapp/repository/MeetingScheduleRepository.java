package com.example.bookingapp.repository;

import com.example.bookingapp.model.MeetingSchedule;
import com.example.bookingapp.model.Room;
import com.example.bookingapp.response.IBookingHistoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingScheduleRepository extends JpaRepository<MeetingSchedule, Long> {
    @Query(value = "SELECT * FROM meeting_schedule ms " +
            "WHERE ms.room_id = ?1 " +
            "AND ms.status_meeting = ?2 " +
            "AND DATE(ms.start_time) = CURRENT_DATE " +
            "ORDER BY ms.start_time ASC ",
            nativeQuery = true)
    List<MeetingSchedule> bookedList(Long id, Integer statusMeeting);

    @Query("SELECT m FROM MeetingSchedule m " +
            "WHERE m.room = :room " +
            "AND ((m.startTime BETWEEN :startTime AND :endTime) " +
            "OR (m.endTime BETWEEN :startTime AND :endTime)) " +
            "AND m.id <> :meetingId")
    List<MeetingSchedule> findConflictingMeetingsInRoom(
            @Param("room") Room room,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("meetingId") Long meetingId
    );
    @Query(value = "SELECT " +
            "r.room_name AS roomName," +
            "ms.meeting_id AS meetingId," +
            "ms.title AS title," +
            "ms.start_time AS startTime," +
            "ms.end_time AS endTime, " +
            "ms.reservation_time AS reservationTime," +
            "    (CASE" +
            "    WHEN ms.status_meeting = 0 THEN 0" +
            "    WHEN current_timestamp < ms.start_time  THEN 1" +
            "    WHEN current_timestamp > ms.end_time THEN 3" +
            "    ELSE 2" +
            "    END) AS statusMeeting " +
            "FROM meeting_schedule ms " +
            "INNER JOIN room r ON ms.room_id = r.room_id " +
            "WHERE r.deleted_at IS NULl " +
            "AND  ms.person_id = ?1 " +
            "AND ms.meeting_id = ?2", nativeQuery = true)
    IBookingHistoryResponse findMeetingByUser(Long personId, Long meetingId);
}