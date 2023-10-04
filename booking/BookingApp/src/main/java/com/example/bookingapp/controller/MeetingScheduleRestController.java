package com.example.bookingapp.controller;

import com.example.bookingapp.exception.MeetingConflictException;
import com.example.bookingapp.exception.ResourceForbidden;
import com.example.bookingapp.exception.ResourceNotFoundException;
import com.example.bookingapp.exception.ValidationException;
import com.example.bookingapp.request.MeetingScheduleRequest;
import com.example.bookingapp.request.MeetingScheduleRequestUpdate;
import com.example.bookingapp.response.MeetingResponseUpdate;
import com.example.bookingapp.response.MeetingScheduleResponse;
import com.example.bookingapp.service.MeetingScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meeting-schedule")
public class MeetingScheduleRestController {
    @Autowired
    MeetingScheduleService meetingScheduleService;

    @PostMapping()
    public ResponseEntity<?> bookMeetingSchedule(@RequestBody MeetingScheduleRequest meetingScheduleRequest) {
        try {
            MeetingScheduleResponse meetingResponse = meetingScheduleService.bookMeetingSchedule(meetingScheduleRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(meetingResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room or person not found. Please verify the provided IDs.");
        } catch (ResourceForbidden e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Scheduling meetings in soft-deleted room is not allowed.");
        } catch (MeetingConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Scheduling conflict. The requested time slot is not available.");
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input data. Please check your request.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error. Please try again later.");
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeeting(@PathVariable Long id,@RequestParam("personId") Long personId, @RequestBody MeetingScheduleRequestUpdate req) {
        try {
            MeetingResponseUpdate meeting = meetingScheduleService.updateMeeting(id,personId,req);
            return ResponseEntity.ok(meeting);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelMeeting(@PathVariable("id") Long id, @RequestParam("personId") Long personId) {
        try {
            meetingScheduleService.cancelMeeting(id, personId);
            return ResponseEntity.ok("The meeting schedule has been successfully canceled");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
