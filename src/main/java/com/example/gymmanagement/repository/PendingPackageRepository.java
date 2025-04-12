package com.example.gymmanagement.repository;

import com.example.gymmanagement.model.PendingPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PendingPackageRepository extends JpaRepository<PendingPackage, Long> {
    List<PendingPackage> findAllByOrderByIdDesc();
}

