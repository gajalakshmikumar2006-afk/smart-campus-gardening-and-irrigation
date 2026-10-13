package com.smartcampus.model;

import java.sql.Timestamp;

public class IrrigationSchedule {
    private int scheduleId;
    private int gardenId;
    private String gardenName;
    private Timestamp scheduledAt;
    private double estimatedWaterLiters;
    private Integer assignedUserId;
    private String assignedUserName;
    private String status;
    private String notes;

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
    public int getGardenId() { return gardenId; }
    public void setGardenId(int gardenId) { this.gardenId = gardenId; }
    public String getGardenName() { return gardenName; }
    public void setGardenName(String gardenName) { this.gardenName = gardenName; }
    public Timestamp getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Timestamp scheduledAt) { this.scheduledAt = scheduledAt; }
    public double getEstimatedWaterLiters() { return estimatedWaterLiters; }
    public void setEstimatedWaterLiters(double estimatedWaterLiters) { this.estimatedWaterLiters = estimatedWaterLiters; }
    public Integer getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Integer assignedUserId) { this.assignedUserId = assignedUserId; }
    public String getAssignedUserName() { return assignedUserName; }
    public void setAssignedUserName(String assignedUserName) { this.assignedUserName = assignedUserName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isActionable() { return "PENDING".equals(status) || "RESCHEDULED".equals(status); }
}
