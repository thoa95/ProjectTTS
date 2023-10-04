package com.example.bookingapp.service.impl;

import com.example.bookingapp.constant.StatusSeatRegistration;
import com.example.bookingapp.model.Person;
import com.example.bookingapp.model.Room;
import com.example.bookingapp.model.SeatRegistration;
import com.example.bookingapp.repository.SeatRegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class SeatRegistrationServiceImplTest {
    @MockBean
    SeatRegistrationRepository seatRegistrationRepository;

    @InjectMocks
    SeatRegistrationServiceImpl seatRegistrationService;

    @Test
    void testCancelSeatRegistration() {
        Long seatRegistrationId = 1L;
        Long personId = 1L;
        SeatRegistration seatRegistration = SeatRegistration.builder()
                .id(seatRegistrationId)
                .person(Person.builder().id(personId).build())
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .seatRegistrationStatus(StatusSeatRegistration.REGISTERED)
                .room(Room.builder().deletedAt(null).build())
                .build();
        when(seatRegistrationRepository.findById(seatRegistrationId)).thenReturn(Optional.of(seatRegistration));
        when(seatRegistrationRepository.save(any(SeatRegistration.class))).thenReturn(seatRegistration);
        seatRegistrationService.cancleSeatRegistration(seatRegistrationId, personId);
        assertEquals(StatusSeatRegistration.CANCELED, seatRegistration.getSeatRegistrationStatus());
        verify(seatRegistrationRepository, times(1)).save(seatRegistration);
    }

    @Test
    void testCancelSeatRegistration_InvalidPersonId() {
        Long seatRegistrationId = 1L;
        Long personId = 2L;
        SeatRegistration seatRegistration = SeatRegistration.builder()
                .id(seatRegistrationId)
                .person(Person.builder().id(1L).build())
                .build();
        when(seatRegistrationRepository.findById(seatRegistrationId)).thenReturn(Optional.of(seatRegistration));
        assertThrows(IllegalArgumentException.class, () -> seatRegistrationService.cancleSeatRegistration(seatRegistrationId, personId));
    }

    @Test
    void testCancelSeatRegistration_InProgressSeatRegistration() {
        Long seatRegistrationId = 1L;
        Long personId = 1L;
        LocalDateTime now = LocalDateTime.now();
        SeatRegistration seatRegistration = SeatRegistration.builder()
                .id(seatRegistrationId)
                .person(Person.builder().id(personId).build())
                .startTime(now.minusMinutes(30))
                .endTime(now.plusMinutes(30))
                .build();
        when(seatRegistrationRepository.findById(seatRegistrationId)).thenReturn(Optional.of(seatRegistration));
        assertThrows(IllegalArgumentException.class, () -> seatRegistrationService.cancleSeatRegistration(seatRegistrationId, personId));
    }

    @Test
    void testCancelSeatRegistration_PastSeatRegistration() {
        Long seatRegistrationId = 1L;
        Long personId = 1L;
        LocalDateTime now = LocalDateTime.now();
        SeatRegistration seatRegistration = SeatRegistration.builder()
                .id(seatRegistrationId)
                .person(Person.builder().id(personId).build())
                .startTime(now.minusHours(2))
                .endTime(now.minusHours(1))
                .build();
        when(seatRegistrationRepository.findById(seatRegistrationId)).thenReturn(Optional.of(seatRegistration));
        assertThrows(IllegalArgumentException.class, () -> seatRegistrationService.cancleSeatRegistration(seatRegistrationId, personId));
    }
}