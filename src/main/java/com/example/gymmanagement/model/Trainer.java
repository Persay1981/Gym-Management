package com.example.gymmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "trainers")
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int trainerId;

    private String username;
    private String contact;
    private String name;
    private String specialization;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL)
    private List<Member> members = new ArrayList<>();


    @ManyToMany
    @JoinTable(
            name = "trainer_available_slots",
            joinColumns = @JoinColumn(name = "trainer_id"),
            inverseJoinColumns = @JoinColumn(name = "day_time_slot_id")
    )
    private Set<DayTimeSlot> availableSlots;

}
