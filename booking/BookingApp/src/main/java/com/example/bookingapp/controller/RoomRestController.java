package com.example.bookingapp.controller;
import com.example.bookingapp.dto.RoomDTO;
import com.example.bookingapp.exception.RoomInvalidFormatParamException;
import com.example.bookingapp.exception.RoomNotFoundException;
import com.example.bookingapp.request.RoomAddRequest;
import com.example.bookingapp.exception.BadRequestException;
import com.example.bookingapp.exception.ResourceNotFoundException;
import com.example.bookingapp.response.DataListRoomResponse;
import com.example.bookingapp.response.IBookingHistoryResponse;
import com.example.bookingapp.response.RoomDetailResponse;
import com.example.bookingapp.response.SearchRoomResponse;
import com.example.bookingapp.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/room")

public class RoomRestController {
    @Autowired
    RoomService roomService;

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.ok("Room with ID " + id + " has been deleted successful !!");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room with ID " + id + " does not exist !!");
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Room has already been deleted !!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");

        }
    }
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody RoomAddRequest roomAddRequest) {
        RoomDTO result = roomService.checkInsertRoom(roomAddRequest);
        if (result == null) {
            return new ResponseEntity<>("Data entry not accurate.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody RoomDTO roomDTO) {
        try {
            RoomDTO updatedRoom = roomService.updateRoom(roomDTO, id);
            return ResponseEntity.ok(updatedRoom);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> detailRoom(@PathVariable Long id) {
        RoomDetailResponse detailResponse = roomService.getRoomDetail(id);
        if (detailResponse == null) {
            return new ResponseEntity<>("Room does not exist", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(detailResponse, HttpStatus.OK);
        }
    }

    @GetMapping
    public ResponseEntity<DataListRoomResponse<List<SearchRoomResponse>>> listRoom(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity,
            @RequestParam(required = false) String roomName
    ) {
        DataListRoomResponse<List<SearchRoomResponse>> responseData = new DataListRoomResponse<>();

        try {
            List<SearchRoomResponse> rooms = roomService.listRoom(fromDate, toDate, minCapacity, maxCapacity, roomName);
            responseData.setData(rooms);
            return ResponseEntity.ok(responseData);
        } catch (RoomNotFoundException e) {
            responseData.setErrorMessage("No rooms match the search criteria.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
        } catch (RoomInvalidFormatParamException e) {
            responseData.setErrorMessage("Format param request is not invalid.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
        } catch (Exception e) {
            responseData.setErrorMessage("An error occurred while searching rooms.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
        }
    }

    @GetMapping("/person/{id}/history")
    public ResponseEntity<?> bookingHistory(@PathVariable(name = "id") Long id,
                                            @RequestParam(name = "title", required = false) String title,
                                            @RequestParam(name = "roomName", required = false) String roomName,
                                            @RequestParam(name = "statusMeeting", required = false) Integer statusMeeting,
                                            @RequestParam(name = "isSort", required = false) Boolean isSort) {
        try {
            List<IBookingHistoryResponse> responseList = roomService.getBookingHistory(id, title, roomName, statusMeeting, isSort);
            return new ResponseEntity<>(responseList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
        }
    }
    @GetMapping("/person/{personId}/meeting-schedule/{meetingId}")
    public ResponseEntity<?> meetingByUser(@PathVariable(name = "personId") Long personId,
                                           @PathVariable (name = "meetingId") Long meetingId){
        try {
            IBookingHistoryResponse result = roomService.findMeetingByUserFromData(personId,meetingId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
        }
    }

}
