package com.example.bookingapp.service.impl;

import com.example.bookingapp.constant.StatusSeatRegistration;
import com.example.bookingapp.exception.RoomNotFoundException;
import com.example.bookingapp.exception.SeatRegistrationForbidden;
import com.example.bookingapp.exception.SeatRegistrationInvalidFormatParamException;
import com.example.bookingapp.model.Room;
import com.example.bookingapp.model.SeatRegistration;
import com.example.bookingapp.repository.RoomRepository;
import com.example.bookingapp.repository.SeatRegistrationRepository;
import com.example.bookingapp.response.SeatRegistrationStatisticsData;
import com.example.bookingapp.response.SeatRegistrationStatisticsResponse;
import com.example.bookingapp.response.iSeatRegistrationStatisticsInterval;
import com.example.bookingapp.service.SeatRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class SeatRegistrationServiceImpl implements SeatRegistrationService {
    private static final Logger logger = Logger.getLogger(SeatRegistrationServiceImpl.class.getName());
    @Autowired
    private SeatRegistrationRepository seatRegistrationRepository;
    @Autowired
    RoomRepository roomRepository;

    @Override
    public void cancleSeatRegistration(Long id, Long personId) {
        SeatRegistration seatRegistration = validateCancelSeatRegistration(id, personId);
        seatRegistration.setSeatRegistrationStatus(StatusSeatRegistration.CANCELED);
        seatRegistrationRepository.save(seatRegistration);
    }

    private SeatRegistration validateCancelSeatRegistration(Long id, Long personId) {
        SeatRegistration seatRegistration = seatRegistrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seat registration id not found"));
        LocalDateTime now = LocalDateTime.now();
        if (!seatRegistration.getPerson().getId().equals(personId)) {
            throw new IllegalArgumentException("You do not have the right to cancel this seat");
        }else if (now.isAfter(seatRegistration.getEndTime())) {
            throw new IllegalArgumentException("Past seat registrations cannot be cancelled");
        }else if (now.isAfter(seatRegistration.getStartTime()) && now.isBefore(seatRegistration.getEndTime())) {
            throw new IllegalArgumentException("Seat registration is in progress and cannot be cancelled");
        }else if (seatRegistration.getRoom().getDeletedAt() != null) {
            throw new IllegalArgumentException("Seats cannot be canceled because the room is deleted");
        } else if (seatRegistration.getSeatRegistrationStatus() != null && seatRegistration.getSeatRegistrationStatus() == StatusSeatRegistration.CANCELED) {
            throw new IllegalArgumentException("You have not registered for this seat yet");
        }
        return seatRegistration;
    }

    @Override
    public SeatRegistrationStatisticsResponse statisticSeatRegistration(Long roomId, LocalDate date, Integer eachMinute) {
        validateParameters(roomId, date, eachMinute);
        List<iSeatRegistrationStatisticsInterval> statistics = seatRegistrationRepository.calculateSeatRegistration(roomId, date, eachMinute);

        SeatRegistrationStatisticsData data = new SeatRegistrationStatisticsData();
        data.setRoomId(roomId);
        data.setDate(date);
        data.setTimeIntervals(statistics);
        return new SeatRegistrationStatisticsResponse(data, null);
    }

    private void validateParameters(Long roomId, LocalDate date, Integer eachMinute) {
        if (date == null || roomId == null || eachMinute == null) {
            logger.severe("Invalid format param request.");
            throw new SeatRegistrationInvalidFormatParamException("Invalid format param request.");
        }
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            logger.severe("Room not found. Please verify the provided Room Id.");
            throw new RoomNotFoundException("Room not found. Please verify the provided Room Id.");
        }
        if (room.getDeletedAt() != null) {
            logger.severe("Statistical analysis of seat registrations in the soft-deleted room is not allowed.");
            throw new SeatRegistrationForbidden("Statistical analysis of seat registrations in the soft-deleted room is not allowed.");
        }
        Integer totalMinutesInDay = 24 * 60;
        if (totalMinutesInDay % eachMinute != 0 || eachMinute < 1 || eachMinute > 1440) {
            logger.severe("Format param eachMinute in request is not satisfied request.");
            throw new SeatRegistrationInvalidFormatParamException("Format param eachMinute in request is not satisfied request.");
        }
    }
}
