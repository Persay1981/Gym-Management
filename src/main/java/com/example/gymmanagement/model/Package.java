package com.example.gymmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int packageId;

    private String name;
    private int durationWeeks;
    private double price;

    private int sessionsPerWeek;

    // Map of DayOfWeek to workout description
    @ElementCollection
    @CollectionTable(name = "package_workout_schedule", joinColumns = @JoinColumn(name = "package_id"))
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "workout_type")
    private Map<DayOfWeek, String> workoutSchedule = new HashMap<>();
}
