package com.smartcampus.model;

import java.sql.Timestamp;

public class WeatherRecord {
    private int weatherId;
    private String campusLocation;
    private double temperatureC;
    private String conditions;
    private int humidity;
    private double rainMmCurrent;
    private double rainMmForecast24h;
    private String forecastSummary;
    private boolean demo;
    private Timestamp retrievedAt;

    public int getWeatherId() { return weatherId; }
    public void setWeatherId(int weatherId) { this.weatherId = weatherId; }
    public String getCampusLocation() { return campusLocation; }
    public void setCampusLocation(String campusLocation) { this.campusLocation = campusLocation; }
    public double getTemperatureC() { return temperatureC; }
    public void setTemperatureC(double temperatureC) { this.temperatureC = temperatureC; }
    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public double getRainMmCurrent() { return rainMmCurrent; }
    public void setRainMmCurrent(double rainMmCurrent) { this.rainMmCurrent = rainMmCurrent; }
    public double getRainMmForecast24h() { return rainMmForecast24h; }
    public void setRainMmForecast24h(double rainMmForecast24h) { this.rainMmForecast24h = rainMmForecast24h; }
    public String getForecastSummary() { return forecastSummary; }
    public void setForecastSummary(String forecastSummary) { this.forecastSummary = forecastSummary; }
    public boolean isDemo() { return demo; }
    public void setDemo(boolean demo) { this.demo = demo; }
    public Timestamp getRetrievedAt() { return retrievedAt; }
    public void setRetrievedAt(Timestamp retrievedAt) { this.retrievedAt = retrievedAt; }
}
