package ru.mai.mathoptimization.dto;

public class ExperimentPresetDto {

    private String label;
    private double[] muValues;
    private String schedule;
    private String domainMode;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double[] getMuValues() {
        return muValues;
    }

    public void setMuValues(double[] muValues) {
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
}
