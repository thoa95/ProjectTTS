package com.example.bookingapp.service.impl;

import com.example.bookingapp.constant.StatusMeetingSchedule;
import com.example.bookingapp.exception.MeetingConflictException;
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
import com.example.bookingapp.service.MeetingScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.ArrayList;
import java.util.logging.Logger;

@Service
public class MeetingScheduleServiceImpl implements MeetingScheduleService {
    private final Map<Long, List<MeetingSchedule>> roomMeetingsCache = new HashMap<>();
    private static final Logger logger = Logger.getLogger(MeetingScheduleServiceImpl.class.getName());

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MeetingScheduleRepository meetingRepository;

    @Override
    public MeetingScheduleResponse bookMeetingSchedule(MeetingScheduleRequest meetingScheduleRequest) {
        try {
            validateMeetingRequest(meetingScheduleRequest);

            Room room;
            Optional<Room> roomOptional = roomRepository.findById(meetingScheduleRequest.getRoomId());
            if (roomOptional.isPresent()) {
                room = roomOptional.get();
                if (room.getDeletedAt() != null) {
                    throw new ResourceForbidden("Scheduling meetings in soft-deleted room is not allowed.");
                }
            } else {
                logger.severe("Room not found.");
                throw new ResourceNotFoundException("Room not found");
            }

            Person person = personRepository.findById(meetingScheduleRequest.getPersonId())
                    .orElseThrow(() -> {
                        logger.severe("Person not found.");
                        return new ResourceNotFoundException("Person not found.");
                    });

            boolean isConflict = checkMeetingConflicts(room, meetingScheduleRequest);
            if (isConflict) {
                throw new MeetingConflictException("Meeting conflict detected.");
            }
            MeetingSchedule meetingSchedule = new MeetingSchedule();
            meetingSchedule.setRoom(room);
            meetingSchedule.setPerson(person);
            meetingSchedule.setTitle(meetingScheduleRequest.getTitle());
            meetingSchedule.setStartTime(meetingScheduleRequest.getStartTime());
            meetingSchedule.setEndTime(meetingScheduleRequest.getEndTime());
            meetingSchedule.setReservationTime(LocalDateTime.now());
            meetingSchedule.setStatusMeeting(StatusMeetingSchedule.SCHEDULED);

            MeetingSchedule savedMeeting = meetingRepository.save(meetingSchedule);
            updateMeetingCache(savedMeeting);

            return convertToMeetingResponse(savedMeeting);

        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Room or person not found. Please verify the provided IDs.");
        } catch (MeetingConflictException e) {
            logger.severe("Meeting conflict detected. The requested time slot is not available.");
            throw new MeetingConflictException("Meeting conflict detected. The requested time slot is not available.");
        } catch (ResourceForbidden e) {
            logger.severe("Scheduling meetings in soft-deleted room is not allowed.");
            throw new ResourceForbidden(("Scheduling meetings in soft-deleted room is not allowed."));
        } catch (ValidationException e) {
            throw new ValidationException("Validation error.");
        } catch (Exception e) {
            logger.severe("Internal server error.");
            throw new RuntimeException("Internal server error.");
        }
    }

    private void validateMeetingRequest(MeetingScheduleRequest meetingScheduleRequest) {
        if (meetingScheduleRequest == null) {
            logger.severe("Meeting request cannot be null");
            throw new ValidationException("Meeting request cannot be null");
        }

        Long roomId = meetingScheduleRequest.getRoomId();
        if (roomId == null || roomId <= 0) {
            logger.severe("Room ID must be a positive number and not null.");
            throw new ValidationException("Room ID must be a positive number and not null.");
        }

        Long personId = meetingScheduleRequest.getPersonId();
        if (personId == null || personId <= 0) {
            logger.severe("Person ID must be a positive number and not null.");
            throw new ValidationException("Person ID must be a positive number and not null.");
        }

        String title = meetingScheduleRequest.getTitle();
        if (title == null || title.trim().isEmpty() || title.length() > 255) {
            logger.severe("Title must be a non-empty string with length less than 255.");
            throw new ValidationException("Title must be a non-empty string with length less than 255.");
        }

        LocalDateTime startTime = meetingScheduleRequest.getStartTime();
        LocalDateTime endTime = meetingScheduleRequest.getEndTime();
        LocalDateTime reversionTime = LocalDateTime.now();

        if (startTime == null) {
            logger.severe("Start time must be not null.");
            throw new ValidationException("Start time must be not null.");
        }

        if (endTime == null) {
            logger.severe("End time must be not null.");
            throw new ValidationException("End time must be not null.");
        }

        long durationMinutes = Duration.between(startTime, endTime).toMinutes();

        if (durationMinutes < 15) {
            logger.severe("Meeting duration must be at least 15 minutes.");
            throw new ValidationException("Meeting duration must be at least 15 minutes.");
        }

        if (reversionTime.isAfter(startTime)) {
            logger.severe("Start time must be after reservation time.");
            throw new ValidationException("Start time must be after reservation time.");
        }

        if (!endTime.toLocalDate().isEqual(startTime.toLocalDate())) {
            logger.severe("End time must be within the same day as start time and before 11:59:59 PM.");
            throw new ValidationException("End time must be within the same day as start time and before 11:59:59 PM.");
        }
    }

