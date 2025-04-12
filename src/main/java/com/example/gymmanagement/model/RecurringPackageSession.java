package com.example.gymmanagement.model;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
public class RecurringPackageSession {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Member member;

    private DayOfWeek dayOfWeek;
    private LocalTime time;
}
