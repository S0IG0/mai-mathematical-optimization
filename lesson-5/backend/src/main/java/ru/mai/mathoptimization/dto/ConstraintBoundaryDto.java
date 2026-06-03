package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class ConstraintBoundaryDto {

    private String label;
    private List<Double> x = new ArrayList<Double>();
    private List<Double> y = new ArrayList<Double>();

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Double> getX() {
        return x;
    }

    public void setX(List<Double> x) {
        this.x = x;
    }

    public List<Double> getY() {
        return y;
    }

    public void setY(List<Double> y) {
        this.y = y;
    }
}
