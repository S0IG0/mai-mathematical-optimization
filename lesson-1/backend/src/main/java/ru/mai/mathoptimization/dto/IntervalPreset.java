package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class IntervalPreset {
    private String label;
    private double a;
    private double b;
    private boolean minimize;

    public IntervalPreset() {
    }

    public IntervalPreset(String label, double a, double b, boolean minimize) {
        this.label = label;
        this.a = a;
        this.b = b;
        this.minimize = minimize;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public boolean isMinimize() {
        return minimize;
    }

    public void setMinimize(boolean minimize) {
        this.minimize = minimize;
    }
}
