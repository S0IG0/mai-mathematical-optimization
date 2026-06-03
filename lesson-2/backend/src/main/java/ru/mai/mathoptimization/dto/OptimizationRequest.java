package ru.mai.mathoptimization.dto;

import java.util.List;

public class OptimizationRequest {

    private int variantId;
    private String functionId;
    private List<Double> x0;
    private double epsilon;
    private double delta;
    private boolean useOneDimensional;
    private boolean minimize = true;

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public List<Double> getX0() {
        return x0;
    }

    public void setX0(List<Double> x0) {
        this.x0 = x0;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public boolean isUseOneDimensional() {
        return useOneDimensional;
    }

    public void setUseOneDimensional(boolean useOneDimensional) {
        this.useOneDimensional = useOneDimensional;
    }

    public boolean isMinimize() {
        return minimize;
    }

    public void setMinimize(boolean minimize) {
        this.minimize = minimize;
    }
}
