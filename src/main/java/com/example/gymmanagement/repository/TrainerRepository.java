package com.example.gymmanagement.repository;

import com.example.gymmanagement.model.Member;
import com.example.gymmanagement.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Integer>{
    Optional<Trainer> findByName(String name);
    Optional<Trainer> findByUsername(String username);
}
