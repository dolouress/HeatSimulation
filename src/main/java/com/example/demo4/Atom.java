package com.example.demo4;

import javafx.scene.paint.Color;

public class Atom {
    private double temperature;
    private double prevTemperature;
    private Color color;

    public Atom(double temperature) {
        this.temperature = temperature;
        this.prevTemperature = temperature;
        this.color = getTemperatureColor(temperature);
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPrevTemperature() {
        return prevTemperature;
    }

    public void setPrevTemperature(double prevTemperature) {
        this.prevTemperature = prevTemperature;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getTemperatureColor(double temperature) {
        double hue;
        if (temperature >= 95) {
            hue = map(temperature, 80, 100, 0, 30); // Map higher temperatures to a narrower red range
        } else {
            hue = map(temperature, 0, 50, 255, 0); // Map lower temperatures to the full blue to red range
        }
        return Color.hsb(hue, 1, 1);
    }

    private double map(double value, double inMin, double inMax, double outMin, double outMax) {
        double t = (value - inMin) / (inMax - inMin);
        t = Math.max(0, Math.min(1, t)); // Clamp t between 0 and 1
        // Apply a non-linear mapping to emphasize red in the middle and at the beginning
        double mappedValue = t * t * outMax + (1 - t * t) * outMin;
        return mappedValue;
    }


}