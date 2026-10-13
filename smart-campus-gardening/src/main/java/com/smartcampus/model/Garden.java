package com.smartcampus.model;


public class Garden {
    private int gardenId;
    private String gardenName;
    private String location;
    private double areaSqm;
    private String gardenType;
    private String description;
    private String status;
    private int plantCount;

    public int getGardenId() { return gardenId; }
    public void setGardenId(int gardenId) { this.gardenId = gardenId; }
    public String getGardenName() { return gardenName; }
    public void setGardenName(String gardenName) { this.gardenName = gardenName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getAreaSqm() { return areaSqm; }
    public void setAreaSqm(double areaSqm) { this.areaSqm = areaSqm; }
    public String getGardenType() { return gardenType; }
    public void setGardenType(String gardenType) { this.gardenType = gardenType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getPlantCount() { return plantCount; }
    public void setPlantCount(int plantCount) { this.plantCount = plantCount; }
}
