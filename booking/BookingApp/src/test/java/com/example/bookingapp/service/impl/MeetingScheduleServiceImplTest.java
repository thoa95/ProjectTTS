package com.example.bookingapp.service.impl;

import com.example.bookingapp.constant.StatusMeetingSchedule;
import com.example.bookingapp.constant.StatusRoom;
import com.example.bookingapp.exception.ResourceForbidden;
import com.example.bookingapp.exception.ResourceNotFoundException;
import com.example.bookingapp.exception.ValidationException;
import com.example.bookingapp.model.MeetingSchedule;
import com.example.bookingapp.model.Person;
import com.example.bookingapp.model.Room;
import com.example.bookingapp.repository.MeetingScheduleRepository;
import com.example.bookingapp.repository.PersonRepository;
import com.example.bookingapp.repository.RoomRepository;
import com.example.bookingapp.request.MeetingScheduleRequest;
import com.example.bookingapp.request.MeetingScheduleRequestUpdate;
import com.example.bookingapp.response.MeetingResponseUpdate;
import com.example.bookingapp.response.MeetingScheduleResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class MeetingScheduleServiceImplTest {
    @MockBean
    private RoomRepository roomRepository;
    @MockBean
    private MeetingScheduleRepository meetingScheduleRepository;
    @MockBean
    private PersonRepository personRepository;

    @InjectMocks
    private MeetingScheduleServiceImpl meetingScheduleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBookMeetingSchedule_ValidMeetingRequest() {
        MeetingScheduleRequest request = createValidMeetingRequest();
        Room room = createValidRoom();
        Person person = createValidPerson();

        when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(room));
        when(personRepository.findById(request.getPersonId())).thenReturn(Optional.of(person));
        when(meetingScheduleRepository.save(any())).thenReturn(createValidMeetingSchedule());

        MeetingScheduleResponse response = meetingScheduleService.bookMeetingSchedule(request);

        assertNotNull(response);
        assertEquals(StatusMeetingSchedule.SCHEDULED, response.getStatusMeeting());

        verify(roomRepository, times(1)).findById(request.getRoomId());
        verify(personRepository, times(1)).findById(request.getPersonId());

        verify(meetingScheduleRepository, times(1)).save(any());
    }

    private Room createValidRoom() {
        Room room = new Room();
        room.setId(1L);
        room.setRoomName("Meeting Room A");
        room.setCapacity(10);
        room.setStatusRoom(StatusRoom.AVAILABLE);
        room.setCreatedAt(new Date());
        room.setUpdatedAt(null);
        room.setDeletedAt(null);
        return room;
    }

    private Person createValidPerson() {
        Person person = new Person();
        person.setId(2L);
        person.setFullName("John Doe");
        person.setAge(30);
        return person;
    }

    private MeetingScheduleRequest createValidMeetingRequest() {
        MeetingScheduleRequest request = new MeetingScheduleRequest();
        request.setRoomId(1L);
        request.setPersonId(2L);
        request.setTitle("Sample Meeting");
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusMinutes(30);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        return request;
    }

    private MeetingSchedule createValidMeetingSchedule() {
        MeetingSchedule meetingSchedule = new MeetingSchedule();
        meetingSchedule.setId(1L);
        meetingSchedule.setTitle("Sample Meeting");

        Room room = createValidRoom();
        Person person = createValidPerson();
        meetingSchedule.setRoom(room);
        meetingSchedule.setPerson(person);

        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusMinutes(30);
        meetingSchedule.setStartTime(startTime);
        meetingSchedule.setEndTime(endTime);
        meetingSchedule.setReservationTime(LocalDateTime.now());
        meetingSchedule.setStatusMeeting(StatusMeetingSchedule.SCHEDULED);

        return meetingSchedule;
    }

    @Test
    void testBookMeetingSchedule_RoomNotFound() {
        MeetingScheduleRequest request = createValidMeetingRequest();

        when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> meetingScheduleService.bookMeetingSchedule(request));
        verify(roomRepository, times(1)).findById(request.getRoomId());
    }

    @Test
    public void testBookMeetingSchedule_PersonNotFound() {
        MeetingScheduleRequest request = createValidMeetingRequest();

        request.setPersonId(2L);
        when(roomRepository.findById(any())).thenReturn(Optional.of(createValidRoom()));
        when(personRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> meetingScheduleService.bookMeetingSchedule(request));
        verify(personRepository, times(1)).findById(2L);
    }

    @Test
    void testBookMeetingSchedule_SoftDeletedRoom() {
        MeetingScheduleRequest request = createValidMeetingRequest();

        Room softDeletedRoom = createSoftDeletedRoom();
        when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(softDeletedRoom));
        assertThrows(ResourceForbidden.class, () -> meetingScheduleService.bookMeetingSchedule(request));
        verify(roomRepository, times(1)).findById(request.getRoomId());
    }

    private Room createSoftDeletedRoom() {
        Room room = new Room();
        room.setId(20L);
        room.setRoomName("Meeting Room A");
        room.setCapacity(10);
        room.setCreatedAt(new Date());
        room.setUpdatedAt(null);
        room.setDeletedAt(new Date());
        return room;
    }

    @Test
    public void testBookMeetingSchedule_InvalidMeetingRequest() {
        MeetingScheduleRequest invalidRequest = new MeetingScheduleRequest();

        assertThrows(ValidationException.class, () -> meetingScheduleService.bookMeetingSchedule(invalidRequest));
        verify(roomRepository, never()).findById(any());
        verify(personRepository, never()).findById(any());
        verify(meetingScheduleRepository, never()).save(any());
    }

    @Test
    public void testBookMeetingSchedule_InternalServerError() {
        MeetingScheduleRequest request = createValidMeetingRequest();
        Room room = createValidRoom();
        Person person = createValidPerson();

        when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(room));
        when(personRepository.findById(request.getPersonId())).thenReturn(Optional.of(person));
        when(meetingScheduleRepository.save(any())).thenThrow(new RuntimeException("Database error"));
        assertThrows(RuntimeException.class, () -> meetingScheduleService.bookMeetingSchedule(request));
        verify(roomRepository, times(1)).findById(request.getRoomId());
        verify(personRepository, times(1)).findById(request.getPersonId());
        verify(meetingScheduleRepository, times(1)).save(any());
    }

    private Map<Long, List<MeetingSchedule>> getRoomMeetingsCache(MeetingScheduleServiceImpl service) throws NoSuchFieldException, IllegalAccessException {
        Field field = MeetingScheduleServiceImpl.class.getDeclaredField("roomMeetingsCache");
        field.setAccessible(true);
        Object fieldValue = field.get(service);

        if (fieldValue instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Long, List<MeetingSchedule>> roomMeetingsCache = (Map<Long, List<MeetingSchedule>>) fieldValue;
            return roomMeetingsCache;
        } else {
            throw new IllegalStateException("roomMeetingsCache field is not of type Map");
        }
    }

    private MeetingSchedule createMeetingSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        MeetingSchedule meetingSchedule = new MeetingSchedule();
        meetingSchedule.setStartTime(startTime);
        meetingSchedule.setEndTime(endTime);
        return meetingSchedule;
    }

    @Test
    public void testCheckMeetingConflicts_ExactOverlap() throws Exception {
        MeetingScheduleServiceImpl service = new MeetingScheduleServiceImpl();
        Room room = createValidRoom();
        MeetingScheduleRequest request = createValidMeetingRequest();
        LocalDateTime existingStartTime = request.getStartTime();
        LocalDateTime existingEndTime = request.getEndTime();
        MeetingSchedule existingMeeting = createMeetingSchedule(existingStartTime, existingEndTime);

        Map<Long, List<MeetingSchedule>> roomMeetingsCache = getRoomMeetingsCache(service);
        List<MeetingSchedule> meetingsInRoom = new ArrayList<>();
        meetingsInRoom.add(existingMeeting);
        roomMeetingsCache.put(room.getId(), meetingsInRoom);

        Method checkMeetingConflictsMethod = MeetingScheduleServiceImpl.class.getDeclaredMethod(
                "checkMeetingConflicts", Room.class, MeetingScheduleRequest.class
        );
        checkMeetingConflictsMethod.setAccessible(true);
        boolean isConflict = (boolean) checkMeetingConflictsMethod.invoke(service, room, request);
        assertTrue(isConflict);
    }

    @Test
    public void testCheckMeetingConflicts_PartialOverlap() throws Exception {
        MeetingScheduleServiceImpl service = new MeetingScheduleServiceImpl();
        Room room = createValidRoom();
        MeetingScheduleRequest request = createValidMeetingRequest();

        LocalDateTime existingStartTime = request.getStartTime().minusMinutes(15);
        LocalDateTime existingEndTime = request.getEndTime().plusMinutes(15);
        MeetingSchedule existingMeeting = createMeetingSchedule(existingStartTime, existingEndTime);

        Map<Long, List<MeetingSchedule>> roomMeetingsCache = getRoomMeetingsCache(service);
        List<MeetingSchedule> meetingsInRoom = new ArrayList<>();
        meetingsInRoom.add(existingMeeting);
        roomMeetingsCache.put(room.getId(), meetingsInRoom);

        Method checkMeetingConflictsMethod = MeetingScheduleServiceImpl.class.getDeclaredMethod(
                "checkMeetingConflicts", Room.class, MeetingScheduleRequest.class
        );
        checkMeetingConflictsMethod.setAccessible(true);
        boolean isConflict = (boolean) checkMeetingConflictsMethod.invoke(service, room, request);
        assertTrue(isConflict);
    }

    @Test
    public void testCheckMeetingConflicts_Adjacent() throws Exception {
        MeetingScheduleServiceImpl service = new MeetingScheduleServiceImpl();
        Room room = createValidRoom();
        MeetingScheduleRequest request = createValidMeetingRequest();

        LocalDateTime existingStartTime = request.getEndTime();
        LocalDateTime existingEndTime = request.getEndTime().plusMinutes(30);
        MeetingSchedule existingMeeting = createMeetingSchedule(existingStartTime, existingEndTime);

        Map<Long, List<MeetingSchedule>> roomMeetingsCache = getRoomMeetingsCache(service);
        List<MeetingSchedule> meetingsInRoom = new ArrayList<>();
        meetingsInRoom.add(existingMeeting);
        roomMeetingsCache.put(room.getId(), meetingsInRoom);

        Method checkMeetingConflictsMethod = MeetingScheduleServiceImpl.class.getDeclaredMethod(
                "checkMeetingConflicts", Room.class, MeetingScheduleRequest.class
        );
        checkMeetingConflictsMethod.setAccessible(true);
        boolean isConflict = (boolean) checkMeetingConflictsMethod.invoke(service, room, request);
        assertTrue(isConflict);
    }

    @Test
    public void testCheckMeetingConflicts_FullyEnclosed() throws Exception {
        MeetingScheduleServiceImpl service = new MeetingScheduleServiceImpl();
        Room room = createValidRoom();
        MeetingScheduleRequest request = createValidMeetingRequest();

        LocalDateTime existingStartTime = request.getStartTime().minusMinutes(30);
        LocalDateTime existingEndTime = request.getEndTime().plusMinutes(30);
        MeetingSchedule existingMeeting = createMeetingSchedule(existingStartTime, existingEndTime);

        Map<Long, List<MeetingSchedule>> roomMeetingsCache = getRoomMeetingsCache(service);
        List<MeetingSchedule> meetingsInRoom = new ArrayList<>();
        meetingsInRoom.add(existingMeeting);
        roomMeetingsCache.put(room.getId(), meetingsInRoom);

        Method checkMeetingConflictsMethod = MeetingScheduleServiceImpl.class.getDeclaredMethod(
                "checkMeetingConflicts", Room.class, MeetingScheduleRequest.class
        );
        checkMeetingConflictsMethod.setAccessible(true);
        boolean isConflict = (boolean) checkMeetingConflictsMethod.invoke(service, room, request);
        assertTrue(isConflict);
    }


    @Test
    void cancelMeeting_DeletedRoom_ThrowsIllegalArgumentException() {
        Long id = 1L;
        Long personId = 2L;
        MeetingSchedule meeting = new MeetingSchedule();
        meeting.setId(id);
        meeting.setPerson(Person.builder().id(personId).build());
        meeting.setStartTime(LocalDateTime.now().plusMinutes(30));
        meeting.setRoom(new Room());
        meeting.getRoom().setDeletedAt(new Date());
        when(meetingScheduleRepository.findById(id)).thenReturn(Optional.of(meeting));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            meetingScheduleService.cancelMeeting(id, personId);
        });
    }

    @Test
    void cancelMeeting_TooLateToCancel_ThrowsIllegalArgumentException() {
        Long id = 1L;
        Long personId = 2L;
        MeetingSchedule meeting = new MeetingSchedule();
        meeting.setId(id);
        meeting.setPerson(Person.builder().id(personId).build());
        meeting.setStartTime(LocalDateTime.now().minusMinutes(10));
        when(meetingScheduleRepository.findById(id)).thenReturn(Optional.of(meeting));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            meetingScheduleService.cancelMeeting(id, personId);
        });
    }


    @Test
    void cancelMeeting_ValidMeeting_SuccessfullyCanceled() {
        Long id = 1L;
        Long personId = 2L;
        LocalDateTime startTime = LocalDateTime.now().plusMinutes(30);
        MeetingSchedule meeting = new MeetingSchedule();
        meeting.setId(id);
        meeting.setPerson(Person.builder().id(personId).build());
        meeting.setStartTime(startTime);
        meeting.setRoom(new Room());
        meeting.setStatusMeeting(StatusMeetingSchedule.SCHEDULED);

        when(meetingScheduleRepository.findById(id)).thenReturn(Optional.of(meeting));
        meetingScheduleService.cancelMeeting(id, personId);
        verify(meetingScheduleRepository, times(1)).save(meeting);
        assertEquals(StatusMeetingSchedule.CANCELED, meeting.getStatusMeeting());
        assertEquals(startTime, meeting.getStartTime());
    }

    @Test
    void cancelMeeting_NonExistMeeting_ThrowsIllegalArgumentException() {
        Long id = 1L;
        Long personId = 2L;
        when(meetingScheduleRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            meetingScheduleService.cancelMeeting(id, personId);
        });
    }

    @Test
    public void testUpdateMeeting_MeetingNotFound() {
        Long meetingId = 1L;
        Long personId = 1L;
        MeetingScheduleRequestUpdate req = new MeetingScheduleRequestUpdate();
        req.setTitle("Updated Title");
        when(meetingScheduleRepository.findById(meetingId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> meetingScheduleService.updateMeeting(meetingId, personId, req));
        verify(meetingScheduleRepository, times(1)).findById(meetingId);
        verify(meetingScheduleRepository, never()).save(any(MeetingSchedule.class));
    }



    @Test
    void testUpdateMeeting() {
        MeetingSchedule existingMeeting = new MeetingSchedule();
        existingMeeting.setId(1L);
        existingMeeting.setTitle("Old Title");
        existingMeeting.setStatusMeeting(StatusMeetingSchedule.SCHEDULED);
        existingMeeting.setStartTime(LocalDateTime.now().plusHours(1));
        existingMeeting.setEndTime(LocalDateTime.now().plusHours(2));
        existingMeeting.setReservationTime(LocalDateTime.now());

        Person person = new Person();
        person.setId(1L);
        existingMeeting.setPerson(person);

        Room room = new Room();
        room.setId(1L);
        existingMeeting.setRoom(room);

        MeetingScheduleRequestUpdate request = new MeetingScheduleRequestUpdate();
        request.setTitle("New Title");
        request.setStartTime(LocalDateTime.now().plusHours(3));
        request.setEndTime(LocalDateTime.now().plusHours(4));

        when(meetingScheduleRepository.findById(1L)).thenReturn(Optional.of(existingMeeting));
        when(meetingScheduleRepository.save(any(MeetingSchedule.class))).thenReturn(existingMeeting);

        MeetingResponseUpdate response = meetingScheduleService.updateMeeting(1L, 1L, request);

        assertEquals("New Title", response.getTitle());
        assertEquals(existingMeeting.getId(), response.getMeetingId());
    }
}