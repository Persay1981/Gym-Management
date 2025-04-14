package com.example.gymmanagement.controller;

import com.example.gymmanagement.model.*;
import com.example.gymmanagement.model.Package;
import com.example.gymmanagement.repository.MemberRepository;
import com.example.gymmanagement.repository.PackageRepository;
import com.example.gymmanagement.repository.TrainerRepository;
import com.example.gymmanagement.repository.DayTimeSlotRepository;
import com.example.gymmanagement.repository.GymSessionRepository;
import com.example.gymmanagement.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.*;
import java.util.*;

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

    @Autowired
    private GymSessionRepository gymSessionRepository;

    @Autowired
    private PaymentService paymentService;

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


    @PostMapping("/member/update-plan")
    public String updatePlan(
            Authentication authentication,
            @RequestParam("paymentPlanType") String paymentPlanType,
            @RequestParam(value = "packageId", required = false) Integer packageId,
            @RequestParam(value = "preferredSlotIds", required = false) List<Integer> preferredSlotIds,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate

    ) {
        String username = authentication.getName();
        Optional<Member> optionalMember = memberRepository.findByUsername(username);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setPaymentPlanType(paymentPlanType);

            if ("Package".equals(paymentPlanType) && packageId != null) {
                packageRepository.findById(packageId).ifPresent(member::setGymPackage);
                if(startDate != null) {
                    member.setPackageStartDate(startDate);
                }
                else {
                    member.setPackageStartDate(LocalDate.now());
                }
                memberRepository.save(member);
                return "redirect:/member/payment-summary";
            }

            if ("SessionPerWeek".equals(paymentPlanType)) {
                if (preferredSlotIds != null) {
                    List<DayTimeSlot> selectedSlots = dayTimeSlotRepository.findAllById(preferredSlotIds);
                    member.setPreferredSlots(new HashSet<>(selectedSlots));
                } else {
                    member.setPreferredSlots(null);
                }
            }

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
    public List<Map<String, Object>> getAvailableTrainers(
            @RequestParam("day") String day,
            @RequestParam("time") String time
    ) {
        DayOfWeek selectedDay = DayOfWeek.valueOf(day.toUpperCase());
        LocalTime selectedTime = LocalTime.parse(time);

        LocalDateTime now = LocalDateTime.now();
        int daysUntil = (selectedDay.getValue() - now.getDayOfWeek().getValue() + 7) % 7;
        LocalDate sessionDate = now.plusDays(daysUntil).toLocalDate();
        LocalDateTime sessionStartDateTime = sessionDate.atTime(selectedTime);
        LocalDateTime sessionEndDateTime = sessionStartDateTime.plusMinutes(60);

        Date sessionStart = Date.from(sessionStartDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date sessionEnd = Date.from(sessionEndDateTime.atZone(ZoneId.systemDefault()).toInstant());

        List<Trainer> allTrainers = trainerRepository.findAll();
        List<Map<String, Object>> available = new ArrayList<>();

        for (Trainer trainer : allTrainers) {
            // 1. Check if trainer is already booked
            List<GymSession> conflicts = gymSessionRepository.findConflictingSessions(
                    trainer.getTrainerId(), sessionStart, sessionEnd
            );

            if (!conflicts.isEmpty()) continue;

            // 2. Check if trainer is available on the given day/time
            boolean hasMatchingSlot = trainer.getAvailableSlots().stream().anyMatch(slot -> {
                LocalTime start = LocalTime.parse(slot.getStartTime());
                LocalTime end = LocalTime.parse(slot.getEndTime());

                // Session must fully fit within the available time
                LocalTime calculatedSessionStart = selectedTime;
                LocalTime calculatedSessionEnd = selectedTime.plusMinutes(59);

                return slot.getDayOfWeek().equals(selectedDay)
                        && !calculatedSessionStart.isBefore(start)
                        && !calculatedSessionEnd.isAfter(end);
            });



            if (hasMatchingSlot) {
                Map<String, Object> t = new HashMap<>();
                t.put("id", trainer.getTrainerId());
                t.put("name", trainer.getName());
                available.add(t);
            }
        }

        return available;
    }

    @PostMapping("/member/book-session")
    @ResponseBody
    public String bookSession(
            Authentication authentication,
            @RequestParam("trainerId") int trainerId,
            @RequestParam("day") String day,
            @RequestParam("time") String time
    ) {
        Optional<Member> optionalMember = memberRepository.findByUsername(authentication.getName());
        Optional<Trainer> optionalTrainer = trainerRepository.findById(trainerId);

        if (optionalMember.isPresent() && optionalTrainer.isPresent()) {
            Member member = optionalMember.get();
            Trainer trainer = optionalTrainer.get();

            // Convert day + time into LocalDateTime
            DayOfWeek selectedDay = DayOfWeek.valueOf(day.toUpperCase());
            LocalDateTime now = LocalDateTime.now();
            int daysUntil = (selectedDay.getValue() - now.getDayOfWeek().getValue() + 7) % 7;
            LocalDateTime sessionStartDateTime = now.plusDays(daysUntil).toLocalDate().atTime(LocalTime.parse(time));
            Date sessionStart = Date.from(sessionStartDateTime.atZone(ZoneId.systemDefault()).toInstant());
            Date sessionEnd = Date.from(sessionStartDateTime.plusMinutes(60).atZone(ZoneId.systemDefault()).toInstant());

            List<GymSession> conflicts = gymSessionRepository.findConflictingSessions(
                    trainerId, sessionStart, sessionEnd
            );

            if (conflicts.isEmpty()) {
                GymSession session = GymSession.builder()
                        .member(member)
                        .trainer(trainer)
                        .date(sessionStart)
                        .duration(60)
                        .isSessionPerWeekPlan(true)
                        .build();
                gymSessionRepository.save(session);
                return "Session booked successfully for " + day + " at " + time + " with " + trainer.getName() + ".";
            } else {
                return "Trainer is already booked at this time.";
            }
        }

        return "Booking failed: member or trainer not found";
    }

    @GetMapping("/dashboard/find-trainers")
    @ResponseBody
    public List<Map<String, String>> findAvailableTrainersJson(
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
                .filter(trainer -> {
                    LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), targetTime);
                    Date start = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
                    Date end = Date.from(dateTime.plusMinutes(60).atZone(ZoneId.systemDefault()).toInstant());
                    return gymSessionRepository.findConflictingSessions(trainer.getTrainerId(), start, end).isEmpty();
                })
                .toList();

        return availableTrainers.stream()
                .map(trainer -> Map.of(
                        "name", trainer.getName(),
                        "specialization", trainer.getSpecialization()
                ))
                .toList();
    }

    @PostMapping("/member/cancel-session")
    public String cancelSession(@RequestParam("sessionId") int sessionId, Authentication authentication) {
        Optional<Member> member = memberRepository.findByUsername(authentication.getName());
        Optional<GymSession> session = gymSessionRepository.findById(sessionId);

        if (member.isPresent() && session.isPresent() && session.get().getMember().equals(member.get())) {
            gymSessionRepository.delete(session.get());
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/member/payment-summary")
    public String getPaymentSummary(Model model, Authentication authentication) {
        Optional<Member> optionalMember = memberRepository.findByUsername(authentication.getName());

        if (optionalMember.isEmpty()) {
            return "error";
        }

        Member member = optionalMember.get();
        paymentService.updateRemainingSessions(member);
        double amountDue = paymentService.calculatePayment(member);

        model.addAttribute("member", member);
        model.addAttribute("amountDue", amountDue);

        return "payment-summary";
    }

    @PostMapping("/member/pay")
    public String processPayment(@RequestParam double amount, Authentication authentication, RedirectAttributes redirectAttributes) {
        Optional<Member> optionalMember = memberRepository.findByUsername(authentication.getName());

        // Simulate payment processing
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setEligibleForPackageReward(false);
            member.setPackageAttendanceCount(0);
            memberRepository.save(member);
            redirectAttributes.addFlashAttribute("successMessage", "Payment of ₹" + amount + " was successful!");
        }

        return "redirect:/dashboard";
    }

}
