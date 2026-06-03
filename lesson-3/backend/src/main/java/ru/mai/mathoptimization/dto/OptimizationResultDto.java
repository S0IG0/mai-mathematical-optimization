package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class OptimizationResultDto {

    private String method;
    private String methodLabel;
    private boolean minimize;
    private List<Double> optimalX;
    private double optimalF;
    private int iterationsCount;
    private int functionEvaluations;
    private List<IterationDto> iterations = new ArrayList<IterationDto>();
    private List<PathPointDto> path = new ArrayList<PathPointDto>();
    private boolean diverged;
    private String statusMessage;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public void setMethodLabel(String methodLabel) {
        this.methodLabel = methodLabel;
    }

    public boolean isMinimize() {
        return minimize;
    }

    public void setMinimize(boolean minimize) {
        this.minimize = minimize;
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

    public int getIterationsCount() {
        return iterationsCount;
    }

    public void setIterationsCount(int iterationsCount) {
        this.iterationsCount = iterationsCount;
    }

    public int getFunctionEvaluations() {
        return functionEvaluations;
    }

    public void setFunctionEvaluations(int functionEvaluations) {
        this.functionEvaluations = functionEvaluations;
    }

    public List<IterationDto> getIterations() {
        return iterations;
    }

    public void setIterations(List<IterationDto> iterations) {
        this.iterations = iterations;
    }

    public List<PathPointDto> getPath() {
        return path;
    }

    public void setPath(List<PathPointDto> path) {
        this.path = path;
    }

    public boolean isDiverged() {
        return diverged;
    }

    public void setDiverged(boolean diverged) {
        this.diverged = diverged;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
