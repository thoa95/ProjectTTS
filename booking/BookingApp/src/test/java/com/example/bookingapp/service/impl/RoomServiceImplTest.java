package com.example.bookingapp.service.impl;

import com.example.bookingapp.constant.InputRequired;
import com.example.bookingapp.constant.StatusMeetingSchedule;
import com.example.bookingapp.constant.StatusRoom;
import com.example.bookingapp.constant.StatusSeatRegistration;
import com.example.bookingapp.dto.MeetingScheduleDTO;
import com.example.bookingapp.dto.SeatRegistrationDTO;
import com.example.bookingapp.exception.BadRequestException;
import com.example.bookingapp.exception.ResourceNotFoundException;
import com.example.bookingapp.exception.RoomInvalidFormatParamException;
import com.example.bookingapp.exception.RoomNotFoundException;
import com.example.bookingapp.model.MeetingSchedule;
import com.example.bookingapp.model.Person;
import com.example.bookingapp.model.Room;
import com.example.bookingapp.model.SeatRegistration;
import com.example.bookingapp.repository.MeetingScheduleRepository;
import com.example.bookingapp.repository.PersonRepository;
import com.example.bookingapp.repository.RoomRepository;
import com.example.bookingapp.dto.RoomDTO;
import com.example.bookingapp.repository.SeatRegistrationRepository;
import com.example.bookingapp.request.RoomAddRequest;
import com.example.bookingapp.request.SeatRegistrationRequest;
import com.example.bookingapp.response.BookingHistoryResponseImpl;
import com.example.bookingapp.response.IBookingHistoryResponse;
import com.example.bookingapp.response.RoomDetailResponse;
import com.example.bookingapp.response.SearchRoomResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {
    @MockBean
    RoomRepository roomRepository;
    @MockBean
    MeetingScheduleRepository meetingScheduleRepository;
    @MockBean
    PersonRepository personRepository;
    @MockBean
    SeatRegistrationRepository seatRegistrationRepository;
    @InjectMocks
    RoomServiceImpl roomService;

    @Test
    public void testSoftDeleteRoom_Success() {
        Long roomId = 1L;

        Room room = new Room();
        room.setId(roomId);
        room.setDeletedAt(null);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        assertDoesNotThrow(() -> roomService.deleteRoom(roomId));

        assertNotNull(room.getDeletedAt());
        verify(roomRepository, times(1)).save(room);
    }

    @Test
    public void testSoftDeleteRoom_AlreadyDeleted() {
        Long roomId = 20L;

        Room room = new Room();
        room.setId(roomId);
        room.setDeletedAt(new Date());
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        Exception exception = assertThrows(BadRequestException.class, () -> roomService.deleteRoom(roomId));
        assertEquals("Room with ID " + roomId + " has already been deleted.", exception.getMessage());
        assertNotNull(room.getDeletedAt());
        verify(roomRepository, never()).save(room);
    }

    @Test
    public void testSoftDeleteRoom_NotFound() {
        Long roomId = 99L;

        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> roomService.deleteRoom(roomId));
        assertEquals("Room with ID " + roomId + " does not exist.", exception.getMessage());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void test_checkInsertRoom_withNullRoomDTO() {
        RoomDTO actual = roomService.checkInsertRoom(null);
        Assertions.assertNull(actual);
    }

    @Test
    public void test_checkInsertRoom_withCapacity() {
        RoomAddRequest roomRequest = new RoomAddRequest();
        roomRequest.setRoomName("Invalid Room");
        roomRequest.setCapacity(-5);
        RoomDTO actual = roomService.checkInsertRoom(roomRequest);
        Assertions.assertNull(actual);
    }

    @Test
    public void test_checkInsertRoom_withRoomName() {
        RoomAddRequest roomRequest = new RoomAddRequest();
        roomRequest.setRoomName("  ");
        roomRequest.setCapacity(5);
        RoomDTO actual = roomService.checkInsertRoom(roomRequest);
        Assertions.assertNull(actual);
    }

    @Test
    void testUpdateRoom_ValidRoom_ReturnsUpdatedRoomDTO() {
        Long roomId = 1L;
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomId(roomId);
        roomDTO.setRoomName("Updated Room");
        roomDTO.setCapacity(5);
        roomDTO.setStatusRoom(StatusRoom.AVAILABLE);

        Room existingRoom = new Room();
        existingRoom.setId(roomId);
        existingRoom.setRoomName("Existing Room");
        existingRoom.setCapacity(10);
        existingRoom.setStatusRoom(StatusRoom.AVAILABLE);
        existingRoom.setDeletedAt(null);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.save(existingRoom)).thenReturn(existingRoom);

        RoomDTO updatedRoomDTO = roomService.updateRoom(roomDTO, roomId);

        assertNotNull(updatedRoomDTO);
        assertEquals(roomId, updatedRoomDTO.getRoomId());
        assertEquals("Updated Room", updatedRoomDTO.getRoomName());
        assertEquals(5, updatedRoomDTO.getCapacity());
        assertEquals(StatusRoom.AVAILABLE, updatedRoomDTO.getStatusRoom());
        assertNotNull(existingRoom.getUpdatedAt());
        verify(roomRepository, times(1)).findById(roomId);
        verify(roomRepository, times(1)).save(existingRoom);
    }

    @Test
    public void testUpdateRoom_SoftDeletedRoom_ExceptionThrown() {
        Long roomId = 1L;
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomName("Updated Room");
        roomDTO.setCapacity(5);
        roomDTO.setStatusRoom(StatusRoom.AVAILABLE);

        Room existingRoom = new Room();
        existingRoom.setId(roomId);
        existingRoom.setRoomName("Existing Room");
        existingRoom.setCapacity(10);
        existingRoom.setStatusRoom(StatusRoom.AVAILABLE);
        existingRoom.setDeletedAt(new Date());

        Mockito.when(roomRepository.findById(roomId)).thenReturn(Optional.of(existingRoom));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> roomService.updateRoom(roomDTO, roomId));

        Mockito.verify(roomRepository, Mockito.times(1)).findById(roomId);
        Mockito.verify(roomRepository, Mockito.never()).save(Mockito.any(Room.class));
    }

    @Test
    void test_getRoomDetail_withId_NotFound() {
        Long roomId = 1L;
        Mockito.when(roomRepository.getRoomByIdAvailable(roomId)).thenReturn(null);
        RoomDetailResponse actual = roomService.getRoomDetail(roomId);
        assertNull(actual);
    }

    @Test
    void test_getRoomDetail_withListSchedule() {
        Long roomId = 1L;
        Room room = new Room();
        room.setRoomName("Room 1");
        room.setCapacity(10);
        when(roomRepository.getRoomByIdAvailable(roomId)).thenReturn(room);

        List<MeetingSchedule> meetingScheduleList = new ArrayList<>();
        MeetingSchedule meeting1 = new MeetingSchedule();
        meeting1.setId(1L);
        meeting1.setPerson(new Person());
        meeting1.setTitle("Meeting 1");
        meetingScheduleList.add(meeting1);

        when(meetingScheduleRepository.bookedList(roomId, StatusMeetingSchedule.SCHEDULED)).thenReturn(meetingScheduleList);

        RoomDetailResponse actual = roomService.getRoomDetail(roomId);

        assertNotNull(actual);
        assertEquals(room.getRoomName(), actual.getRoomName());
        assertEquals(room.getCapacity(), actual.getCapacity());
        assertNotNull(actual.getBookedMeeting());
        assertEquals(1, actual.getBookedMeeting().size());
        assertEquals(meeting1.getTitle(), actual.getBookedMeeting().get(0).getTitle());
    }

    @Test
    void testListRoom_InvalidParameters_NullFromDateAndToDate() {
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        Integer minCapacity = 5;
        Integer maxCapacity = 10;
        String roomName = "Meeting Room";
        assertThrows(RoomInvalidFormatParamException.class, () -> roomService.listRoom(fromDate, toDate, minCapacity, maxCapacity, roomName));
    }

    @Test
    void testListRoom_InvalidParameters_MinGreaterThanMaxCapacity() {
        LocalDateTime fromDate = LocalDateTime.now().plusHours(1);
        LocalDateTime toDate = LocalDateTime.now().plusHours(2);
        Integer minCapacity = 10;
        Integer maxCapacity = 5;
        String roomName = "Meeting Room";
        when(roomRepository.save(any(Room.class))).thenReturn(new Room());
        Exception exception = assertThrows(RoomInvalidFormatParamException.class, () -> roomService.listRoom(fromDate, toDate, minCapacity, maxCapacity, roomName));
        assertEquals("minCapacity cannot be greater than or equal to maxCapacity.", exception.getMessage());
    }

    @Test
    void testListRoom_InvalidParameters_MinCapacityLessThanZero() {
        LocalDateTime fromDate = LocalDateTime.now().plusHours(1);
        LocalDateTime toDate = LocalDateTime.now().plusHours(2);
        Integer minCapacity = Integer.MIN_VALUE;
        Integer maxCapacity = 5;
        String roomName = "Meeting Room";
        when(roomRepository.save(any(Room.class))).thenReturn(new Room());
        Exception exception = assertThrows(RoomInvalidFormatParamException.class, () -> roomService.listRoom(fromDate, toDate, minCapacity, maxCapacity, roomName));
        assertEquals("minCapacity cannot be less than zero.", exception.getMessage());
    }

    @Test
    void testListRoom_InvalidParameters_MaxCapacityExceedsLimit() {
        LocalDateTime fromDate = LocalDateTime.now().plusHours(1);
        LocalDateTime toDate = LocalDateTime.now().plusHours(2);
        Integer minCapacity = 1;
        Integer maxCapacity = Integer.MAX_VALUE;
        String roomName = "Meeting Room";
        when(roomRepository.save(any(Room.class))).thenReturn(new Room());
        Exception exception = assertThrows(RoomInvalidFormatParamException.class, () -> roomService.listRoom(fromDate, toDate, minCapacity, maxCapacity, roomName));
        assertEquals("maxCapacity cannot exceed " + Integer.MAX_VALUE + ".", exception.getMessage());
    }

    @Test
    void testListRoom_InvalidParameters_InvalidRoomName() {
        LocalDateTime fromDate = LocalDateTime.now().plusHours(1);
        LocalDateTime toDate = LocalDateTime.now().plusHours(2);
        Integer minCapacity = 1;
        Integer maxCapacity = 10;
        String roomName = generateRandomString(300);
        when(roomRepository.save(any(Room.class))).thenReturn(new Room());
        Exception exception = assertThrows(RoomInvalidFormatParamException.class, () -> roomService.listRoom(fromDate, toDate, minCapacity, maxCapacity, roomName));
        assertEquals("roomName must be a non-empty string with length less than 255.", exception.getMessage());
    }

    private String generateRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than zero.");
        }
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomString = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            randomString.append(randomChar);
        }
        return randomString.toString();
    }

    @Test
    void testCheckOverlap_NoOverlap() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LocalDateTime startFirst = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime endFirst = LocalDateTime.of(2023, 1, 1, 11, 0);
        LocalDateTime startSecond = LocalDateTime.of(2023, 1, 1, 12, 0);
        LocalDateTime endSecond = LocalDateTime.of(2023, 1, 1, 13, 0);
        Method checkOverlapMethod = getCheckOverlapMethod();
        boolean overlap = (boolean) checkOverlapMethod.invoke(roomService, startFirst, endFirst, startSecond, endSecond);
        assertFalse(overlap, "There should be no overlap.");
    }

    @Test
    void testCheckOverlap_PartialOverlap() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LocalDateTime startFirst = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime endFirst = LocalDateTime.of(2023, 1, 1, 11, 0);
        LocalDateTime startSecond = LocalDateTime.of(2023, 1, 1, 10, 30);
        LocalDateTime endSecond = LocalDateTime.of(2023, 1, 1, 11, 30);
        Method checkOverlapMethod = getCheckOverlapMethod();
        boolean overlap = (boolean) checkOverlapMethod.invoke(roomService, startFirst, endFirst, startSecond, endSecond);
        assertTrue(overlap, "There should be a partial overlap.");
    }

    @Test
    void testCheckOverlap_FullOverlap() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LocalDateTime startFirst = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime endFirst = LocalDateTime.of(2023, 1, 1, 12, 0);
        LocalDateTime startSecond = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime endSecond = LocalDateTime.of(2023, 1, 1, 12, 0);
        Method checkOverlapMethod = getCheckOverlapMethod();
        boolean overlap = (boolean) checkOverlapMethod.invoke(roomService, startFirst, endFirst, startSecond, endSecond);
        assertTrue(overlap, "There should be a full overlap.");
    }

    private Method getCheckOverlapMethod() throws NoSuchMethodException {
        Method method = RoomServiceImpl.class.getDeclaredMethod("checkOverlap", LocalDateTime.class, LocalDateTime.class, LocalDateTime.class, LocalDateTime.class);
        method.setAccessible(true);
        return method;
    }

    private Method getIsMatchingScheduleMethod() throws NoSuchMethodException {
        Method method = RoomServiceImpl.class.getDeclaredMethod("isMatchingSchedule", MeetingSchedule.class, Long.class, Integer.class);
        method.setAccessible(true);
        return method;
    }

    @Test
    void testIsMatchingSchedule_MatchingSchedule() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MeetingSchedule schedule = new MeetingSchedule();
        Room room = new Room();
        room.setId(1L);
        schedule.setRoom(room);
        schedule.setStatusMeeting(1);
        when(meetingScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        Method isMatchingScheduleMethod = getIsMatchingScheduleMethod();
        boolean isMatching = (boolean) isMatchingScheduleMethod.invoke(roomService, schedule, 1L, 1);
        assertTrue(isMatching, "The schedule should match.");
    }

    private Method getMapToSearchRoomResponseMethod() throws NoSuchMethodException {
        Method method = RoomServiceImpl.class.getDeclaredMethod("mapToSearchRoomResponse", Room.class);
        method.setAccessible(true);
        return method;
    }

    @Test
    void testMapToSearchRoomResponse_WithoutMeetingSchedules() throws Exception {
        Room room = new Room();
        room.setId(1L);
        room.setRoomName("Meeting Room 1");
        room.setCapacity(10);
        room.setStatusRoom(StatusRoom.AVAILABLE);
        SearchRoomResponse response = (SearchRoomResponse) getMapToSearchRoomResponseMethod().invoke(roomService, room);
        assertEquals(1L, response.getRoomId());
        assertEquals("Meeting Room 1", response.getRoomName());
        assertEquals(10, response.getCapacity());
        assertEquals(StatusRoom.AVAILABLE, response.getStatusRoom());
        List<MeetingScheduleDTO> meetingSchedules = response.getMeetingSchedules();
        assertNotNull(meetingSchedules);
        assertTrue(meetingSchedules.isEmpty());
    }

    private Method getFilterRoomsByDateMethod() throws NoSuchMethodException {
        Method method = RoomServiceImpl.class.getDeclaredMethod("filterRoomsByDate", List.class, LocalDateTime.class, LocalDateTime.class);
        method.setAccessible(true);
        return method;
    }

    @Test
    void testFilterRoomsByDate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Room> rooms = createSampleRooms();
        LocalDateTime fromDate = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime toDate = LocalDateTime.of(2023, 1, 1, 12, 0);
        List<Room> filteredRooms = (List<Room>) getFilterRoomsByDateMethod().invoke(roomService, rooms, fromDate, toDate);
        assertEquals(4, filteredRooms.size());
    }

    private Method getFilterRoomsByNameMethod() throws NoSuchMethodException {
        Method method = RoomServiceImpl.class.getDeclaredMethod("filterRoomsByName", List.class, String.class);
        method.setAccessible(true);
        return method;
    }

    @Test
    void testFilterRoomsByName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Room> rooms = createSampleRooms();
        List<Room> filteredRooms = (List<Room>) getFilterRoomsByNameMethod().invoke(roomService, rooms, "meeting");
        assertEquals(2, filteredRooms.size());
    }

    private Method getFilterRoomsByCapacityMethod() throws NoSuchMethodException {
        Method method = RoomServiceImpl.class.getDeclaredMethod("filterRoomsByCapacity", List.class, Integer.class, Integer.class);
        method.setAccessible(true);
        return method;
    }

    @Test
    void testFilterRoomsByCapacity() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Room> rooms = createSampleRooms();
        List<Room> filteredRooms = (List<Room>) getFilterRoomsByCapacityMethod().invoke(roomService, rooms, 10, 20);
        assertEquals(4, filteredRooms.size());
    }

    private List<Room> createSampleRooms() {
        List<Room> rooms = new ArrayList<>();
        rooms.add(createSampleRoom("Meeting Room 1"));
        rooms.add(createSampleRoom("Conference Room 1"));
        rooms.add(createSampleRoom("Meeting Room 2"));
        rooms.add(createSampleRoom("Lounge Room"));
        // Add more rooms as needed
        return rooms;
    }

    private Room createSampleRoom(String roomName) {
        Room room = new Room();
        room.setRoomName(roomName);
        room.setCapacity(10);
        room.setStatusRoom(StatusRoom.AVAILABLE);
        return room;
    }

    @Test
    public void testListRoomWithNoMatchingRooms() {
        LocalDateTime fromDate = LocalDateTime.now();
        LocalDateTime toDate = fromDate.plusHours(2);
        Integer minCapacity = 10;
        Integer maxCapacity = 20;
        String roomName = "Meeting Room";
        when(roomRepository.findAll()).thenReturn(new ArrayList<>());
        assertThrows(RoomNotFoundException.class, () -> {
            roomService.listRoom(fromDate, toDate, minCapacity, maxCapacity, roomName);
        });
        Mockito.verify(roomRepository, Mockito.times(1)).findAll();
    }
    @Test
    void test_getBookingHistory_success() {
        Person person = new Person();
        person.setId(1L);
        List<IBookingHistoryResponse> responseList = new ArrayList<>();
        BookingHistoryResponseImpl response = new BookingHistoryResponseImpl();
        response.setTitle("Meeting 1");
        response.setRoomName("Room A");
        response.setStatusMeeting(2);
        responseList.add(response);
        when(personRepository.findById(person.getId())).thenReturn(Optional.of(person));
        when(roomRepository.findBookingHistoryFromData(person.getId(), null, null, null, null)).thenReturn(responseList);
        List<IBookingHistoryResponse> actual = roomService.getBookingHistory(person.getId(), null, null, null,null);
        assertFalse(actual.isEmpty());
        assertEquals(1,actual.size());
    }

    @Test
    void test_getBookingHistory_withPersonId_fail() {
        Long personId = -1L;
        when(personRepository.findById(personId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> roomService.getBookingHistory(personId, null, null, null,null));
    }

    @Test
    void test_getBookingHistory_withStatusMeeting_fail() {
        Long personId = 1L;
        Person person = new Person();
        Integer statusMeeting = 4;
        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        assertThrows(IllegalArgumentException.class,
                () -> roomService.getBookingHistory(personId, null, null, statusMeeting,null));
    }

    @Test
    void test_findMeetingByUserFromData_withMeetingId_NotExit(){
        Long personId = 1L;
        Long meetingId = -5L;
        when(meetingScheduleRepository.findById(meetingId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> roomService.findMeetingByUserFromData(personId, meetingId));
    }

    @Test
    void test_findMeetingByUserFromData_withPersonId_fail() {
        Long personId = -1L;
        Long meetingId = 5L;
        when(personRepository.findById(personId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> roomService.findMeetingByUserFromData(personId, meetingId));
    }

    @Test
    void test_findMeetingByUserFromData() {
        Long personId = 1L;
        Long meetingId = 5L;

        Person person = new Person();
        person.setId(personId);
        MeetingSchedule ms = new MeetingSchedule();
        ms.setId(meetingId);
        ms.setPerson(person);
        ms.setTitle("Meeting test");
        BookingHistoryResponseImpl response = new BookingHistoryResponseImpl();
        response.setTitle(ms.getTitle());

        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(meetingScheduleRepository.findById(meetingId)).thenReturn(Optional.of(ms));
        when(meetingScheduleRepository.findMeetingByUser(personId,meetingId)).thenReturn(response);
        IBookingHistoryResponse actual = roomService.findMeetingByUserFromData(personId,meetingId);

        assertNotNull( actual);
        assertEquals( ms.getTitle(),actual.getTitle());
        assertNull(actual.getRoomName());
        assertNull(actual.getStartTime());
        assertNull(actual.getEndTime());
        assertNull(actual.getStatusMeeting());
        assertNull(actual.getReservationTime());
    }

    @Test
    void test_resolverSeatRegistration_with_personIdFail() {
        SeatRegistrationRequest request = new SeatRegistrationRequest();
        request.setPersonId(1L);
        when(personRepository.findById(request.getPersonId())).thenReturn(Optional.empty());
        String message = assertThrows(IllegalArgumentException.class,
                () -> roomService.resolverSeatRegistration(request)).getMessage();
        assertEquals("User ID is incorrect.", message);
    }

    @Test
    void test_resolverSeatRegistration_with_roomIdDeleted() {
        SeatRegistrationRequest request = new SeatRegistrationRequest();
        request.setRoomId(1L);
        request.setPersonId(1L);
        Person person = new Person();
        person.setId(request.getPersonId());
        when(personRepository.findById(request.getPersonId())).thenReturn(Optional.of(person));
        when(roomRepository.getRoomByIdAvailable(request.getRoomId())).thenReturn(null);
        String message = assertThrows(IllegalArgumentException.class,
                () -> roomService.resolverSeatRegistration(request)).getMessage();
        assertEquals("Room ID is incorrect or Room has been deleted.", message);
    }

    @Test
    void test_resolverSeatRegistration_with_pastTime() {
        SeatRegistrationRequest request = new SeatRegistrationRequest();
        request.setRoomId(1L);
        request.setPersonId(1L);
        request.setStartTime(LocalDateTime.now().minusHours(2));
        request.setEndTime(LocalDateTime.now());
        Person person = new Person();
        person.setId(request.getPersonId());
        when(personRepository.findById(request.getPersonId())).thenReturn(Optional.of(person));
        when(roomRepository.getRoomByIdAvailable(request.getRoomId())).thenReturn(new Room());
        String message = assertThrows(IllegalArgumentException.class,
                () -> roomService.resolverSeatRegistration(request)).getMessage();
        assertEquals("Time cannot be set in the past.", message);
    }

    @Test
    void test_resolverSeatRegistration_with_diferentDate() {
        SeatRegistrationRequest request = new SeatRegistrationRequest();
        request.setRoomId(1L);
        request.setPersonId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusDays(1));
        Person person = new Person();
        person.setId(request.getPersonId());
        when(personRepository.findById(request.getPersonId())).thenReturn(Optional.of(person));
        when(roomRepository.getRoomByIdAvailable(request.getRoomId())).thenReturn(new Room());
        String message = assertThrows(IllegalArgumentException.class,
                () -> roomService.resolverSeatRegistration(request)).getMessage();
        assertEquals("Start time and end time on the same day.", message);
    }

    @Test
    void test_resolverSeatRegistration_with_fullyBooked() {
        SeatRegistrationRequest request = new SeatRegistrationRequest();
        request.setRoomId(1L);
        request.setPersonId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        Person person = new Person();
        person.setId(request.getPersonId());
        when(personRepository.findById(request.getPersonId())).thenReturn(Optional.of(person));
        when(roomRepository.getRoomByIdAvailable(request.getRoomId())).thenReturn(new Room());
        when(roomRepository.getAvailableSeat(request.getRoomId(), InputRequired.SEATS_FOR_ONE_REGISTRATION)).thenReturn(0);
        String message = assertThrows(IllegalArgumentException.class,
                () -> roomService.resolverSeatRegistration(request)).getMessage();
        assertEquals("Fully booked.", message);
    }

    @Test
    void test_resolverSeatRegistration_with_overLapTime() {
        SeatRegistrationRequest request = new SeatRegistrationRequest();
        request.setRoomId(1L);
        request.setPersonId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        Person person = new Person();
        person.setId(request.getPersonId());
        when(personRepository.findById(request.getPersonId())).thenReturn(Optional.of(person));
        when(roomRepository.getRoomByIdAvailable(request.getRoomId())).thenReturn(new Room());
        when(roomRepository.getAvailableSeat(request.getRoomId(), InputRequired.SEATS_FOR_ONE_REGISTRATION)).thenReturn(3);
        when(roomRepository.findRoomIdByOverlapTime(request.getRoomId(), request.getPersonId(), StatusMeetingSchedule.SCHEDULED, request.getStartTime(), request.getEndTime())).thenReturn(request.getRoomId());
        String message = assertThrows(IllegalArgumentException.class,
                () -> roomService.resolverSeatRegistration(request)).getMessage();
        assertEquals("Overlap time.", message);
    }

    @Test
    void test_resolverSeatRegistration_success() {
        SeatRegistrationRequest request = new SeatRegistrationRequest();
        request.setRoomId(1L);
        request.setPersonId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        Person person = new Person();
        person.setId(request.getPersonId());
        Room room = new Room();
        room.setId(request.getRoomId());
        SeatRegistrationDTO dto = new SeatRegistrationDTO();
        dto.setId(100L);
        dto.setRoomId(request.getRoomId());
        dto.setPersonId(request.getPersonId());
        dto.setStartTime(request.getStartTime());
        dto.setEndTime(request.getEndTime());
        dto.setSeatRegistrationStatus(StatusSeatRegistration.REGISTERED);
        dto.setSeatRegistrationTime(LocalDateTime.now());
        when(personRepository.findById(request.getPersonId())).thenReturn(Optional.of(person));
        when(roomRepository.getRoomByIdAvailable(request.getRoomId())).thenReturn(room);
        when(roomRepository.getAvailableSeat(request.getRoomId(), InputRequired.SEATS_FOR_ONE_REGISTRATION)).thenReturn(3);
        when(roomRepository.findRoomIdByOverlapTime(request.getRoomId(), request.getPersonId(), StatusMeetingSchedule.SCHEDULED, request.getStartTime(), request.getEndTime())).thenReturn(null);
        when(seatRegistrationRepository.save(any(SeatRegistration.class))).thenReturn(new ModelMapper().map(dto, SeatRegistration.class));
        SeatRegistrationDTO actual = roomService.resolverSeatRegistration(request);
        assertNotNull(actual);
        assertEquals(dto.getId(), actual.getId());
    }
}

