package com.example.gymmanagement.controller;

import com.example.gymmanagement.model.DayTimeSlot;
import com.example.gymmanagement.model.Member;
import com.example.gymmanagement.model.Package;
import com.example.gymmanagement.model.Trainer;
import com.example.gymmanagement.repository.MemberRepository;
import com.example.gymmanagement.repository.PackageRepository;
import com.example.gymmanagement.repository.TrainerRepository;
import com.example.gymmanagement.repository.DayTimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MemberController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private DayTimeSlotRepository dayTimeSlotRepository;

//    @GetMapping("/member")
//    public String memberDashboard(Authentication authentication, Model model) {
//        String username = authentication.getName();
//        Optional<Member> optionalMember = memberRepository.findByUsername(username);
//
//        if (optionalMember.isPresent()) {
//            Member member = optionalMember.get();
//            List<Trainer> trainers = trainerRepository.findAll(); // Make sure this returns a non-null list
//
//            model.addAttribute("member", member);
//            model.addAttribute("trainers", trainers);
//
//            return "member";
//        } else {
//            return "error";
//        }
//    }

    @PostMapping("/member/choose-trainer")
    public String chooseTrainer(Authentication authentication, @RequestParam("trainerId") int trainerId) {
        String username = authentication.getName();
        Optional<Member> optionalMember = memberRepository.findByUsername(username);
        Optional<Trainer> optionalTrainer = trainerRepository.findById(trainerId);

        if (optionalMember.isPresent() && optionalTrainer.isPresent()) {
            Member member = optionalMember.get();
            Trainer newTrainer = optionalTrainer.get();

            //Handling old trainer
            Trainer oldTrainer = member.getTrainer();
            if (oldTrainer != null && oldTrainer.getMembers() != null) {
                oldTrainer.getMembers().remove(member);
                trainerRepository.save(oldTrainer);
            }
            member.setTrainer(newTrainer);
            newTrainer.getMembers().add(member);

            trainerRepository.save(newTrainer);
            memberRepository.save(member);
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/member/change-trainer")
    public String changeTrainer(Authentication authentication, @RequestParam("trainerId") int trainerId) {
        return chooseTrainer(authentication, trainerId);
    }

//    @PostMapping("/member/update-plan")
//    public String updatePlan(Authentication authentication,
//                             @RequestParam("paymentPlanType") String paymentPlanType,
//                             @RequestParam(value = "packageId", required = false) Integer packageId,
//                             @RequestParam(value = "remainingSessions", required = false) Integer remainingSessions) {
//        String username = authentication.getName();
//        Optional<Member> optionalMember = memberRepository.findByUsername(username);
//
//        if (optionalMember.isPresent()) {
//            Member member = optionalMember.get();
//
//            // Update payment plan type
//            member.setPaymentPlanType(paymentPlanType);
//
//            // If "Package Plan" is selected, update the selected package
//            if ("Package".equals(paymentPlanType) && packageId != null) {
//                System.out.println("PACKAGE ID:" + packageId);
//                Optional<Package> optionalPackage = packageRepository.findById(packageId);
//                optionalPackage.ifPresent(member::setGymPackage);
//            }
//
//            // If "SessionPerWeek Plan" is selected, update the remaining sessions
//            if ("SessionPerWeek".equals(paymentPlanType) && remainingSessions != null) {
//                member.setRemainingSessions(remainingSessions);
//            }
//
//            // Save the updated member
//            memberRepository.save(member);
//        }
//
//        return "redirect:/dashboard";
//    }

    @PostMapping("/member/update-plan")
    public String updatePlan(
            Authentication authentication,
            @RequestParam("paymentPlanType") String paymentPlanType,
            @RequestParam(value = "packageId", required = false) Integer packageId,
            @RequestParam(value = "preferredSlotIds", required = false) List<Integer> preferredSlotIds
    ) {
        String username = authentication.getName();
        Optional<Member> optionalMember = memberRepository.findByUsername(username);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setPaymentPlanType(paymentPlanType);

            if ("Package".equals(paymentPlanType) && packageId != null) {
                packageRepository.findById(packageId).ifPresent(member::setGymPackage);
            }

            if ("SessionPerWeek".equals(paymentPlanType)) {
                if (preferredSlotIds != null) {
                    List<DayTimeSlot> selectedSlots = dayTimeSlotRepository.findAllById(preferredSlotIds);
                    member.setPreferredSlots(new HashSet<>(selectedSlots));
                } else {
                    member.setPreferredSlots(null);
                }
            }
//            if ("SessionPerWeek".equals(paymentPlanType) && preferredSlotIds != null) {
//                Set<DayTimeSlot> selectedSlots = new HashSet<>(dayTimeSlotRepository.findAllById(preferredSlotIds));
//                member.setPreferredSlots(selectedSlots);
//            }

            memberRepository.save(member);
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/member/select-trainer")
    public String selectTrainer(
            Authentication authentication,
            @RequestParam("trainerId") int trainerId
    ) {
        String username = authentication.getName();
        Optional<Member> optionalMember = memberRepository.findByUsername(username);
        Optional<Trainer> optionalTrainer = trainerRepository.findById(trainerId);

        if (optionalMember.isPresent() && optionalTrainer.isPresent()) {
            Member member = optionalMember.get();
            Trainer trainer = optionalTrainer.get();

            member.setTrainer(trainer);
            memberRepository.save(member);
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/member/available-trainers")
    @ResponseBody
    public List<Map<String, String>> getAvailableTrainers(
            @RequestParam("day") DayOfWeek day,
            @RequestParam("time") String time
    ) {
        LocalTime targetTime = LocalTime.parse(time);
        List<Trainer> allTrainers = trainerRepository.findAll();

        List<Trainer> availableTrainers = allTrainers.stream()
                .filter(trainer -> trainer.getAvailableSlots().stream().anyMatch(slot ->
                        slot.getDayOfWeek().equals(day)
                                && LocalTime.parse(slot.getStartTime()).isBefore(targetTime)
                                && LocalTime.parse(slot.getEndTime()).isAfter(targetTime)
                ))
                .toList();

        return availableTrainers.stream()
                .map(trainer -> {
                    Map<String, String> data = new HashMap<>();
                    data.put("id", String.valueOf(trainer.getTrainerId()));
                    data.put("name", trainer.getName());
                    return data;
                })
                .collect(Collectors.toList());
    }




}
