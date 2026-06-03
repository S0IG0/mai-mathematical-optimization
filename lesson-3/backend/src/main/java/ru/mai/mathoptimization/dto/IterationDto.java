package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class IterationDto {

    private int k;
    private List<Double> xk;
    private double fXk;
    private List<SubStepDto> subSteps = new ArrayList<SubStepDto>();

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public List<Double> getXk() {
        return xk;
    }

    public void setXk(List<Double> xk) {
        this.xk = xk;
    }

    public double getFXk() {
        return fXk;
    }

    public void setFXk(double fXk) {
        this.fXk = fXk;
    }

    public List<SubStepDto> getSubSteps() {
        return subSteps;
    }

    public void setSubSteps(List<SubStepDto> subSteps) {
        this.subSteps = subSteps;
    }
}
