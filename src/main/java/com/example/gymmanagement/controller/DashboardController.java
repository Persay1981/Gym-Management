package com.example.gymmanagement.controller;

import com.example.gymmanagement.model.*;
import com.example.gymmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Controller
public class DashboardController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PendingPackageRepository pendingPackageRepository;

    @Autowired
    private PendingTrainerRepository pendingTrainerRepository;

    @Autowired
    private DayTimeSlotRepository dayTimeSlotRepository;

    @Autowired
    private GymSessionRepository gymSessionRepository;


    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        switch (role) {
            case "ROLE_ADMIN":
                List<PendingTrainer> pendingTrainers = pendingTrainerRepository.findAll();
                model.addAttribute("pendingPackages", pendingPackageRepository.findAll());
                model.addAttribute("pendingTrainers", pendingTrainers);
                return "admin";
            case "ROLE_TRAINER":
                Optional<Trainer> optionalTrainer = trainerRepository.findByUsername(username);
                List<DayTimeSlot> allSlots = dayTimeSlotRepository.findAll();
                if(optionalTrainer.isPresent()) {
                    Trainer trainer = optionalTrainer.get();

                    // Fetch upcoming sessions
                    List<GymSession> upcomingSessions = gymSessionRepository.findByTrainerAndDateAfterOrderByDateAsc(
                            trainer,
                            new Date()
                    );

                    model.addAttribute("trainer", trainer);
                    model.addAttribute("availableSlots", allSlots);
                    model.addAttribute("upcomingSessions", upcomingSessions);
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
                    List<GymSession> upcomingSessions = gymSessionRepository.findByMemberAndDateAfter(member, new Date());

                    model.addAttribute("member", member);
                    model.addAttribute("trainers", trainers);
                    model.addAttribute("packages", packageRepository.findAll());
                    model.addAttribute("availableSlots", availableSlots);
                    model.addAttribute("upcomingSessions", upcomingSessions);
                    model.addAttribute("today", LocalDate.now());
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