    private boolean checkMeetingConflicts(Room room, MeetingScheduleRequest meetingScheduleRequest) {
        LocalDateTime startTime = meetingScheduleRequest.getStartTime();
        LocalDateTime endTime = meetingScheduleRequest.getEndTime();
        Long roomId = room.getId();

        List<MeetingSchedule> meetingsInRoom = roomMeetingsCache.getOrDefault(roomId, new ArrayList<>());
        for (MeetingSchedule existingMeeting : meetingsInRoom) {
            LocalDateTime existingStartTime = existingMeeting.getStartTime();
            LocalDateTime existingEndTime = existingMeeting.getEndTime();
            if (startTime.isEqual(existingStartTime) && endTime.isEqual(existingEndTime)) {
                logger.severe("Case 1: Exact Overlap Check");
                return true;
            }
            if ((startTime.isAfter(existingStartTime) && startTime.isBefore(existingEndTime)) ||
                    (endTime.isAfter(existingStartTime) && endTime.isBefore(existingEndTime))) {
                logger.severe("Case 2: Partial Overlap Check");
                return true;
            }
            if (startTime.isEqual(existingEndTime) || endTime.isEqual(existingStartTime)) {
                logger.severe("Case 3: Adjacent Check ");
                return true;
            }
            if (startTime.isAfter(existingStartTime) && endTime.isBefore(existingEndTime)) {
                logger.severe("Case 4: Existing Meeting Fully Encloses the New Meeting");
                return true;
            }
        }
        return false;
    }

    private void updateMeetingCache(MeetingSchedule meeting) {
        Long roomId = meeting.getRoom().getId();

        List<MeetingSchedule> roomMeetings = roomMeetingsCache.getOrDefault(roomId, new ArrayList<>());
        roomMeetings.add(meeting);
        roomMeetingsCache.put(roomId, roomMeetings);
    }

    private MeetingScheduleResponse convertToMeetingResponse(MeetingSchedule meetingSchedule) {
        MeetingScheduleResponse meetingScheduleResponse = new MeetingScheduleResponse();

        meetingScheduleResponse.setMeetingId(meetingSchedule.getId());
        meetingScheduleResponse.setTitle(meetingSchedule.getTitle());
        meetingScheduleResponse.setStartTime(meetingSchedule.getStartTime());
        meetingScheduleResponse.setEndTime(meetingSchedule.getEndTime());
        meetingScheduleResponse.setStatusMeeting(meetingSchedule.getStatusMeeting());
        meetingScheduleResponse.setReservationTime(meetingSchedule.getReservationTime());
        return meetingScheduleResponse;
    }

