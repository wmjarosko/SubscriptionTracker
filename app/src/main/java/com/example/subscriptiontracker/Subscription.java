package com.example.subscriptiontracker;

import java.io.Serializable;
import java.util.Date;

public class Subscription implements Serializable {
    private String name;
    private Date renewalDate;
    private Date startDate;
    private String email;
    private String notes;
    private double price;
    private boolean isRecurring;
    private boolean isMonthly;
    private boolean isActive;
    private Date snoozedUntil;
    private String category;

    public Subscription(String name, Date startDate, Date renewalDate, String email, boolean isRecurring, String notes, boolean isMonthly, double price, String category) {
        this.name = name;
        this.startDate = startDate;
        this.renewalDate = renewalDate;
        this.email = email;
        this.isRecurring = isRecurring;
        this.notes = notes;
        this.isMonthly = isMonthly;
        this.price = price;
        this.category = category;
        this.isActive = true; // By default, a new subscription is active
    }

    // Overloading constructor for backward compatibility
    public Subscription(String name, Date startDate, Date renewalDate, String email, boolean isRecurring, String notes, boolean isMonthly) {
        this(name, startDate, renewalDate, email, isRecurring, notes, isMonthly, 0.0, null);
    }

    // Overloading constructor for backward compatibility
    public Subscription(String name, Date startDate, Date renewalDate, String email, boolean isRecurring, String notes) {
        this(name, startDate, renewalDate, email, isRecurring, notes, false, 0.0, null);
    }

    // Overloading constructor for backward compatibility
    public Subscription(String name, Date startDate, Date renewalDate, String email, boolean isRecurring) {
        this(name, startDate, renewalDate, email, isRecurring, "", false, 0.0, null);
    }


    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getRenewalDate() {
        return renewalDate;
    }

    public String getEmail() {
        return email;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public boolean isMonthly() {
        return isMonthly;
    }

    public void setMonthly(boolean monthly) {
        isMonthly = monthly;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getSnoozedUntil() {
        return snoozedUntil;
    }

    public void setSnoozedUntil(Date snoozedUntil) {
        this.snoozedUntil = snoozedUntil;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setRenewalDate(Date renewalDate) {
        this.renewalDate = renewalDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }
}
