package com.example.gymmanagement.repository;

import com.example.gymmanagement.model.Member;
import com.example.gymmanagement.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByUsername(String username);

    List<Member> findByTrainerAndPaymentPlanType(Trainer trainer, String aPackage);

    List<Member> findByTrainer(Trainer trainer);
}
