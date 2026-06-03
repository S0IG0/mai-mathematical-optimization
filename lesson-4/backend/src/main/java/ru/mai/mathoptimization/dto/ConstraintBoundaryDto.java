package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class ConstraintBoundaryDto {

    private String id;
    private String label;
    private final List<Double> x = new ArrayList<Double>();
    private final List<Double> y = new ArrayList<Double>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Double> getX() {
        return x;
    }

    public List<Double> getY() {
        return y;
    }
}
