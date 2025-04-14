package com.example.gymmanagement.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class PackageAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    private LocalDate date;

    private String workoutType;

    private LocalDate packageStartDate;
}
