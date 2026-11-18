package com.bbsutsd.pocketpilot.model;

import java.io.Serializable;

public class SavingsGoal implements Serializable {
    private int id;
    private String goalName;
    private double targetAmount;
    private double currentAmount;
    private String targetDate;

    public SavingsGoal() {
    }

    public SavingsGoal(int id, String goalName, double targetAmount, double currentAmount, String targetDate) {
        this.id = id;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }

    public int getProgressPercentage() {
        if (targetAmount <= 0) return 0;
        int pct = (int) Math.round((currentAmount / targetAmount) * 100);
        return Math.min(pct, 100);
    }

    public boolean isCompleted() {
        return currentAmount >= targetAmount;
    }
}
