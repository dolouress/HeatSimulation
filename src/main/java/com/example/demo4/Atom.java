package com.example.demo4;

import javafx.scene.paint.Color;

public class Atom {
    private int x;
    private int y;
    private double temperature;
    private double prevTemperature;
    Color color;
    private double MIN_TEMP = 0;
    private double MAX_TEMP = 100;
    private Simulation simulation;

    public Atom(int x, int y, double temperature, Simulation simulation) {
        this.x = x;
        this.y = y;
        this.temperature = temperature;
        this.prevTemperature = temperature;
        this.color = getTemperatureColor(temperature);//Color.BLUE;
        this.simulation = simulation;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPrevTemperature(double prevTemperature) {
        this.prevTemperature = prevTemperature;
    }
    public Color getTemperatureColor(double temperature) {
        //FIX THIS TO better color gradient???????
        double tempPercent = (temperature - MIN_TEMP) / (MAX_TEMP - MIN_TEMP);
        int red = (int) (255 * tempPercent);
        int green = (int) (255 * (1 - tempPercent));
        int blue = 255 - red - green;
        Color c =  Color.rgb(red,blue,green);
        return c;
    }

    public double getPrevTemperature() {
        return prevTemperature;
    }
    public double getTemperature() {
        return temperature;
    }


    public void setTemperature(double temperature) {
        if (this.temperature != temperature) {
            this.temperature = temperature;
            this.color = getTemperatureColor(temperature);
            simulation.changedAtoms.add(this); // Add the atom to the changedAtoms list
        }
    }


    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
