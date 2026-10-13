package com.smartcampus.model;


public class Plant {
    private int plantId;
    private int gardenId;
    private String gardenName;
    private String plantName;
    private String category;
    private int quantity;
    private int wateringFrequencyDays;
    private double waterRequirementLiters;
    private String description;

    public int getPlantId() { return plantId; }
    public void setPlantId(int plantId) { this.plantId = plantId; }
    public int getGardenId() { return gardenId; }
    public void setGardenId(int gardenId) { this.gardenId = gardenId; }
    public String getGardenName() { return gardenName; }
    public void setGardenName(String gardenName) { this.gardenName = gardenName; }
    public String getPlantName() { return plantName; }
    public void setPlantName(String plantName) { this.plantName = plantName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getWateringFrequencyDays() { return wateringFrequencyDays; }
    public void setWateringFrequencyDays(int wateringFrequencyDays) { this.wateringFrequencyDays = wateringFrequencyDays; }
    public double getWaterRequirementLiters() { return waterRequirementLiters; }
    public void setWaterRequirementLiters(double waterRequirementLiters) { this.waterRequirementLiters = waterRequirementLiters; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
