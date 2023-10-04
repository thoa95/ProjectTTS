package com.example.bookingapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_registration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_registration_id")
    private Long id;
    @JoinColumn(name = "room_id")
    @ManyToOne
    private Room room;
    @JoinColumn(name = "person_id")
    @ManyToOne
    private Person person;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime seatRegistrationTime;
    private Integer seatRegistrationStatus;
}
