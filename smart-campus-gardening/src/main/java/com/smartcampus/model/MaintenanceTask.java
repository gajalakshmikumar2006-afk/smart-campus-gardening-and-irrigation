package com.smartcampus.model;

import java.sql.Date;

public class MaintenanceTask {
    private int taskId;
    private int gardenId;
    private String gardenName;
    private Integer assignedUserId;
    private String assignedUserName;
    private String title;
    private String description;
    private String priority;
    private Date dueDate;
    private String status;
    private boolean overdue;

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    public int getGardenId() { return gardenId; }
    public void setGardenId(int gardenId) { this.gardenId = gardenId; }
    public String getGardenName() { return gardenName; }
    public void setGardenName(String gardenName) { this.gardenName = gardenName; }
    public Integer getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Integer assignedUserId) { this.assignedUserId = assignedUserId; }
    public String getAssignedUserName() { return assignedUserName; }
    public void setAssignedUserName(String assignedUserName) { this.assignedUserName = assignedUserName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }
}
