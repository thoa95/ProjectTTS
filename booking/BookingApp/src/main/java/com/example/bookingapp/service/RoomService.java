package com.example.bookingapp.service;
import com.example.bookingapp.dto.RoomDTO;
import com.example.bookingapp.dto.SeatRegistrationDTO;
import com.example.bookingapp.request.RoomAddRequest;
import com.example.bookingapp.request.SeatRegistrationRequest;
import com.example.bookingapp.response.IBookingHistoryResponse;
import com.example.bookingapp.response.RoomDetailResponse;
import com.example.bookingapp.response.SearchRoomResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomService {
    RoomDTO checkInsertRoom(RoomAddRequest roomRequest);
    void deleteRoom(Long roomId);
    RoomDTO updateRoom(RoomDTO roomDTO, Long roomId) throws Exception;
    RoomDetailResponse getRoomDetail(Long roomId);
    List<SearchRoomResponse> listRoom(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Integer minCapacity,
            Integer maxCapacity,
            String roomName
    );
    List<IBookingHistoryResponse> getBookingHistory(Long personId, String title, String roomName, Integer statusMeeting, Boolean isSort);
    IBookingHistoryResponse findMeetingByUserFromData(Long personId, Long meetingId);
    SeatRegistrationDTO resolverSeatRegistration(SeatRegistrationRequest seatRegistrationRequest);
}
