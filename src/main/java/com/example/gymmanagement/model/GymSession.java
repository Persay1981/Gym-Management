package com.example.gymmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "gym_sessions")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int sessionId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member; // Reference back to the Member

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    private int duration; // Duration of the session in minutes

    private boolean attended;

    private boolean isSessionPerWeekPlan; // True if part of the Session-per-Week Plan
}
