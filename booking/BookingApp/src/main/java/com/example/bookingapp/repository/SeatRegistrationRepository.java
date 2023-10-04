package com.example.bookingapp.repository;

import com.example.bookingapp.model.SeatRegistration;
import com.example.bookingapp.response.iSeatRegistrationStatisticsInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SeatRegistrationRepository extends JpaRepository<SeatRegistration, Long> {
    @Query(value =
            "SELECT result.block_start AS startTime," +
                    "       result.block_end AS endTime," +
                    "       SUM(COALESCE(result.count, 0)) AS totalSeatRegistrations\n" +
                    "FROM (" +
                    "    WITH block_times (block_start, block_end) AS" +
                    "        (SELECT" +
                    "             generate_series(\n" +
                    "               CAST(:date AS TIMESTAMP) + CAST('00:00:00' AS TIME)," +
                    "               CAST(:date AS TIMESTAMP) + INTERVAL '1 day' - INTERVAL '1 minute' * :eachMinute," +
                    "               CAST(:eachMinute || ' minutes' AS INTERVAL MINUTE TO SECOND)" +
                    "               ) AS block_start,\n" +
                    "            generate_series(\n" +
                    "                CAST(:date AS TIMESTAMP) + CAST(:eachMinute || ' minutes' AS INTERVAL MINUTE TO SECOND)," +
                    "                CAST(:date AS TIMESTAMP) + INTERVAL '1 day'," +
                    "                CAST(:eachMinute || ' minutes' AS INTERVAL MINUTE TO SECOND)" +
                    "               ) AS block_end" +
                    "       )" +
                    "    SELECT bt.block_start, bt.block_end, count(bt.block_start)" +
                    "    FROM block_times bt" +
                    "    CROSS JOIN seat_registration sr" +
                    "    INNER JOIN room r ON sr.room_id = r.room_id" +
                    "    WHERE r.deleted_at IS NULL" +
                    "    AND r.room_id = :roomId" +
                    "    AND sr.seat_registration_status <> 0" +
                    "    AND (sr.start_time BETWEEN bt.block_start AND bt.block_end" +
                    "        OR sr.end_time BETWEEN bt.block_start AND bt.block_end" +
                    "        OR bt.block_start BETWEEN sr.start_time AND sr.end_time" +
                    "        OR bt.block_end BETWEEN sr.start_time AND sr.end_time)" +
                    "    GROUP BY bt.block_start, bt.block_end" +
                    "    UNION" +
                    "    SELECT block_start, block_end, 0 count" +
                    "    FROM block_times" +
                    "    ORDER BY block_start, block_end" +
                    ") AS result " +
                    "GROUP BY result.block_start, result.block_end " +
                    "ORDER BY result.block_start, result.block_end", nativeQuery = true)
    List<iSeatRegistrationStatisticsInterval> calculateSeatRegistration(
            @Param("roomId") Long roomId,
            @Param("date") LocalDate date,
            @Param("eachMinute") Integer eachMinute
    );
}
