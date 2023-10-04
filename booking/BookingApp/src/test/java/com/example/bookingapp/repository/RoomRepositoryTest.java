package com.example.bookingapp.repository;
import com.example.bookingapp.constant.InputRequired;
import com.example.bookingapp.constant.StatusSeatRegistration;
import com.example.bookingapp.model.Room;
import com.example.bookingapp.response.IBookingHistoryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;


@DataJpaTest
class RoomRepositoryTest {
    @Autowired
    RoomRepository roomRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @ParameterizedTest
    @ValueSource(strings = {"Room 1","  Room 1  "})
    void test_findByRoomName(){
        Room room= Room.builder()
                .roomName("Room 1")
                .capacity(10).build();
        roomRepository.save(room);
        String roomName= "  Room 1  ";
        boolean actual =roomRepository.existsByRoomName(roomName.trim());
        assertTrue(actual);
    }
    @Test
    void test_findByRoomName_notFound(){
        String roomName= "Room 123";
        boolean actual =roomRepository.existsByRoomName(roomName.trim());
        assertFalse(actual);
    }

    @Test
    void test_getRoomByIdAvailable_withAvailableRoom() {
        Room room = new Room();
        room.setRoomName("Room 1");
        room.setDeletedAt(null);
        roomRepository.save(room);
        Room actual = roomRepository.getRoomByIdAvailable(room.getId());
        assertNotNull(actual);
        assertEquals(room.getRoomName(), actual.getRoomName());

    }

    @Test
    void test_getRoomByIdAvailable_withRoomDeleted() {
        Room room = new Room();
        room.setRoomName("Room 1");
        room.setDeletedAt(new Date());
        roomRepository.save(room);
        Room actual = roomRepository.getRoomByIdAvailable(room.getId());
        assertNull(actual);
    }

    @Test
    @Sql("/dataBookingHistory.sql")
    void test_findBookingHistoryFromData_haventParams() {
        Long personId = 1L;
        Boolean isSort = true;
        List<IBookingHistoryResponse> actual = roomRepository.findBookingHistoryFromData(personId, null, null, null, isSort);
        assertEquals(3, actual.size());
        assertEquals(300, actual.get(0).getMeetingId());
        assertEquals(200, actual.get(1).getMeetingId());
        assertEquals(100, actual.get(2).getMeetingId());
    }

    @Test
    void test_findBookingHistoryFromData_withStatusMeeting_fail() {
        Long personId = 1L;
        Integer statusMeeting = 5;
        List<IBookingHistoryResponse> actual = roomRepository.findBookingHistoryFromData(personId, null, null, statusMeeting, null);
        assertTrue(actual.isEmpty());
    }

    @Test
    @Sql("/dataBookingHistory.sql")
    void test_findBookingHistoryFromData_haveParams() {
        Long personId = 1L;
        String title = "E";
        String roomName = "A";
        Integer statusMeeting = 2;
        List<IBookingHistoryResponse> actual = roomRepository.findBookingHistoryFromData(personId, title, roomName, statusMeeting,null);
        assertEquals(1, actual.size());
        assertEquals("Meeting 1", actual.get(0).getTitle());
        assertEquals("Room A", actual.get(0).getRoomName());
        assertEquals(statusMeeting, actual.get(0).getStatusMeeting());
    }

    @Test
    @Sql("/dataSeatBooking.sql")
    void test_getAvailableSeat(){
        Long roomId = 1L;
        int seatsForOneRegistration = InputRequired.SEATS_FOR_ONE_REGISTRATION;
        int actual = roomRepository.getAvailableSeat(roomId, seatsForOneRegistration);
        assertEquals(1, actual);
    }

    @Test
    @Sql("/dataSeatBooking.sql")
    void test_getAvailableSeat_withRoomDeleted(){
        Long roomId = 3L;
        int seatsForOneRegistration = InputRequired.SEATS_FOR_ONE_REGISTRATION;
        int actual = roomRepository.getAvailableSeat(roomId, seatsForOneRegistration);
        assertEquals(-1, actual);
    }

    @Test
    @Sql("/dataSeatBooking.sql")
    void test_getAvailableSeat_withRoomDeleted_andSeatRegistration_Canceled(){
        Long roomId = 4L;
        int seatsForOneRegistration = InputRequired.SEATS_FOR_ONE_REGISTRATION;
        int actual = roomRepository.getAvailableSeat(roomId, seatsForOneRegistration);
        assertEquals(0, actual);
    }

    @Test
    @Sql("/dataSeatBooking.sql")
    void test_findRoomIdByOverlapTime(){
        Long roomId = 1L;
        Long personId = 1L;
        int statusSeatRegistration = StatusSeatRegistration.REGISTERED;
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        LocalDateTime endTime = LocalDateTime.now().plusHours(3);
        Long actual = roomRepository.findRoomIdByOverlapTime(roomId, personId, statusSeatRegistration, startTime, endTime);
        assertNotNull(actual);
        assertEquals(roomId, actual);
    }
}