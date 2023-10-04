package com.example.bookingapp.controller;

import com.example.bookingapp.dto.RoomDTO;
import com.example.bookingapp.exception.RoomNotFoundException;
import com.example.bookingapp.request.RoomAddRequest;
import com.example.bookingapp.response.BookingHistoryResponseImpl;
import com.example.bookingapp.response.IBookingHistoryResponse;
import com.example.bookingapp.response.RoomDetailResponse;
import com.example.bookingapp.response.SearchRoomResponse;
import com.example.bookingapp.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.example.bookingapp.exception.BadRequestException;
import com.example.bookingapp.exception.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(RoomRestController.class)
@SpringJUnitConfig
public class RoomRestControllerTest {
    @Autowired
     MockMvc mvc;

    @MockBean
    RoomService roomService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void testCancelRoomSuccess() throws Exception {
        Long id = 1L;

        // Mock the roomService to not throw any exceptions
        doNothing().when(roomService).deleteRoom(id);
        mvc.perform(MockMvcRequestBuilders.delete("/room/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Room with ID " + id + " has been deleted successful !!"));

        verify(roomService, times(1)).deleteRoom(id);

    }

    @Test
    public void testCancelRoomRoomNotFoundException() throws Exception {
        Long id = 1L;

        // Mock the roomService to throw a RoomNotFoundException
        doThrow(ResourceNotFoundException.class).when(roomService).deleteRoom(id);

        mvc.perform(MockMvcRequestBuilders.delete("/room/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Room with ID " + id + " does not exist !!"));

        verify(roomService, times(1)).deleteRoom(id);
    }

    @Test
    public void testCancelRoomBadRequestException() throws Exception {
        Long id = 1L;
        // Mock the roomService to throw a BadRequestException
        doThrow(BadRequestException.class).when(roomService).deleteRoom(id);

        mvc.perform(MockMvcRequestBuilders.delete("/room/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Room has already been deleted !!"));

        verify(roomService, times(1)).deleteRoom(id);
    }

    @Test
    public void testCancelRoomInternalServerError() throws Exception {
        Long id = 1L;

        // Mock the roomService to throw an unexpected exception
        doThrow(RuntimeException.class).when(roomService).deleteRoom(id);

        mvc.perform(MockMvcRequestBuilders.delete("/room/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred"));

        verify(roomService, times(1)).deleteRoom(id);
    }

