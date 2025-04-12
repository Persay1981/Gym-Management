package com.example.gymmanagement.controller;

import com.example.gymmanagement.dto.UserDTO;
import com.example.gymmanagement.model.*;
import com.example.gymmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SignUpController {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PendingTrainerRepository pendingTrainerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        model.addAttribute("packages", packageRepository.findAll());
        model.addAttribute("userDTO", new UserDTO()); // Data Transfer Object for user
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignUp(@ModelAttribute("userDTO") UserDTO userDTO) {
        String hashedPassword = passwordEncoder.encode(userDTO.getPassword());

        // Save to users table (applies to both roles)

        if (userDTO.getRole() == Role.MEMBER) {
            User user = User.builder()
                    .username(userDTO.getUsername())
                    .password(hashedPassword)
                    .role(userDTO.getRole())
                    .build();
            userRepository.save(user);

            Member member = getMember(userDTO);
            memberRepository.save(member);
        } else if (userDTO.getRole() == Role.TRAINER) {
            // Store in pending trainers table for admin approval
            PendingTrainer pendingTrainer = PendingTrainer.builder()
                    .name(userDTO.getName())
                    .contact(userDTO.getContact())
                    .username(userDTO.getUsername())
                    .password(hashedPassword)
                    .build();
            pendingTrainerRepository.save(pendingTrainer);
        }

        return "redirect:/login";
    }

    private static Member getMember(UserDTO userDTO) {
        Member member = new Member();
        member.setName(userDTO.getName());
        member.setUsername(userDTO.getUsername());
        member.setContact(userDTO.getContact());
        member.setPaymentPlanType(userDTO.getPaymentPlanType());

        if ("SessionPerWeek".equalsIgnoreCase(member.getPaymentPlanType())) {
            member.setGymPackage(null);
        } else if ("Package".equalsIgnoreCase(member.getPaymentPlanType())) {
            member.setCarriedOverSessions(0); // Not used but stored for consistency
        }
        return member;
    }
}
