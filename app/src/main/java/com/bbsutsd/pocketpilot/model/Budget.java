package com.bbsutsd.pocketpilot.model;

import java.io.Serializable;

public class Budget implements Serializable {
    private int id;
    private String category;
    private String period; // e.g. "Monthly", "Weekly"
    private double limitAmount;
    private double spentAmount;

    public Budget() {
    }

    public Budget(int id, String category, String period, double limitAmount, double spentAmount) {
        this.id = id;
        this.category = category;
        this.period = period;
        this.limitAmount = limitAmount;
        this.spentAmount = spentAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingAmount() {
        return limitAmount - spentAmount;
    }

    public int getUsagePercentage() {
        if (limitAmount <= 0) return 0;
        int pct = (int) Math.round((spentAmount / limitAmount) * 100);
        return Math.min(pct, 100);
    }

    public boolean isOverBudget() {
        return spentAmount > limitAmount;
    }
}
