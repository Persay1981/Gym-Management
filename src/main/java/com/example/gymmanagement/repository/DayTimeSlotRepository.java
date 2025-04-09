package com.example.gymmanagement.repository;

import com.example.gymmanagement.model.DayTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DayTimeSlotRepository extends JpaRepository<DayTimeSlot, Integer> {
//    List<DayTimeSlot> findAllById(List<Long> selectedSlotIds);
}
