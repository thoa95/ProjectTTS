package com.example.bookingapp.response;

import com.example.bookingapp.dto.MeetingScheduleDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRoomResponse {
    private Long roomId;
    private String roomName;
    private int capacity;
    private Integer statusRoom;
    private List<MeetingScheduleDTO> meetingSchedules;
}

