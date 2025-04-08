package com.example.gymmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @OneToMany
    @JoinColumn(name = "package_id")
    private List<WorkoutPlan> workoutPlans; // Workout plans included in the package

    @ElementCollection
    @CollectionTable(name = "package_days", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "day_of_week")
    private List<String> allowedDays; // Days of the week members must attend (e.g., "Monday", "Wednesday", "Friday")
}
