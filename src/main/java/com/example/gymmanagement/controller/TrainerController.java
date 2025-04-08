package com.example.gymmanagement.controller;

import com.example.gymmanagement.model.DayTimeSlot;
import com.example.gymmanagement.model.Member;
import com.example.gymmanagement.model.Trainer;
import com.example.gymmanagement.repository.MemberRepository;
import com.example.gymmanagement.repository.DayTimeSlotRepository;
import com.example.gymmanagement.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.util.*;

public class TrainerController {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private DayTimeSlotRepository dayTimeSlotRepository;


    @GetMapping("/trainer")
    public String trainerDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<Trainer> optionalTrainer = trainerRepository.findByUsername(username);

        if (optionalTrainer.isPresent()) {
            Trainer trainer = optionalTrainer.get();
            model.addAttribute("trainer", trainer);
            return "trainer";
        } else {
            return "error";
        }
    }

    @PostMapping("/trainer/update-availability")
    public String updateAvailability(
            Authentication authentication,
            @RequestParam Map<String, String> startTimeMap,
            @RequestParam Map<String, String> endTimeMap
    ) {
        String username = authentication.getName();
        Optional<Trainer> optionalTrainer = trainerRepository.findByUsername(username);

        if (optionalTrainer.isPresent()) {
            Trainer trainer = optionalTrainer.get();
            Set<DayTimeSlot> newSlots = new HashSet<>();

            for (DayOfWeek day : DayOfWeek.values()) {
                String startKey = "startTimeMap[" + day + "]";
                String endKey = "endTimeMap[" + day + "]";

                String startTime = startTimeMap.get(startKey);
                String endTime = endTimeMap.get(endKey);

                if (startTime != null && endTime != null && !startTime.isEmpty() && !endTime.isEmpty()) {
                    DayTimeSlot slot = DayTimeSlot.builder()
                            .dayOfWeek(day)
                            .StartTime(startTime)
                            .endTime(endTime)
                            .build();

                    dayTimeSlotRepository.save(slot);  // Optional: Only if not exists already
                    newSlots.add(slot);
                }
            }

            trainer.setAvailableSlots(newSlots);
            trainerRepository.save(trainer);
        }

        return "redirect:/dashboard";
    }


}
