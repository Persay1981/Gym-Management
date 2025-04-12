package com.example.gymmanagement.controller;

import com.example.gymmanagement.model.*;
import com.example.gymmanagement.model.Package;
import com.example.gymmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private PendingTrainerRepository pendingTrainerRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PendingPackageRepository pendingPackageRepository;

    @Autowired
    private PackageRepository packageRepository;

    @PostMapping("/admin/approve-package")
    public String approvePackage(@RequestParam Long id) {
        Optional<PendingPackage> optional = pendingPackageRepository.findById(id);
        if (optional.isPresent()) {
            PendingPackage pending = optional.get();

            Package pkg = new Package();
            pkg.setName(pending.getName());
            pkg.setDurationWeeks(pending.getDurationWeeks());
            pkg.setPrice(pending.getPrice());

            Map<DayOfWeek, String> workoutSchedule = new HashMap<>(pending.getWorkoutSchedule());
            pkg.setWorkoutSchedule(workoutSchedule);

            packageRepository.save(pkg);
            pendingPackageRepository.delete(pending);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/admin/reject-package")
    public String rejectPackage(@RequestParam Long id) {
        pendingPackageRepository.deleteById(id);
        return "redirect:/dashboard";
    }

    @PostMapping("/admin/approveTrainer")
    public String approveTrainer(@RequestParam("username") String username) {
        PendingTrainer pending = pendingTrainerRepository.findByUsername(username).orElseThrow();

        // Add to Trainer table
        Trainer trainer = new Trainer();
        trainer.setName(pending.getName());
        trainer.setContact(pending.getContact());
        trainer.setUsername(pending.getUsername());
        trainerRepository.save(trainer);

        //Add trainer to Users table so they can login
        User user = User.builder()
                .username(pending.getUsername())
                .password(pending.getPassword())
                .role(Role.TRAINER)
                .build();
        userRepository.save(user);

        // Remove from pending table
        pendingTrainerRepository.delete(pending);

        return "redirect:/dashboard";
    }

    @PostMapping("/admin/rejectTrainer")
    public String rejectTrainer(@RequestParam("username") String username) {
        Optional<PendingTrainer> pendingOpt = pendingTrainerRepository.findByUsername(username);
        pendingOpt.ifPresent(pendingTrainerRepository::delete);

        //In case trainer has somehow got added to the users table
        userRepository.findByUsername(username).ifPresent(userRepository::delete);
        return "redirect:/dashboard";
    }
}
