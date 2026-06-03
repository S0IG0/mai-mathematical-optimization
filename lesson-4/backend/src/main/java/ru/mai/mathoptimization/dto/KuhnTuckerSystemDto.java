package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class KuhnTuckerSystemDto {

    private String lagrangian;
    private String stationarity;
    private String feasibility;
    private String complementarity;
    private final List<String> constraintForms = new ArrayList<String>();

    public String getLagrangian() {
        return lagrangian;
    }

    public void setLagrangian(String lagrangian) {
        this.lagrangian = lagrangian;
    }

    public String getStationarity() {
        return stationarity;
    }

    public void setStationarity(String stationarity) {
        this.stationarity = stationarity;
    }

    public String getFeasibility() {
        return feasibility;
    }

    public void setFeasibility(String feasibility) {
        this.feasibility = feasibility;
    }

    public String getComplementarity() {
        return complementarity;
    }

    public void setComplementarity(String complementarity) {
        this.complementarity = complementarity;
    }

    public List<String> getConstraintForms() {
        return constraintForms;
    }
}
