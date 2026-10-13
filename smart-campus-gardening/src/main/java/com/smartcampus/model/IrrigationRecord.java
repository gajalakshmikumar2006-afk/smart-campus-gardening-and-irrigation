package com.smartcampus.model;

import java.sql.Timestamp;

public class IrrigationRecord {
    private int recordId;
    private int scheduleId;
    private Timestamp completedAt;
    private double estimatedWaterUsedLiters;
    private String notes;

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
    public double getEstimatedWaterUsedLiters() { return estimatedWaterUsedLiters; }
    public void setEstimatedWaterUsedLiters(double estimatedWaterUsedLiters) { this.estimatedWaterUsedLiters = estimatedWaterUsedLiters; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
