package com.example.gymmanagement.controller;

import com.example.gymmanagement.model.DayTimeSlot;
import com.example.gymmanagement.model.Member;
import com.example.gymmanagement.model.PendingTrainer;
import com.example.gymmanagement.model.Trainer;
import com.example.gymmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PendingTrainerRepository pendingTrainerRepository;

    @Autowired
    private DayTimeSlotRepository dayTimeSlotRepository;

    @Autowired
    private TrainerSlotRepository trainerSlotRepository;


    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        switch (role) {
            case "ROLE_ADMIN":
                List<PendingTrainer> pendingTrainers = pendingTrainerRepository.findAll();
                model.addAttribute("pendingTrainers", pendingTrainers);
                return "admin";
            case "ROLE_TRAINER":
                Optional<Trainer> optionalTrainer = trainerRepository.findByUsername(username);
                List<DayTimeSlot> allSlots = dayTimeSlotRepository.findAll();
                if(optionalTrainer.isPresent()) {
                    Trainer trainer = optionalTrainer.get();
                    model.addAttribute("trainer", trainer);
                    model.addAttribute("availableSlots", allSlots);
                    return "trainer";
                }
                else {
                    return "error";
                }

            case "ROLE_MEMBER":
                Optional<Member> optionalMember = memberRepository.findByUsername(username);
                if(optionalMember.isPresent()) {
                    Member member = optionalMember.get();
                    List<Trainer> trainers = trainerRepository.findAll();
                    List<DayTimeSlot> availableSlots = dayTimeSlotRepository.findAll();

                    model.addAttribute("member", member);
                    model.addAttribute("trainers", trainers);
                    model.addAttribute("packages", packageRepository.findAll());
                    model.addAttribute("availableSlots", availableSlots);
                    return "member";
                }
                else {
                    return "error";
                }
            default:
                return "error";
        }
    }
}
