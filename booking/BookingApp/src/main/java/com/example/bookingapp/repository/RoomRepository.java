package com.example.bookingapp.repository;

import com.example.bookingapp.model.Room;
import com.example.bookingapp.response.IBookingHistoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room,Long> {
    boolean existsByRoomName(String roomName);
    @Query("SELECT r FROM Room r WHERE r.id = ?1 and r.deletedAt IS NULL")
    Room getRoomByIdAvailable(Long id);

    @Query(value = "SELECT * FROM " +
            "(SELECT r.room_name AS roomName, " +
            "ms.meeting_id AS meetingId, " +
            "ms.title AS title, " +
            "ms.start_time AS startTime," +
            "ms.end_time AS endTime, " +
            "ms.reservation_time AS reservationTime," +
            "    (CASE" +
            "        WHEN ms.status_meeting = 0 THEN 0 " +
            "        WHEN current_timestamp < ms.start_time THEN 1" +
            "        WHEN current_timestamp > ms.end_time THEN 3" +
            "       ELSE 2" +
            "    END) AS statusMeeting " +
            "FROM meeting_schedule ms " +
            "INNER JOIN room r ON ms.room_id = r.room_id " +
            "WHERE ms.person_id = ?1 " +
            "AND r.deleted_at IS NULL " +
            "AND (LOWER(ms.title) LIKE LOWER(CONCAT('%', ?2, '%')) OR ?2 IS NULL) " +
            "AND (LOWER(r.room_name) LIKE LOWER(CONCAT('%', ?3, '%')) OR ?3 IS NULL) " +
            ") AS ms2 " +
            "WHERE (ms2.statusMeeting = ?4 OR ?4 IS NULL) " +
            "ORDER BY " +
            "    CASE" +
            "        WHEN ?5 = true THEN ms2.startTime" +
            "        ELSE ms2.reservationTime" +
            "    END", nativeQuery = true)
    List<IBookingHistoryResponse> findBookingHistoryFromData(Long personId, String title, String roomName, Integer statusMeeting, Boolean isSort);

    @Query(value = "SELECT (" +
            "    COALESCE(" +
            "        (SELECT r.capacity" +
            "        FROM room r" +
            "        WHERE r.deleted_at IS NULL" +
            "        AND r.room_id = ?1), 0)" +
            "        -" +
            "    COALESCE(" +
            "        (SELECT SUM(" +
            "        CASE" +
            "        WHEN sr.seat_registration_status = 0 THEN 0" +
            "        WHEN CURRENT_TIMESTAMP < sr.start_time THEN ?2" +
            "        WHEN CURRENT_TIMESTAMP > sr.end_time THEN 0" +
            "        ELSE ?2" +
            "        END)" +
            "        FROM seat_registration sr" +
            "        WHERE sr.room_id = ?1), 0)" +
            ") AS result", nativeQuery = true)
    int getAvailableSeat(Long roomId, int seatsForOneRegistration);

    @Query(value = "SELECT sr.room_id " +
            "FROM seat_registration sr " +
            "INNER JOIN room r " +
            "ON r.room_id = sr.room_id " +
            "WHERE r.deleted_at IS NULL " +
            "AND sr.room_id = ?1 " +
            "AND sr.person_id= ?2 " +
            "AND sr.seat_registration_status = ?3 " +
            "AND ((?4 BETWEEN sr.start_time AND sr.end_time) " +
            "    OR (?5 BETWEEN sr.start_time AND sr.end_time) " +
            "    OR (sr.start_time BETWEEN ?4 AND ?5) " +
            "    OR (sr.end_time BETWEEN ?4 AND ?5)) ", nativeQuery = true)
    Long findRoomIdByOverlapTime(Long roomId, Long personId, int statusSeatRegistration, LocalDateTime startTime, LocalDateTime endTime);

}
