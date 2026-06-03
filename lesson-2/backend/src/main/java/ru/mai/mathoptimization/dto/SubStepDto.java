package ru.mai.mathoptimization.dto;

import java.util.List;

public class SubStepDto {

    private int j;
    private List<Double> dj;
    private List<Double> yj;
    private Double fYj;
    private Double deltaJ;
    private Double lambdaJ;
    private List<Double> yjPlus;
    private Double fYjPlus;
    private List<Double> yjMinus;
    private Double fYjMinus;

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public List<Double> getDj() {
        return dj;
    }

    public void setDj(List<Double> dj) {
        this.dj = dj;
    }

    public List<Double> getYj() {
        return yj;
    }

    public void setYj(List<Double> yj) {
        this.yj = yj;
    }

    public Double getFYj() {
        return fYj;
    }

    public void setFYj(Double fYj) {
        this.fYj = fYj;
    }

    public Double getDeltaJ() {
        return deltaJ;
    }

    public void setDeltaJ(Double deltaJ) {
        this.deltaJ = deltaJ;
    }

    public Double getLambdaJ() {
        return lambdaJ;
    }

    public void setLambdaJ(Double lambdaJ) {
        this.lambdaJ = lambdaJ;
    }

    public List<Double> getYjPlus() {
        return yjPlus;
    }

    public void setYjPlus(List<Double> yjPlus) {
        this.yjPlus = yjPlus;
    }

    public Double getFYjPlus() {
        return fYjPlus;
    }

    public void setFYjPlus(Double fYjPlus) {
        this.fYjPlus = fYjPlus;
    }

    public List<Double> getYjMinus() {
        return yjMinus;
    }

    public void setYjMinus(List<Double> yjMinus) {
        this.yjMinus = yjMinus;
    }

    public Double getFYjMinus() {
        return fYjMinus;
    }

    public void setFYjMinus(Double fYjMinus) {
        this.fYjMinus = fYjMinus;
    }
}
