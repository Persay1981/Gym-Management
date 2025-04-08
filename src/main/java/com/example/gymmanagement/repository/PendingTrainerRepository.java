package com.example.gymmanagement.repository;

import com.example.gymmanagement.model.PendingTrainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingTrainerRepository extends JpaRepository<PendingTrainer, Long> {
    Optional<PendingTrainer> findByUsername(String username);

}
