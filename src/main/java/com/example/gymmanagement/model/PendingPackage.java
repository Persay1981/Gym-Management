package com.example.gymmanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
public class PendingPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int durationWeeks;
    private double price;

    private int sessionsPerWeek;

    // Map each day to a workout split, e.g. MONDAY -> "Leg Day"
    @ElementCollection
    @CollectionTable(name = "pending_package_schedule", joinColumns = @JoinColumn(name = "package_id"))
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "workout_split")
    private Map<DayOfWeek, String> workoutSchedule = new HashMap<>();

    @ManyToOne
    private Trainer proposedBy;

}
