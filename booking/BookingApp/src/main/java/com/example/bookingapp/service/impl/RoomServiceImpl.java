package com.example.bookingapp.service.impl;

import com.example.bookingapp.constant.InputRequired;
import com.example.bookingapp.constant.StatusMeetingSchedule;
import com.example.bookingapp.constant.StatusRoom;
import com.example.bookingapp.constant.StatusSeatRegistration;
import com.example.bookingapp.dto.MeetingScheduleDTO;
import com.example.bookingapp.dto.RoomDTO;
import com.example.bookingapp.dto.SeatRegistrationDTO;
import com.example.bookingapp.exception.RoomNotFoundException;
import com.example.bookingapp.exception.BadRequestException;
import com.example.bookingapp.exception.DatabaseOperationException;
import com.example.bookingapp.exception.ResourceNotFoundException;
import com.example.bookingapp.exception.RoomInvalidFormatParamException;
import com.example.bookingapp.model.MeetingSchedule;
import com.example.bookingapp.model.Person;
import com.example.bookingapp.model.Room;
import com.example.bookingapp.model.SeatRegistration;
import com.example.bookingapp.repository.MeetingScheduleRepository;
import com.example.bookingapp.repository.PersonRepository;
import com.example.bookingapp.repository.RoomRepository;
import com.example.bookingapp.repository.SeatRegistrationRepository;
import com.example.bookingapp.request.RoomAddRequest;
import com.example.bookingapp.request.SeatRegistrationRequest;
import com.example.bookingapp.response.IBookingHistoryResponse;
import com.example.bookingapp.response.MeetingDetail;
import com.example.bookingapp.response.RoomDetailResponse;
import com.example.bookingapp.response.SearchRoomResponse;
import com.example.bookingapp.service.RoomService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.ArrayList;

