package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class OptimizationResult {
    private String method;
    private String methodLabel;
    private boolean minimize;
    private double initialA;
    private double initialB;
    private double finalA;
    private double finalB;
    private double optimalX;
    private double optimalF;
    private int functionEvaluations;
    private int iterationsCount;
    private List<IterationStep> iterations = new ArrayList<IterationStep>();

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

    public double getInitialA() {
        return initialA;
    }

    public void setInitialA(double initialA) {
        this.initialA = initialA;
    }

    public double getInitialB() {
        return initialB;
    }

    public void setInitialB(double initialB) {
        this.initialB = initialB;
    }

    public double getFinalA() {
        return finalA;
    }

    public void setFinalA(double finalA) {
        this.finalA = finalA;
    }

    public double getFinalB() {
        return finalB;
    }

    public void setFinalB(double finalB) {
        this.finalB = finalB;
    }

    public double getOptimalX() {
        return optimalX;
    }

    public void setOptimalX(double optimalX) {
        this.optimalX = optimalX;
    }

    public double getOptimalF() {
        return optimalF;
    }

    public void setOptimalF(double optimalF) {
        this.optimalF = optimalF;
    }

    public int getFunctionEvaluations() {
        return functionEvaluations;
    }

    public void setFunctionEvaluations(int functionEvaluations) {
        this.functionEvaluations = functionEvaluations;
    }

    public int getIterationsCount() {
        return iterationsCount;
    }

    public void setIterationsCount(int iterationsCount) {
        this.iterationsCount = iterationsCount;
    }

    public List<IterationStep> getIterations() {
        return iterations;
    }

    public void setIterations(List<IterationStep> iterations) {
        this.iterations = iterations;
    }
}
