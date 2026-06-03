package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class CandidatePointDto {

    private PointDto point;
    private double objectiveValue;
    private boolean feasible;
    private boolean kktSatisfied;
    private PointDto gradient;
    private double[] multipliers;
    private String description;
    private final List<ConstraintStatusDto> constraints = new ArrayList<ConstraintStatusDto>();

    public PointDto getPoint() {
        return point;
    }

    public void setPoint(PointDto point) {
        this.point = point;
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    public void setObjectiveValue(double objectiveValue) {
        this.objectiveValue = objectiveValue;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public void setFeasible(boolean feasible) {
        this.feasible = feasible;
    }

    public boolean isKktSatisfied() {
        return kktSatisfied;
    }

    public void setKktSatisfied(boolean kktSatisfied) {
        this.kktSatisfied = kktSatisfied;
    }

    public PointDto getGradient() {
        return gradient;
    }

    public void setGradient(PointDto gradient) {
        this.gradient = gradient;
    }

    public double[] getMultipliers() {
        return multipliers;
    }

    public void setMultipliers(double[] multipliers) {
        this.multipliers = multipliers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ConstraintStatusDto> getConstraints() {
        return constraints;
    }
}
