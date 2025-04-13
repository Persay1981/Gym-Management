package com.example.gymmanagement.model;

public class PackagePlan implements PaymentPlan {

    private final Package gymPackage;

    public PackagePlan(Package gymPackage) {
        this.gymPackage = gymPackage;
    }

    @Override
    public double calculateCost(Member member) {
        return gymPackage.getPrice();
    }
}
