package com.example.gymmanagement.repository;

import com.example.gymmanagement.model.DayTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DayTimeSlotRepository extends JpaRepository<DayTimeSlot, Integer> {
//    List<DayTimeSlot> findAllById(List<Long> selectedSlotIds);
}
