package com.example.gymmanagement.model;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class TrainerScheduleEntry {
    private int memberId;
    private String memberName;
    private String packageName;
    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private String workoutDescription;
    private boolean attended;

    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}


