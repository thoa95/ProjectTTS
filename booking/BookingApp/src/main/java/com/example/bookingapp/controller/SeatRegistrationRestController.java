package com.example.bookingapp.controller;

import com.example.bookingapp.dto.SeatRegistrationDTO;
import com.example.bookingapp.exception.RoomNotFoundException;
import com.example.bookingapp.exception.SeatRegistrationForbidden;
import com.example.bookingapp.exception.SeatRegistrationInvalidFormatParamException;
import com.example.bookingapp.request.SeatRegistrationRequest;
import com.example.bookingapp.response.SeatRegistrationStatisticsResponse;
import com.example.bookingapp.service.RoomService;
import com.example.bookingapp.service.SeatRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;


@RestController
@RequestMapping("/seat-registration")
public class SeatRegistrationRestController {
    @Autowired
    private SeatRegistrationService seatRegistrationService;
    @Autowired
    RoomService roomService;

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelSeatRegistration(@PathVariable("id") Long id, @RequestParam("personId") Long personId) {
        try {
            seatRegistrationService.cancleSeatRegistration(id, personId);
            return ResponseEntity.ok("The seat has been successfully canceled");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createSeatRegistration(@RequestBody SeatRegistrationRequest request) {
        try {
            SeatRegistrationDTO result = roomService.resolverSeatRegistration(request);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
        }
    }

    @GetMapping
    public ResponseEntity<SeatRegistrationStatisticsResponse> getSeatRegistrationStatistics(
            @RequestParam (required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam (required = false) Long roomId,
            @RequestParam (required = false) Integer eachMinute
    ) {
        try {
            SeatRegistrationStatisticsResponse response = seatRegistrationService.statisticSeatRegistration(roomId, date, eachMinute);
            return ResponseEntity.ok(response);
        } catch (SeatRegistrationInvalidFormatParamException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SeatRegistrationStatisticsResponse(null, e.getMessage()));
        } catch (SeatRegistrationForbidden e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new SeatRegistrationStatisticsResponse(null, e.getMessage()));
        } catch (RoomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SeatRegistrationStatisticsResponse(null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SeatRegistrationStatisticsResponse(null, "An error occurred while statisticizing the number of seat registrations."));
        }
    }

}
