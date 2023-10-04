package com.example.bookingapp.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRoomRequest {
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private int minCapacity;
    private int maxCapacity;
    private String roomName;
}