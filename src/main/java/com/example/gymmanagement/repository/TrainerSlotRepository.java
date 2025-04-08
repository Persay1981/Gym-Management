package com.example.gymmanagement.repository;

import com.example.gymmanagement.model.DayTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerSlotRepository extends JpaRepository<DayTimeSlot, Long> {
}
