package ru.mai.mathoptimization.dto;

import java.util.List;

public class PathPointDto {

    private List<Double> x;
    private double f;

    public List<Double> getX() {
        return x;
    }

    public void setX(List<Double> x) {
        this.x = x;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }
}
