package com.example.gymmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "workout_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int planId;

    private String name;

    @ElementCollection
    private List<String> exercises; // List of exercises in the plan

    @ElementCollection
    private List<String> machines; // List of machines to be used (e.g., "Treadmill", "Leg Press", "Bench Press")
}
