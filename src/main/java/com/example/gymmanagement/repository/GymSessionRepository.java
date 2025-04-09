package com.example.gymmanagement.repository;

import com.example.gymmanagement.model.GymSession;
import com.example.gymmanagement.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface GymSessionRepository extends JpaRepository<GymSession, Integer> {

    @Query(value = "SELECT * FROM gym_sessions s " +
            "WHERE s.trainer_id = :trainerId AND " +
            "((:startTime < ADDTIME(s.date, SEC_TO_TIME(s.duration * 60))) AND " +
            "(:endTime > s.date))",
            nativeQuery = true)
    List<GymSession> findConflictingSessions(
            @Param("trainerId") int trainerId,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime
    );

}

