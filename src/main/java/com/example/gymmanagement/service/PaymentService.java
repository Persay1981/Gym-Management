package com.example.gymmanagement.service;

import com.example.gymmanagement.model.Member;
import com.example.gymmanagement.model.PackagePlan;
import com.example.gymmanagement.model.PaymentPlan;
import com.example.gymmanagement.model.SessionsPerWeekPlan;
import com.example.gymmanagement.repository.GymSessionRepository;
import com.example.gymmanagement.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class PaymentService {

    @Autowired
    private GymSessionRepository gymSessionRepository;

    @Autowired
    private MemberRepository memberRepository;

    public void updateRemainingSessions(Member member) {
        Date now = new Date();
        int count = gymSessionRepository.countByMemberAndDateAfter(member, now);
        member.setRemainingSessions(count);
        memberRepository.save(member);
    }

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