    @Test
    public void testCancelRoom_WhenInvalidRoomId() throws Exception {
        String invalidRoomId = "10```"; // Invalid room ID with special characters

        mvc.perform(MockMvcRequestBuilders.delete("/room/{id}", invalidRoomId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid : " + invalidRoomId));
    }

    @Test
    void test_createRoom_withCheckFail() throws Exception {
        RoomAddRequest roomRequest = new RoomAddRequest();
        when(roomService.checkInsertRoom(roomRequest)).thenReturn(null);
        String urlTemplate= "/room";
        MvcResult result = mvc.perform(post(urlTemplate)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(roomRequest)))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("Data entry not accurate.",result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateRoomWithValidData(){
        RoomAddRequest roomRequest = RoomAddRequest.builder()
                        .roomName("aki")
                        .capacity(100).build();
        RoomDTO roomDTO = RoomDTO.builder()
                        .roomName("aki")
                        .statusRoom(2)
                        .capacity(100).build();
        Mockito.when(roomService.checkInsertRoom(roomRequest)).thenReturn(roomDTO);
        RoomDTO actual= roomService.checkInsertRoom(roomRequest);
        Assertions.assertEquals(roomDTO,actual);
        Assertions.assertEquals(roomDTO.getRoomName(),actual.getRoomName());
    }

    @Test
    public void testUpdateRoom_ThrowsException() throws Exception {
        Long id = 1L;
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomName("UpdatedRoom");

        when(roomService.updateRoom(any(RoomDTO.class), anyLong())).thenThrow(new RuntimeException("Room not found"));
        mvc.perform(MockMvcRequestBuilders.put("/room/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"UpdatedRoom\"}"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Room not found"));
    }

    @Test
    public void test_detailRoom_withResultNull() throws Exception {
        Long id = 12L;
        String urlTemplate = "/room/{id}";
        when(roomService.getRoomDetail(id)).thenReturn(null);
        MvcResult result = mvc.perform(get(urlTemplate, id))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("Room does not exist", result.getResponse().getContentAsString());
    }

    @Test
    public void test_detailRoom_success() throws Exception {
        Long id = 12L;
        RoomDetailResponse detailResponse = new RoomDetailResponse();
        detailResponse.setRoomName("Room Meeting");
        detailResponse.setCapacity(20);
        detailResponse.setBookedMeeting(null);
        String urlTemplate = "/room/{id}";
        when(roomService.getRoomDetail(id)).thenReturn(detailResponse);
        MvcResult result = mvc.perform(get(urlTemplate, id))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("application/json", result.getResponse().getContentType());
    }

    @Test
    void testListRoomWithValidParameters() throws Exception {
        LocalDateTime fromDate = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime toDate = LocalDateTime.of(2023, 1, 1, 12, 0);
        Integer minCapacity = 10;
        Integer maxCapacity = 20;
        String roomName = "Meeting Room";

        List<SearchRoomResponse> expectedRooms = new ArrayList<>();

        doReturn(expectedRooms).when(roomService).listRoom(fromDate, toDate, minCapacity, maxCapacity, roomName);

        mvc.perform(MockMvcRequestBuilders.get("/room")
                        .param("fromDate", "2023-01-01T10:00")
                        .param("toDate", "2023-01-01T12:00")
                        .param("minCapacity", "10")
                        .param("maxCapacity", "20")
                        .param("roomName", "Meeting Room")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testListRoomWithRoomNotFoundException() throws Exception {
        LocalDateTime fromDate = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime toDate = LocalDateTime.of(2023, 1, 1, 12, 0);
        Integer minCapacity = 10;
        Integer maxCapacity = 20;
        String roomName = "Meeting Room";

        doThrow(RoomNotFoundException.class).when(roomService).listRoom(fromDate, toDate, minCapacity, maxCapacity, roomName);

        mvc.perform(MockMvcRequestBuilders.get("/room")
                        .param("fromDate", "2023-01-01T10:00")
                        .param("toDate", "2023-01-01T12:00")
                        .param("minCapacity", "10")
                        .param("maxCapacity", "20")
                        .param("roomName", "Meeting Room")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    @Test
    void test_bookingHistory() throws Exception {
        String url = "/room/person/{id}/history";
        Long id = 1L;
        String title = "A";
        String roomName = "B";
        Integer statusMeeting = 1;
        List<IBookingHistoryResponse> mockResponseList = new ArrayList<>();
        when(roomService.getBookingHistory(id, title, roomName, statusMeeting,null)).thenReturn(mockResponseList);
        MvcResult result = mvc.perform(get(url, id)
                        .param("title", title)
                        .param("roomName", roomName)
                        .param("statusMeeting", statusMeeting.toString()))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("application/json", result.getResponse().getContentType());
    }

    @Test
    void test_bookingHistory_withPersonId_fail() throws Exception{
        String url = "/room/person/{id}/history";
        Long id = 1L;
        when(roomService.getBookingHistory(id, null, null, null,null)).thenThrow(new IllegalArgumentException("User ID is incorrect."));
        MvcResult result = mvc.perform(get(url, id))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("User ID is incorrect.", result.getResponse().getContentAsString());
    }

    @Test
    void test_bookingHistory_withStatusMeeting_fail() throws Exception {
        String url = "/room/person/{id}/history";
        Long id = 1L;
        Integer statusMeeting = -1;
        when(roomService.getBookingHistory(id, null, null, statusMeeting,null)).thenThrow(new IllegalArgumentException("Status meeting from 0 to 3"));
        MvcResult result = mvc.perform(get(url, id)
                        .param("statusMeeting", statusMeeting.toString()))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("Status meeting from 0 to 3", result.getResponse().getContentAsString());
    }

    @Test
    public void test_meetingByUser_withPersonId_fail() throws Exception {
        Long personId = 1L;
        Long meetingId = 2L;
        String urlTemplate = "/room/person/{personId}/meeting-schedule/{meetingId}";
        when(roomService.findMeetingByUserFromData(personId,meetingId)).thenThrow(new IllegalArgumentException("User ID is incorrect."));
        MvcResult result = mvc.perform(get(urlTemplate,personId,meetingId))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("User ID is incorrect.", result.getResponse().getContentAsString());
    }

    @Test
    public void test_meetingByUser_withMeetingId_fail() throws Exception {
        Long personId = 1L;
        Long meetingId = 2L;
        String urlTemplate = "/room/person/{personId}/meeting-schedule/{meetingId}";
        when(roomService.findMeetingByUserFromData(personId,meetingId)).thenThrow(new IllegalArgumentException("Meeting ID does not exist."));
        MvcResult result = mvc.perform(get(urlTemplate,personId,meetingId))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("Meeting ID does not exist.", result.getResponse().getContentAsString());
    }

    @Test
    public void test_meetingByUser_success() throws Exception {
        Long personId = 1L;
        Long meetingId = 2L;
        String urlTemplate = "/room/person/{personId}/meeting-schedule/{meetingId}";
        BookingHistoryResponseImpl response = new BookingHistoryResponseImpl();
        response.setTitle("Meeting a");
        response.setRoomName("Room 1");
        when(roomService.findMeetingByUserFromData(personId,meetingId)).thenReturn(response);
        MvcResult result = mvc.perform(get(urlTemplate,personId,meetingId))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("application/json", result.getResponse().getContentType());
    }
}
