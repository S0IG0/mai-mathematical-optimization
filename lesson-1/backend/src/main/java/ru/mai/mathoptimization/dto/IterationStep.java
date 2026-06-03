package ru.mai.mathoptimization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IterationStep {
    private int k;
    private double a;
    private double b;
    private Double lambda;
    private Double mu;
    private Double fLambda;
    private Double fMu;
    private Double fA;
    private Double fB;

    public IterationStep() {
    }

    public IterationStep(int k, double a, double b, Double lambda, Double mu,
                         Double fLambda, Double fMu, Double fA, Double fB) {
        this.k = k;
        this.a = a;
        this.b = b;
        this.lambda = lambda;
        this.mu = mu;
        this.fLambda = fLambda;
        this.fMu = fMu;
        this.fA = fA;
        this.fB = fB;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public Double getLambda() {
        return lambda;
    }

    public void setLambda(Double lambda) {
        this.lambda = lambda;
    }

    public Double getMu() {
        return mu;
    }

    public void setMu(Double mu) {
        this.mu = mu;
    }

    @JsonProperty("fLambda")
    public Double getFLambda() {
        return fLambda;
    }

    public void setFLambda(Double fLambda) {
        this.fLambda = fLambda;
    }

    @JsonProperty("fMu")
    public Double getFMu() {
        return fMu;
    }

    public void setFMu(Double fMu) {
        this.fMu = fMu;
    }

    @JsonProperty("fA")
    public Double getFA() {
        return fA;
    }

    public void setFA(Double fA) {
        this.fA = fA;
    }

    @JsonProperty("fB")
    public Double getFB() {
        return fB;
    }

    public void setFB(Double fB) {
        this.fB = fB;
    }
}
