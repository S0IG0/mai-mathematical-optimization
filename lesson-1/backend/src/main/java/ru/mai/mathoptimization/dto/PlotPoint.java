package ru.mai.mathoptimization.dto;

public class PlotPoint {
    private double x;
    private Double y;

    public PlotPoint() {
    }

    public PlotPoint(double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
