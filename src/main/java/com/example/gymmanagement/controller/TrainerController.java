package com.example.gymmanagement.controller;

import com.example.gymmanagement.model.DayTimeSlot;
import com.example.gymmanagement.model.PendingPackage;
import com.example.gymmanagement.model.Trainer;
import com.example.gymmanagement.repository.DayTimeSlotRepository;
import com.example.gymmanagement.repository.TrainerRepository;
import com.example.gymmanagement.repository.PendingPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.DayOfWeek;
import java.util.*;

@Controller
public class TrainerController {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private DayTimeSlotRepository dayTimeSlotRepository;

    @Autowired
    private PendingPackageRepository pendingPackageRepository;

    @PostMapping("/trainer/propose-package")
    public String proposePackage(@ModelAttribute PendingPackage pendingPackage,
                                 Authentication authetication) {
        Optional<Trainer> optionalTrainer = trainerRepository.findByUsername(authetication.getName());
        if (optionalTrainer.isPresent()) {
            Trainer trainer = optionalTrainer.get();
            pendingPackage.setProposedBy(trainer);
            pendingPackageRepository.save(pendingPackage);
        }
        return "redirect:/dashboard?proposed=true";
    }


    @PostMapping("/trainer/update-availability")
    public String updateAvailability(
            Authentication authentication,
            @RequestParam Map<String, String> allParams
    ) {
        String username = authentication.getName();
        Optional<Trainer> optionalTrainer = trainerRepository.findByUsername(username);

        if (optionalTrainer.isPresent()) {
            Trainer trainer = optionalTrainer.get();
            Set<DayTimeSlot> slots = new HashSet<>();

            for (DayOfWeek day : DayOfWeek.values()) {
                String startKey = "startTimeMap[" + day + "]";
                String endKey = "endTimeMap[" + day + "]";

                if (allParams.containsKey(startKey) && allParams.containsKey(endKey)) {
                    String start = allParams.get(startKey);
                    String end = allParams.get(endKey);

                    if (!start.isBlank() && !end.isBlank()) {
                        DayTimeSlot slot = DayTimeSlot.builder()
                                .dayOfWeek(day)
                                .startTime(start)
                                .endTime(end)
                                .build();

                        DayTimeSlot savedSlot = dayTimeSlotRepository.save(slot);
                        slots.add(savedSlot);
                    }
                }
            }

            trainer.setAvailableSlots(slots);
            trainerRepository.save(trainer);
        }

        return "redirect:/dashboard";
    }
}
