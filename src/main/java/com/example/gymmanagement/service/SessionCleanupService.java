package com.example.gymmanagement.service;

import com.example.gymmanagement.repository.GymSessionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class SessionCleanupService {

    private final GymSessionRepository gymSessionRepository;

    @PostConstruct
    public void runOnStartup() {
        deletePastSessions();
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void deletePastSessions() {
        Date now = new Date();
        int deletedCount = gymSessionRepository.deleteByDateBefore(now);
        System.out.println("Deleted " + deletedCount + " past gym sessions at " + now);
    }
}
