package com.example.gymmanagement.controller;

import com.example.gymmanagement.model.*;
import com.example.gymmanagement.model.Package;
import com.example.gymmanagement.repository.MemberRepository;
import com.example.gymmanagement.repository.PackageRepository;
import com.example.gymmanagement.repository.TrainerRepository;
import com.example.gymmanagement.repository.DayTimeSlotRepository;
import com.example.gymmanagement.repository.GymSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
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

    @Autowired
    private GymSessionRepository gymSessionRepository;

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
    public List<Map<String, Object>> getAvailableTrainers(
            @RequestParam("day") String day,
            @RequestParam("time") String time
    ) {
        List<Trainer> allTrainers = trainerRepository.findAll();

        DayOfWeek selectedDay = DayOfWeek.valueOf(day.toUpperCase());
        LocalDateTime now = LocalDateTime.now();
        int daysUntil = (selectedDay.getValue() - now.getDayOfWeek().getValue() + 7) % 7;
        LocalDateTime sessionStartDateTime = now.plusDays(daysUntil).toLocalDate().atTime(LocalTime.parse(time));

        Date start = Date.from(sessionStartDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(sessionStartDateTime.plusMinutes(60).atZone(ZoneId.systemDefault()).toInstant());

        return allTrainers.stream()
                .filter(trainer -> gymSessionRepository.findConflictingSessions(trainer.getTrainerId(), start, end).isEmpty())
                .map(trainer -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", trainer.getTrainerId());
                    data.put("name", trainer.getName());
                    return data;
                })
                .collect(Collectors.toList());
    }


    @GetMapping("/member/check-available-trainers")
    @ResponseBody
    public List<Trainer> checkAvailableTrainers(
            @RequestParam("day") DayOfWeek day,
            @RequestParam("time") String timeStr,
            @RequestParam("duration") int durationInMinutes // optional: or hardcode 60
    ) {
        LocalTime time = LocalTime.parse(timeStr);
        LocalDate now = LocalDate.now();
        LocalDate targetDate = now.with(TemporalAdjusters.nextOrSame(day));
        LocalDateTime startDateTime = LocalDateTime.of(targetDate, time);
        LocalDateTime endDateTime = startDateTime.plusMinutes(durationInMinutes);

        Date start = java.sql.Timestamp.valueOf(startDateTime);
        Date end = java.sql.Timestamp.valueOf(endDateTime);

        List<Trainer> all = trainerRepository.findAll();
        return all.stream()
                .filter(t -> gymSessionRepository.findConflictingSessions(t.getTrainerId(), start, end).isEmpty())
                .collect(Collectors.toList());
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
                return "Session booked successfully for " + day + " at " + time + "with" + trainer.getName() + ".";
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



}
