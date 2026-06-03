package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class SolveResultDto {

    private int variantId;
    private String methodKind;
    private String schedule;
    private String domainMode;
    private List<Double> optimalX;
    private double optimalF;
    private double penaltyOrBarrierAtOpt;
    private boolean feasible;
    private double maxViolation;
    private String conclusion;
    private List<String> constraintViolations = new ArrayList<String>();
    private List<MuStepDto> steps = new ArrayList<MuStepDto>();
    private List<PathPointDto> path = new ArrayList<PathPointDto>();

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public String getMethodKind() {
        return methodKind;
    }

    public void setMethodKind(String methodKind) {
        this.methodKind = methodKind;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getDomainMode() {
        return domainMode;
    }

    public void setDomainMode(String domainMode) {
        this.domainMode = domainMode;
    }

    public List<Double> getOptimalX() {
        return optimalX;
    }

    public void setOptimalX(List<Double> optimalX) {
        this.optimalX = optimalX;
    }

    public double getOptimalF() {
        return optimalF;
    }

    public void setOptimalF(double optimalF) {
        this.optimalF = optimalF;
    }

    public double getPenaltyOrBarrierAtOpt() {
        return penaltyOrBarrierAtOpt;
    }

    public void setPenaltyOrBarrierAtOpt(double penaltyOrBarrierAtOpt) {
        this.penaltyOrBarrierAtOpt = penaltyOrBarrierAtOpt;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public void setFeasible(boolean feasible) {
        this.feasible = feasible;
    }

    public double getMaxViolation() {
        return maxViolation;
    }

    public void setMaxViolation(double maxViolation) {
        this.maxViolation = maxViolation;
    }

    public List<String> getConstraintViolations() {
        return constraintViolations;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public List<MuStepDto> getSteps() {
        return steps;
    }

    public List<PathPointDto> getPath() {
        return path;
    }
}
