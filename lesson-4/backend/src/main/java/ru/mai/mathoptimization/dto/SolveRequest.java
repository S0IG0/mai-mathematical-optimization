package ru.mai.mathoptimization.dto;

public class SolveRequest {

    private int variantId;
    private boolean includeUnconstrainedPart;
    private double unconstrainedX0 = 0;
    private double unconstrainedY0 = 0;

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public boolean isIncludeUnconstrainedPart() {
        return includeUnconstrainedPart;
    }

    public void setIncludeUnconstrainedPart(boolean includeUnconstrainedPart) {
        this.includeUnconstrainedPart = includeUnconstrainedPart;
    }

    public double getUnconstrainedX0() {
        return unconstrainedX0;
    }

    public void setUnconstrainedX0(double unconstrainedX0) {
        this.unconstrainedX0 = unconstrainedX0;
    }

    public double getUnconstrainedY0() {
        return unconstrainedY0;
    }

    public void setUnconstrainedY0(double unconstrainedY0) {
        this.unconstrainedY0 = unconstrainedY0;
    }
}
