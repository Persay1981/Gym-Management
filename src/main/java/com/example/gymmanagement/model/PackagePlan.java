package com.example.gymmanagement.model;

public class PackagePlan implements PaymentPlan {

    private final Package gymPackage;

    public PackagePlan(Package gymPackage) {
        this.gymPackage = gymPackage;
    }

    @Override
    public double calculateCost(Member member) {
        double basePrice =  gymPackage.getPrice();

        // Apply reward discount if eligible
        if (Boolean.TRUE.equals(member.getEligibleForPackageReward())) {
            // Example: 10% discount
            System.out.println("AAAAAAAA");
            basePrice = basePrice * 0.9;
        }

        return basePrice;
    }
}
