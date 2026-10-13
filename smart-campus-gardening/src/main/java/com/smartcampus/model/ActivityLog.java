package com.smartcampus.model;

import java.sql.Timestamp;

public class ActivityLog {
    private int logId;
    private Integer userId;
    private String userName;
    private String activityType;
    private String description;
    private Timestamp createdAt;

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
