package com.example.gymmanagement.service;

import com.example.gymmanagement.model.Member;
import com.example.gymmanagement.model.PackagePlan;
import com.example.gymmanagement.model.PaymentPlan;
import com.example.gymmanagement.model.SessionsPerWeekPlan;
import org.springframework.stereotype.Service;


@Service
public class PaymentService {

    public double calculatePayment(Member member) {
        PaymentPlan plan;

        if ("Package".equals(member.getPaymentPlanType()) && member.getGymPackage() != null) {
            plan = new PackagePlan(member.getGymPackage());
        } else {
            plan = new SessionsPerWeekPlan();
        }

        return plan.calculateCost(member);
    }
}

