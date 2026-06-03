package ru.mai.mathoptimization.dto;

import java.util.List;

public class PathPointDto {

    private List<Double> x;
    private double f;
    private int k;

    public PathPointDto() {
    }

    public PathPointDto(double[] coords, double f, int k) {
        this.x = new PointDto(coords).getCoordinates();
        this.f = f;
        this.k = k;
    }

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

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
