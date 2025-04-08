package com.example.gymmanagement.dto;

import com.example.gymmanagement.model.Package;
import com.example.gymmanagement.model.Role;
import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String name;
    private String contact;
    private Role role;
    private String paymentPlanType; // Only for members
    private Package gymPackage;         // Only for members
}
