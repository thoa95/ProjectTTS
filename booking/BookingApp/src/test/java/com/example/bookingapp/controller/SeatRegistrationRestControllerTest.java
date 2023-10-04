package com.example.bookingapp.controller;

import com.example.bookingapp.dto.SeatRegistrationDTO;
import com.example.bookingapp.request.SeatRegistrationRequest;
import com.example.bookingapp.service.RoomService;
import com.example.bookingapp.service.SeatRegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatRegistrationRestController.class)
class SeatRegistrationRestControllerTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    SeatRegistrationService seatRegistrationService;
    @MockBean
    RoomService roomService;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createSeatRegistration_fail() throws Exception {
        SeatRegistrationRequest request = new SeatRegistrationRequest();
        request.setPersonId(1L);
        request.setRoomId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        when(roomService.resolverSeatRegistration(any(SeatRegistrationRequest.class))).thenThrow(new IllegalArgumentException("User ID is incorrect."));
        MvcResult result = mvc.perform(post("/seat-registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("User ID is incorrect.", result.getResponse().getContentAsString());
    }

    @Test
    void createSeatRegistration_success() throws Exception {
        SeatRegistrationRequest request = new SeatRegistrationRequest();
        request.setPersonId(1L);
        request.setRoomId(1L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));
        when(roomService.resolverSeatRegistration(any(SeatRegistrationRequest.class))).thenReturn(new SeatRegistrationDTO());
        MvcResult result = mvc.perform(post("/seat-registration")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("application/json", result.getResponse().getContentType());
    }

    @Test
    public void testCancelSeatRegistrationFailure() throws Exception {
        Long id = 1L;
        Long personId = 2L;
        doThrow(new IllegalArgumentException("Invalid input")).when(seatRegistrationService).cancleSeatRegistration(id, personId);
        mvc.perform(MockMvcRequestBuilders
                .delete("/seat-registration/{id}?personId={personId}", id, personId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input"));
    }

    @Test
    public void testCancelSeatRegistrationSuccess() throws Exception {
        Long id = 1L;
        Long personId = 2L;
        doNothing().when(seatRegistrationService).cancleSeatRegistration(id, personId);
        mvc.perform(MockMvcRequestBuilders
                .delete("/seat-registration/{id}?personId={personId}", id, personId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("The seat has been successfully canceled"));
    }
}