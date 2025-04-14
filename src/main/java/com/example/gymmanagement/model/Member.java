package com.example.gymmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "members")
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int memberId;

    private String name;
    private String contact;
    private String paymentPlanType; // "SessionPerWeek" or "Package"
    private int remainingSessions;
    private int carriedOverSessions;
    private String username;
    private boolean eligibleForPackageReward;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "package_id")
    private Package gymPackage;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<GymSession> gymSessions; // List of sessions booked by the member

    @ManyToMany
    @JoinTable(
            name = "member_preferred_slots",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "day_time_slot_id")
    )
    private Set<DayTimeSlot> preferredSlots = new HashSet<>();

    @Column(name = "package_start_date")
    private LocalDate packageStartDate;

    @ElementCollection
    @CollectionTable(name = "member_package_attendance", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "attended_date")
    private Set<LocalDate> packageAttendances = new HashSet<>();

    private int packageAttendanceCount;

    public Boolean getEligibleForPackageReward() {
        return eligibleForPackageReward;
    }
}
