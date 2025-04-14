package com.example.gymmanagement.controller;

import com.example.gymmanagement.model.*;
import com.example.gymmanagement.model.Package;
import com.example.gymmanagement.repository.DayTimeSlotRepository;
import com.example.gymmanagement.repository.TrainerRepository;
import com.example.gymmanagement.repository.MemberRepository;
import com.example.gymmanagement.repository.PendingPackageRepository;
import com.example.gymmanagement.repository.GymSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TrainerController {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DayTimeSlotRepository dayTimeSlotRepository;

    @Autowired
    private PendingPackageRepository pendingPackageRepository;

    @Autowired
    private GymSessionRepository gymSessionRepository;

    @PostMapping("/trainer/propose-package")
    public String proposePackage(@ModelAttribute PendingPackage pendingPackage,
                                 Authentication authentication) {
        Optional<Trainer> optionalTrainer = trainerRepository.findByUsername(authentication.getName());
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

    @PostMapping("/trainer/confirm-attendance")
    public String confirmAttendance(@RequestParam int sessionId) {
        Optional<GymSession> sessionOptional = gymSessionRepository.findById(sessionId);
        if (sessionOptional.isPresent()) {
            GymSession session = sessionOptional.get();
            session.setAttended(true); // You should have this field in your GymSession model
            gymSessionRepository.save(session);
        }
        return "redirect:/dashboard"; // or wherever you show the trainer dashboard
    }

    @GetMapping("/trainer/schedule")
    public String getTrainerSchedule(Model model, Authentication authentication) {
        Optional<Trainer> trainerOpt = trainerRepository.findByUsername(authentication.getName());
        if (trainerOpt.isEmpty()) return "redirect:/dashboard";

        Trainer trainer = trainerOpt.get();
        List<Member> members = memberRepository.findByTrainer(trainer);
        List<TrainerScheduleEntry> schedule = new ArrayList<>();

        for (Member member : members) {
            Package pkg = member.getGymPackage();
            if (pkg == null || member.getPackageStartDate() == null) continue;

            long totalDaysInPackage = pkg.getWorkoutSchedule().entrySet().stream()
                    .filter(entry -> entry.getValue() != null && !entry.getValue().trim().isEmpty())
                    .count() * pkg.getDurationWeeks();
            long attendedDays = member.getPackageAttendanceCount();
            if(attendedDays >= totalDaysInPackage)
                continue;

            LocalDate startDate = member.getPackageStartDate();
            int durationWeeks = pkg.getDurationWeeks();
            Map<DayOfWeek, String> scheduleMap = pkg.getWorkoutSchedule();

            Set<LocalDate> attendedDates = member.getPackageAttendances() != null
                    ? member.getPackageAttendances()
                    : new HashSet<>();

            for (int week = 0; week < durationWeeks; week++) {
                for (Map.Entry<DayOfWeek, String> entry : scheduleMap.entrySet()) {
                    LocalDate sessionDate = startDate.plusWeeks(week).with(entry.getKey());

                    boolean alreadyAttended = attendedDates.contains(sessionDate);

                    schedule.add(new TrainerScheduleEntry(
                            member.getMemberId(),
                            member.getName(),
                            pkg.getName(),
                            sessionDate,
                            entry.getKey(),
                            entry.getValue(),
                            alreadyAttended
                    ));
                }
            }
        }

        // Group by date for the view
        Map<LocalDate, List<TrainerScheduleEntry>> groupedByDate = schedule.stream()
                .collect(Collectors.groupingBy(TrainerScheduleEntry::getDate, TreeMap::new, Collectors.toList()));

        model.addAttribute("groupedSchedule", groupedByDate);

        return "trainer-schedule";
    }

    @PostMapping("/trainer/confirm-package-attendance")
    public String confirmPackageAttendance(@RequestParam("memberId") int memberId,
                                           @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Optional<Member> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) return "redirect:/trainer/schedule";

        Member member = memberOpt.get();
        Package pkg = member.getGymPackage();

        if (pkg == null || member.getPackageStartDate() == null) return "redirect:/trainer/schedule";

        // Initialize attendance set if null
        if (member.getPackageAttendances() == null) {
            member.setPackageAttendances(new HashSet<>());
        }

        // Only add if not already marked
        if (!member.getPackageAttendances().contains(date)) {
            member.getPackageAttendances().add(date);
            member.setPackageAttendanceCount(member.getPackageAttendanceCount() + 1);

            long totalDaysInPackage = pkg.getWorkoutSchedule().entrySet().stream()
                    .filter(entry -> entry.getValue() != null && !entry.getValue().trim().isEmpty())
                    .count() * pkg.getDurationWeeks();
            long attendedDays = member.getPackageAttendanceCount(); // Using set size directly
//            System.out.println(totalDaysInPackage);
//            System.out.println(attendedDays);

            if (attendedDays == totalDaysInPackage) {
                member.setEligibleForPackageReward(true);
                member.setPackageAttendances(new HashSet<>());
                member.setGymPackage(null);
                member.setPackageStartDate(null);
            }

            memberRepository.save(member);
        }

        return "redirect:/trainer/schedule";
    }



}
