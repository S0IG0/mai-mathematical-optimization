package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class MuStepDto {

    private int k;
    private double mu;
    private List<Double> x;
    private double f;
    private double alphaOrB;
    private double theta;
    private double muTimesAux;
    private List<PathPointDto> innerPath = new ArrayList<PathPointDto>();

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public double getMu() {
        return mu;
    }

    public void setMu(double mu) {
        this.mu = mu;
    }

    public List<Double> getX() {
        return x;
    }

    public void setX(List<Double> x) {
        this.x = x;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public double getAlphaOrB() {
        return alphaOrB;
    }

    public void setAlphaOrB(double alphaOrB) {
        this.alphaOrB = alphaOrB;
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getMuTimesAux() {
        return muTimesAux;
    }

    public void setMuTimesAux(double muTimesAux) {
        this.muTimesAux = muTimesAux;
    }

    public List<PathPointDto> getInnerPath() {
        return innerPath;
    }

    public void setInnerPath(List<PathPointDto> innerPath) {
        this.innerPath = innerPath;
    }
}