import java.util.logging.Logger;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class RoomServiceImpl implements RoomService {
    private static final Logger logger = Logger.getLogger(RoomServiceImpl.class.getName());
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    MeetingScheduleRepository meetingScheduleRepository;
    @Autowired
    PersonRepository personRepository;
    @Autowired
    SeatRegistrationRepository seatRegistrationRepository;

    @Override
    public void deleteRoom(Long roomId) {
        Optional<Room> roomOptional = roomRepository.findById(roomId);

        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();
            if (room.getDeletedAt() != null) {
                throw new BadRequestException("Room with ID " + roomId + " has already been deleted.");
            } else {
                try {
                    room.setDeletedAt(new Date());
                    roomRepository.save(room);
                } catch (DataAccessException e) {
                    throw new DatabaseOperationException("Error while soft deleting room with ID " + roomId, e);
                }
            }
        } else {
            throw new ResourceNotFoundException("Room with ID " + roomId + " does not exist.");
        }
    }

    @Override
    public RoomDTO updateRoom(RoomDTO roomDTO, Long roomId) {
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isPresent()) {
            Room existingRoom = roomOptional.get();
            if (existingRoom.getDeletedAt() != null) {
                throw new IllegalArgumentException("Room has been soft-deleted and cannot be updated");
            } else {
                validateUpdateRoom(roomDTO);
                existingRoom.setRoomName(roomDTO.getRoomName());
                existingRoom.setCapacity(roomDTO.getCapacity());
                existingRoom.setStatusRoom(roomDTO.getStatusRoom());
                existingRoom.setUpdatedAt(new Date());
                roomRepository.save(existingRoom);
                return roomDTO;
            }
        } else {
            throw new ResourceNotFoundException("Room with ID " + roomId + " does not exist.");
        }
    }

    private void validateUpdateRoom(RoomDTO room) {
        if (room == null || room.getRoomId() == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }
        if (room.getRoomName() == null || room.getRoomName().trim().isEmpty()) {
            throw new IllegalArgumentException("Room name cannot be isEmpty");
        }
        if (room.getRoomName().length() > 255) {
            throw new IllegalArgumentException("Room name < 255");
        }
        if (roomRepository.existsByRoomName(room.getRoomName())) {
            throw new IllegalArgumentException("Room name cannot be the same");
        }
        if (!room.getRoomName().matches("[a-zA-Z0-9\\s]+")) {
            throw new IllegalArgumentException("Room names cannot contain special characters");
        }
        if (room.getCapacity() <= 0) {
            throw new IllegalArgumentException("The number of capacity must be greater than 0");
        }
        if (room.getStatusRoom() !=2 ) {
            throw new IllegalArgumentException("Room status must be 2 - available");
        }
    }

    @Override
    public RoomDTO checkInsertRoom(RoomAddRequest roomRequest) {
        if (roomRequest == null) {
            return null;
        } else {
            if (checkRoomName(roomRequest) && roomRequest.getCapacity() > 0) {
                Room newRoom = new ModelMapper().map(roomRequest, Room.class);
                newRoom.setStatusRoom(StatusRoom.AVAILABLE);
                newRoom.setCreatedAt(new Date());
                Room saveRoom = roomRepository.save(newRoom);
                return new ModelMapper().map(saveRoom, RoomDTO.class);
            } else {
                return null;
            }
        }
    }

    private boolean checkRoomName(RoomAddRequest roomRequest) {
        if (roomRequest == null || roomRequest.getRoomName() == null || roomRequest.getRoomName().trim().length() == 0)
            return false;
        else {
            boolean exists = roomRepository.existsByRoomName(roomRequest.getRoomName().trim());
            if (exists) {
                return false;
            } else {
                return roomRequest.getRoomName().matches("[a-zA-Z0-9\\s]{1,255}");
            }
        }
    }

    @Override
    public RoomDetailResponse getRoomDetail(Long roomId) {
        Room room = roomRepository.getRoomByIdAvailable(roomId);
        if (room == null) {
            return null;
        } else {
            return convertToResponse(room, getBookedMeetings(roomId));
        }
    }

    private List<MeetingSchedule> getBookedMeetings(Long id) {
        return meetingScheduleRepository.bookedList(id, StatusMeetingSchedule.SCHEDULED);
    }

    private RoomDetailResponse convertToResponse(Room room, List<MeetingSchedule> scheduleList) {
        RoomDetailResponse detailResponse = new RoomDetailResponse();

        detailResponse.setRoomName(room.getRoomName());
        detailResponse.setCapacity(room.getCapacity());
        if (scheduleList == null || scheduleList.isEmpty()) {
            detailResponse.setBookedMeeting(null);
        } else {
            List<MeetingDetail> detailList = new LinkedList<>();
            for (MeetingSchedule ms : scheduleList) {
                MeetingDetail md = new MeetingDetail();
                md.setMeetingId(ms.getId());
                md.setPersonId(ms.getPerson().getId());
                md.setTitle(ms.getTitle());
                md.setStartTime(ms.getStartTime());
                md.setEndTime(ms.getEndTime());
                md.setReservationTime(ms.getReservationTime());
                detailList.add(md);
            }
            detailResponse.setBookedMeeting(detailList);
        }
        return detailResponse;
    }

    @Override
    public List<SearchRoomResponse> listRoom(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Integer minCapacity,
            Integer maxCapacity,
            String roomName
    ) {
        try {
            validateListRoomRequestParams(fromDate, toDate, minCapacity, maxCapacity, roomName);
            List<Room> allRooms = roomRepository.findAll();
            List<Room> filteredRooms = allRooms.stream()
                    .filter(room -> room.getDeletedAt() == null)
                    .sorted(Comparator.comparing(Room::getRoomName))
                    .collect(Collectors.toList());

            filteredRooms = filterRoomsByDate(filteredRooms, fromDate, toDate);

            if (roomName != null && !roomName.isEmpty()) {
                filteredRooms = filterRoomsByName(filteredRooms, roomName);
            }

            if (minCapacity != null || maxCapacity != null) {
                filteredRooms = filterRoomsByCapacity(filteredRooms, minCapacity, maxCapacity);
            }

            if (filteredRooms.isEmpty()) {
                logger.severe("No rooms match the search criteria.");
                throw new RoomNotFoundException("No rooms match the search criteria.");
            }

            return filteredRooms.stream()
                    .map(this::mapToSearchRoomResponse)
                    .collect(Collectors.toList());
        } catch (RoomInvalidFormatParamException e) {
            throw e;
        } catch (RoomNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while searching for rooms.", e);
        }
    }

    private List<Room> filterRoomsByDate(List<Room> rooms, LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            logger.severe("fromDate param cannot be after toDate param.");
            throw new RoomInvalidFormatParamException("fromDate param cannot be after toDate param.");
        }
        List<Room> availableRooms = new ArrayList<>();
        List<Room> unavailableRooms = new ArrayList<>();

        for (Room room : rooms) {
            if (isRoomAvailable(room, fromDate, toDate)) {
                availableRooms.add(room);
            } else {
                unavailableRooms.add(room);
            }
        }

        availableRooms.sort(Comparator.comparing(Room::getRoomName));
        unavailableRooms.sort(Comparator.comparing(Room::getRoomName));

        List<Room> sortedRooms = new ArrayList<>(availableRooms);
        sortedRooms.addAll(unavailableRooms);

        return sortedRooms;
    }

    private List<Room> filterRoomsByName(List<Room> rooms, String roomName) {
        return rooms.stream()
                .filter(room -> room.getRoomName().toLowerCase().contains(roomName.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<Room> filterRoomsByCapacity(List<Room> rooms, Integer minCapacity, Integer maxCapacity) {
        return rooms.stream()
                .filter(room -> {
                    boolean meetsMinCapacity = minCapacity == null || room.getCapacity() >= minCapacity;
                    boolean meetsMaxCapacity = maxCapacity == null || room.getCapacity() <= maxCapacity;
                    return meetsMinCapacity && meetsMaxCapacity;
                })
                .collect(Collectors.toList());
    }

    private boolean isRoomAvailable(Room room, LocalDateTime startTime, LocalDateTime endTime) {
        List<MeetingSchedule> meetingSchedules = getMeetingScheduleForRoom(room.getId(), StatusMeetingSchedule.SCHEDULED);

        if (meetingSchedules.isEmpty()) {
            return true;
        }
        for (MeetingSchedule schedule : meetingSchedules) {
            LocalDateTime scheduleStartTime = schedule.getStartTime();
            LocalDateTime scheduleEndTime = schedule.getEndTime();
            if (checkOverlap(startTime, endTime, scheduleStartTime, scheduleEndTime)) {
                return false;
            }
        }
        return true;
    }

    private SearchRoomResponse mapToSearchRoomResponse(Room room) {
        SearchRoomResponse response = new SearchRoomResponse();
        response.setRoomId(room.getId());
        response.setRoomName(room.getRoomName());
        response.setCapacity(room.getCapacity());
        response.setStatusRoom(room.getStatusRoom());

        List<MeetingScheduleDTO> meetingSchedules = new ArrayList<>();
        List<MeetingSchedule> allSchedulesForRoom = getMeetingScheduleForRoom(room.getId(), StatusMeetingSchedule.SCHEDULED);
        for (MeetingSchedule schedule : allSchedulesForRoom) {
            MeetingScheduleDTO scheduleDTO = new MeetingScheduleDTO();
            scheduleDTO.setMeetingId(schedule.getId());
            scheduleDTO.setTitle(schedule.getTitle());
            scheduleDTO.setStartTime(schedule.getStartTime());
            scheduleDTO.setEndTime(schedule.getEndTime());
            scheduleDTO.setReservationTime(schedule.getReservationTime());
            meetingSchedules.add(scheduleDTO);
        }
        response.setMeetingSchedules(meetingSchedules);
        return response;
    }

    private List<MeetingSchedule> getMeetingScheduleForRoom(Long roomId, Integer statusMeeting) {
        List<MeetingSchedule> allMeetingSchedules = meetingScheduleRepository.findAll();

        return allMeetingSchedules.stream()
                .filter(schedule -> isMatchingSchedule(schedule, roomId, statusMeeting))
                .collect(Collectors.toList());
    }

    private boolean isMatchingSchedule(MeetingSchedule schedule, Long roomId, Integer statusMeeting) {
        return schedule.getRoom().getId().equals(roomId) && schedule.getStatusMeeting() == statusMeeting;
    }

    private boolean checkOverlap(LocalDateTime startFirst, LocalDateTime endFirst, LocalDateTime startSecond, LocalDateTime endSecond) {
        return (startFirst.isBefore(endSecond) || startFirst.isEqual(endSecond)) && (endFirst.isAfter(startSecond) || endFirst.isEqual(startSecond));
    }

    private void validateListRoomRequestParams(LocalDateTime fromDate, LocalDateTime toDate, Integer minCapacity, Integer maxCapacity, String roomName) {
        if (fromDate == null || toDate == null) {
            String errorMessage = "Both fromDate param and toDate param must be provided.";
            logger.severe(errorMessage);
            throw new RoomInvalidFormatParamException(errorMessage);
        }

        if (minCapacity != null && maxCapacity != null && minCapacity >= maxCapacity) {
            String errorMessage = "minCapacity cannot be greater than or equal to maxCapacity.";
            logger.severe(errorMessage);
            throw new RoomInvalidFormatParamException(errorMessage);
        }
        if (minCapacity != null && minCapacity < 0) {
            String errorMessage = "minCapacity cannot be less than zero.";
            logger.severe(errorMessage);
            throw new RoomInvalidFormatParamException(errorMessage);
        }
        if (maxCapacity != null && maxCapacity >= Integer.MAX_VALUE) {
            String errorMessage = "maxCapacity cannot exceed " + Integer.MAX_VALUE + ".";
            logger.severe(errorMessage);
            throw new RoomInvalidFormatParamException(errorMessage);
        }
        if (roomName != null && (roomName.trim().isEmpty() || roomName.length() > 255)) {
            logger.severe("roomName must be a non-empty string with length less than 255.");
            throw new RoomInvalidFormatParamException("roomName must be a non-empty string with length less than 255.");
        }
    }
    @Override
    public List<IBookingHistoryResponse> getBookingHistory(Long personId, String title, String roomName, Integer statusMeeting, Boolean isSort) {
        if (!checkPersonId(personId)) {
            throw new IllegalArgumentException("User ID is incorrect.");
        } else {
            List<IBookingHistoryResponse> result = roomRepository.findBookingHistoryFromData(personId, title, roomName, statusMeeting, isSort);
            if (!checkStatusMeeting(statusMeeting)) {
                throw new IllegalArgumentException("Status meeting from 0 to 3");
            } else if (result.isEmpty()) {
                throw new IllegalArgumentException("No matching results found");
            } else {
                return result;
            }
        }
    }

    @Override
    public IBookingHistoryResponse findMeetingByUserFromData(Long personId, Long meetingId) {
        if (!checkPersonId(personId)) {
            throw new IllegalArgumentException("User ID is incorrect.");
        }
        if (!checkMeetingId(meetingId)) {
            throw new IllegalArgumentException("Meeting ID does not exist.");
        } else {
            IBookingHistoryResponse response = meetingScheduleRepository.findMeetingByUser(personId, meetingId);
            if (response == null) {
                throw new IllegalArgumentException("No matching results found");
            } else {
                return response;
            }
        }
    }

    private boolean checkPersonId(Long personId) {
        if (personId == null) {
            return false;
        } else {
            Optional<Person> findPerson = personRepository.findById(personId);
            return findPerson.isPresent();
        }
    }

    private boolean checkStatusMeeting(Integer statusMeeting){
        return statusMeeting == null || (statusMeeting >= 0 && statusMeeting <= 3);
    }

    private boolean checkMeetingId(Long meetingId) {
        if (meetingId == null) {
            return false;
        } else {
            Optional<MeetingSchedule> findMeeting = meetingScheduleRepository.findById(meetingId);
            return findMeeting.isPresent();
        }
    }

    @Override
    public SeatRegistrationDTO resolverSeatRegistration(SeatRegistrationRequest request) {
        if (!checkPersonId(request.getPersonId())) {
            throw new IllegalArgumentException("User ID is incorrect.");
        }
        if (!checkRoomId(request.getRoomId())) {
            throw new IllegalArgumentException("Room ID is incorrect or Room has been deleted.");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Time must not be left empty.");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now()) || request.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Time cannot be set in the past.");
        }
        if (!request.getStartTime().toLocalDate().equals(request.getEndTime().toLocalDate())) {
            throw new IllegalArgumentException("Start time and end time on the same day.");
        }
        if (Duration.between(request.getStartTime(), request.getEndTime()).toMinutes() < InputRequired.MINIMUM_TIME) {
            throw new IllegalArgumentException("The minimum registration time is "+ InputRequired.MINIMUM_TIME+ " minutes.");
        }
        int availableSeat = roomRepository.getAvailableSeat(request.getRoomId(), InputRequired.SEATS_FOR_ONE_REGISTRATION);
        if (availableSeat < InputRequired.SEATS_FOR_ONE_REGISTRATION) {
            throw new IllegalArgumentException("Fully booked.");
        }
        Long roomIdOverlapTime = roomRepository.findRoomIdByOverlapTime(request.getRoomId(), request.getPersonId(), StatusSeatRegistration.REGISTERED, request.getStartTime(), request.getEndTime());
        if ( roomIdOverlapTime != null) {
            throw new IllegalArgumentException("Overlap time.");
        } else {
            SeatRegistrationDTO registrationDTO = convertToSeatRegistrationDTO(request);
            SeatRegistration seatRegistration = seatRegistrationRepository.save(new ModelMapper().map(registrationDTO, SeatRegistration.class));
            return new ModelMapper().map(seatRegistration,SeatRegistrationDTO.class);
        }
    }

    private boolean checkRoomId(Long roomId) {
        if (roomId == null) {
            return false;
        } else {
            Room room = roomRepository.getRoomByIdAvailable(roomId);
            return room != null;
        }
    }

    private SeatRegistrationDTO convertToSeatRegistrationDTO(SeatRegistrationRequest request) {
        SeatRegistrationDTO registrationDTO = new SeatRegistrationDTO();
        registrationDTO.setRoomId(request.getRoomId());
        registrationDTO.setPersonId(request.getPersonId());
        registrationDTO.setStartTime(request.getStartTime());
        registrationDTO.setEndTime(request.getEndTime());
        registrationDTO.setSeatRegistrationStatus(StatusSeatRegistration.REGISTERED);
        registrationDTO.setSeatRegistrationTime(LocalDateTime.now());
        return registrationDTO;
    }
}
