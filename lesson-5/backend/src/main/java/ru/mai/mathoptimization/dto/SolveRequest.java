package ru.mai.mathoptimization.dto;

import java.util.List;

public class SolveRequest {

    private int variantId;
    private List<Double> x0;
    private List<Double> muValues;
    private String schedule = "INCREASING";
    private String domainMode = "ALL_CONSTRAINTS";
    private int experimentIndex;

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public List<Double> getX0() {
        return x0;
    }

    public void setX0(List<Double> x0) {
        this.x0 = x0;
    }

    public List<Double> getMuValues() {
        return muValues;
    }

    public void setMuValues(List<Double> muValues) {
        this.muValues = muValues;
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

    public int getExperimentIndex() {
        return experimentIndex;
    }

    public void setExperimentIndex(int experimentIndex) {
        this.experimentIndex = experimentIndex;
    }
}
