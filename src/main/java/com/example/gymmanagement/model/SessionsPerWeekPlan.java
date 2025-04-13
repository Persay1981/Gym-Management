package com.example.gymmanagement.model;

public class SessionsPerWeekPlan implements PaymentPlan {

    private static final double COST_PER_SESSION = 200.0;

    @Override
    public double calculateCost(Member member) {
        return member.getRemainingSessions() * COST_PER_SESSION;
    }
}