    @Override
    public MeetingResponseUpdate updateMeeting(Long id, Long personId, MeetingScheduleRequestUpdate req) {
        MeetingSchedule existingMeeting = meetingRepository.findById(id).orElse(null);
        if (existingMeeting == null){
            throw new IllegalArgumentException("No meeting schedule found");
        }
        validateMeetingTime(id, personId, req.getStartTime(), req.getEndTime());
        if (req.getTitle() == null || req.getTitle().trim().isEmpty() || req.getTitle().length() > 255){
            throw new IllegalArgumentException("The title cannot be blank and must not be larger than 255 characters");
        }else if (req.getStartTime() == null ||  req.getEndTime() == null){
            throw new IllegalArgumentException("start time and end time cannot be null");
        }else {
            existingMeeting.setTitle(req.getTitle());
            existingMeeting.setStartTime(req.getStartTime());
            existingMeeting.setEndTime(req.getEndTime());
            existingMeeting.setReservationTime(LocalDateTime.now());
            meetingRepository.save(existingMeeting);
        }
        MeetingResponseUpdate meetingResponseUpdate = new MeetingResponseUpdate();
        meetingResponseUpdate.setMeetingId(existingMeeting.getId());
        meetingResponseUpdate.setPersonId(existingMeeting.getPerson().getId());
        meetingResponseUpdate.setTitle(existingMeeting.getTitle());
        meetingResponseUpdate.setStartTime(existingMeeting.getStartTime());
        meetingResponseUpdate.setEndTime(existingMeeting.getEndTime());
        meetingResponseUpdate.setReservationTime(existingMeeting.getReservationTime());
        return meetingResponseUpdate;
    }
    private void validateMeetingTime(Long id, Long personId, LocalDateTime startTime, LocalDateTime endTime) {
        Optional<MeetingSchedule> optionalMeeting = meetingRepository.findById(id);
        if (!optionalMeeting.isPresent()) {
            throw new IllegalArgumentException("No meeting schedule found");
        }
        MeetingSchedule meeting = optionalMeeting.get();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime updateAt = meeting.getReservationTime();
        Room room = meeting.getRoom();
        if (!meeting.getPerson().getId().equals(personId)) {
            throw new IllegalArgumentException("You do not have the right to update this meeting schedule.");
        } else if (now.isAfter(meeting.getEndTime())) {
            throw new IllegalArgumentException("The meeting has taken place and cannot be edited");
        } else if (now.isAfter(meeting.getStartTime()) && now.isBefore(meeting.getEndTime())) {
            throw new IllegalArgumentException("The meeting is in progress and cannot be edited.");
        } else if (meeting.getRoom().getDeletedAt() != null) {
            throw new IllegalArgumentException("Cannot cancel the appointment because the meeting room has been deleted.");
        } else if (startTime.isBefore(now) || endTime.isBefore(now)) {
            throw new IllegalArgumentException("The meeting time cannot be in the past");
        } else if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        } else if (startTime.plusMinutes(15).isAfter(endTime)) {
            throw new IllegalArgumentException("The minimum meeting duration is 15 minutes");
        } else if (!startTime.toLocalDate().isEqual(endTime.toLocalDate())) {
            throw new IllegalArgumentException("The meeting cannot span across days.");
        } else if (meeting.getStatusMeeting() == StatusMeetingSchedule.CANCELED) {
            throw new IllegalArgumentException("The meeting is cancle");
        } else if (updateAt.plusMinutes(15).isAfter(startTime)) {
            throw new IllegalArgumentException("The meeting appointment time must be 15 minutes from the start time");
        }
        List<MeetingSchedule> conflictingMeetings = meetingRepository.findConflictingMeetingsInRoom(room, startTime, endTime, id);
        if (!conflictingMeetings.isEmpty()) {
            throw new IllegalArgumentException("The meeting schedule conflicts with existing meetings in the same room");
        }
    }

    private void validateMeetingForCancellation(Long id, Long personId) {
        Optional<MeetingSchedule> optionalMeeting = meetingRepository.findById(id);
        if (!optionalMeeting.isPresent()) {
            throw new IllegalArgumentException("No meeting schedule found.");
        }
        MeetingSchedule meeting = optionalMeeting.get();
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime minCancellationTime = currentTime.plusMinutes(15);
        if(!meeting.getPerson().getId().equals(personId)){
            throw new IllegalArgumentException("You do not have the right to cancel this meeting schedule.");
        }else if (meeting.getStartTime().isBefore(minCancellationTime)) {
            throw new IllegalArgumentException("Cancellation must be 15 minutes in advance");
        }else if (meeting.getRoom().getDeletedAt() != null) {
            throw new IllegalArgumentException("Cannot cancel the appointment because the meeting room has been deleted");
        }
    }

    @Override
    public void cancelMeeting(Long id, Long personId) {
        validateMeetingForCancellation(id, personId);
        Optional<MeetingSchedule> optionalMeeting = meetingRepository.findById(id);
        if (optionalMeeting.isPresent()) {
            MeetingSchedule meeting = optionalMeeting.get();
            if (meeting.getStatusMeeting() == StatusMeetingSchedule.CANCELED) {
                throw new IllegalArgumentException("There have been no meetings yet");
            } else {
                meeting.setStatusMeeting(StatusMeetingSchedule.CANCELED);
                meetingRepository.save(meeting);
            }
        }
    }
}

