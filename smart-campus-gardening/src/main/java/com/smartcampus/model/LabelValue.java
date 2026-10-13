package com.smartcampus.model;

/** Simple label/number pair used for chart data and grouped reports. */
public class LabelValue {
    private final String label;
    private final double value;
    public LabelValue(String label, double value) { this.label = label; this.value = value; }
    public String getLabel() { return label; }
    public double getValue() { return value; }
}
