package com.example.bookingapp.controller;

import com.example.bookingapp.constant.StatusMeetingSchedule;
import com.example.bookingapp.exception.ResourceForbidden;
import com.example.bookingapp.exception.ResourceNotFoundException;
import com.example.bookingapp.request.MeetingScheduleRequest;
import com.example.bookingapp.request.MeetingScheduleRequestUpdate;
import com.example.bookingapp.response.MeetingScheduleResponse;
import com.example.bookingapp.service.MeetingScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeetingScheduleRestController.class)
@SpringJUnitConfig
class MeetingScheduleRestControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    MeetingScheduleService meetingScheduleService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testBookMeetingSchedule_Success() throws Exception {
        MeetingScheduleRequest validMeetingRequest = new MeetingScheduleRequest();
        validMeetingRequest.setRoomId(1L);
        validMeetingRequest.setPersonId(1L);
        validMeetingRequest.setTitle("Meeting 1");
        validMeetingRequest.setStartTime(LocalDateTime.of(2023, 9, 20, 10, 0, 0));
        validMeetingRequest.setEndTime(LocalDateTime.of(2023, 9, 20, 11, 0, 0));

        MeetingScheduleResponse mockResponse = new MeetingScheduleResponse();
        mockResponse.setMeetingId(1L);
        mockResponse.setTitle("Meeting 1");
        mockResponse.setStartTime(validMeetingRequest.getStartTime());
        mockResponse.setEndTime(validMeetingRequest.getEndTime());
        mockResponse.setStatusMeeting(StatusMeetingSchedule.SCHEDULED);
        mockResponse.setReservationTime(LocalDateTime.now());

        Mockito.when(meetingScheduleService.bookMeetingSchedule(Mockito.any())).thenReturn(mockResponse);

        String validMeetingRequestJson = objectMapper.writeValueAsString(validMeetingRequest);

        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/meeting-schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validMeetingRequestJson));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated());

        resultActions.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(mockResponse)));
    }

    @Test
    void testBookMeetingSchedule_RoomNotFound() throws Exception {
        // Define a valid meeting request
        MeetingScheduleRequest validMeetingRequest = new MeetingScheduleRequest();
        validMeetingRequest.setRoomId(1L);
        validMeetingRequest.setPersonId(1L);
        validMeetingRequest.setTitle("Meeting 1");
        validMeetingRequest.setStartTime(LocalDateTime.of(2023, 9, 20, 10, 0, 0));
        validMeetingRequest.setEndTime(LocalDateTime.of(2023, 9, 20, 11, 0, 0));

        Mockito.when(meetingScheduleService.bookMeetingSchedule(Mockito.any()))
                .thenThrow(new ResourceNotFoundException("Room not found"));

        String validMeetingRequestJson = objectMapper.writeValueAsString(validMeetingRequest);

        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/meeting-schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validMeetingRequestJson));

        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testBookMeetingSchedule_SoftDeletedRoom() throws Exception {
        // Define a valid meeting request
        MeetingScheduleRequest validMeetingRequest = new MeetingScheduleRequest();
        validMeetingRequest.setRoomId(1L);
        validMeetingRequest.setPersonId(1L);
        validMeetingRequest.setTitle("Meeting 1");
        validMeetingRequest.setStartTime(LocalDateTime.of(2023, 9, 20, 10, 0, 0));
        validMeetingRequest.setEndTime(LocalDateTime.of(2023, 9, 20, 11, 0, 0));

        Mockito.when(meetingScheduleService.bookMeetingSchedule(Mockito.any()))
                .thenThrow(new ResourceForbidden("Scheduling meetings in soft-deleted room is not allowed."));

        String validMeetingRequestJson = objectMapper.writeValueAsString(validMeetingRequest);

        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/meeting-schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validMeetingRequestJson));

        resultActions.andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void testCancelMeetingFailure() throws Exception {
        Long id = 1L;
        Long personId = 2L;

        doThrow(new IllegalArgumentException("Invalid input")).when(meetingScheduleService).cancelMeeting(id, personId);

        ResultActions result = mvc.perform(MockMvcRequestBuilders
                .delete("/meeting-schedule/{id}?personId={personId}", id, personId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input"));
    }

    @Test
    public void testCancelMeetingSuccess() throws Exception {
        Long id = 1L;
        Long personId = 2L;

        doNothing().when(meetingScheduleService).cancelMeeting(id, personId);

        mvc.perform(MockMvcRequestBuilders
                .delete("/meeting-schedule/{id}?personId={personId}", id, personId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("The meeting schedule has been successfully canceled"));
    }

    @Test
    public void testUpdateMeeting_InvalidRequest() throws Exception {
        Long id = 1L;
        Long personId = 1L;
        MeetingScheduleRequestUpdate requestUpdate = new MeetingScheduleRequestUpdate();
        requestUpdate.setTitle(" ");

        String errorMessage = "The title cannot be blank and must not be larger than 255 characters";
        Mockito.when(meetingScheduleService.updateMeeting(id, personId, requestUpdate))
                .thenThrow(new IllegalArgumentException(errorMessage));
        mvc.perform(MockMvcRequestBuilders.put("/meeting-schedule/{id}?personId={personId}", id,personId,requestUpdate)
                .param("personId", personId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(requestUpdate)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}