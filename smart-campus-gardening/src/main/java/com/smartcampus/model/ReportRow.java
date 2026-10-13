package com.smartcampus.model;

import java.sql.Timestamp;

/** One row in the unified report (irrigation or maintenance activity). */
public class ReportRow {
    private String activityType;
    private Timestamp activityDate;
    private String gardenName;
    private String details;
    private String staffName;
    private String status;
    public String getActivityType() { return activityType; }
    public void setActivityType(String v) { this.activityType = v; }
    public Timestamp getActivityDate() { return activityDate; }
    public void setActivityDate(Timestamp v) { this.activityDate = v; }
    public String getGardenName() { return gardenName; }
    public void setGardenName(String v) { this.gardenName = v; }
    public String getDetails() { return details; }
    public void setDetails(String v) { this.details = v; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String v) { this.staffName = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}
