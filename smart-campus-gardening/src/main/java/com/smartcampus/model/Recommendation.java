package com.smartcampus.model;

import java.util.List;

/** Result of the irrigation recommendation engine for one garden. */
public class Recommendation {
    private final int gardenId;
    private final String gardenName;
    private final boolean waterNow;
    private final double liters;
    private final String headline;
    private final List<String> reasons;
    private final boolean weatherIsDemo;

    public Recommendation(int gardenId, String gardenName, boolean waterNow, double liters,
                          String headline, List<String> reasons, boolean weatherIsDemo) {
        this.gardenId = gardenId; this.gardenName = gardenName; this.waterNow = waterNow;
        this.liters = liters; this.headline = headline; this.reasons = reasons; this.weatherIsDemo = weatherIsDemo;
    }
    public int getGardenId() { return gardenId; }
    public String getGardenName() { return gardenName; }
    public boolean isWaterNow() { return waterNow; }
    public double getLiters() { return liters; }
    public String getHeadline() { return headline; }
    public List<String> getReasons() { return reasons; }
    public boolean isWeatherIsDemo() { return weatherIsDemo; }
}
