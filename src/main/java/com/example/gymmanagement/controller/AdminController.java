package com.example.gymmanagement.controller;

import com.example.gymmanagement.model.PendingTrainer;
import com.example.gymmanagement.model.Role;
import com.example.gymmanagement.model.Trainer;
import com.example.gymmanagement.model.User;
import com.example.gymmanagement.repository.PendingTrainerRepository;
import com.example.gymmanagement.repository.TrainerRepository;
import com.example.gymmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

//    @GetMapping("/admin/dashboard")
//    public String showDashboard(Model model) {
//        List<PendingTrainer> pendingTrainers = pendingTrainerRepository.findAll();
//        model.addAttribute("pendingTrainers", pendingTrainers);
//        return "admin";
//    }

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
