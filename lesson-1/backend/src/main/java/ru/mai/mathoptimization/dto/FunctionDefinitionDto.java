package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class FunctionDefinitionDto {
    private String id;
    private String label;
    private String formula;
    private boolean defaultMinimize;
    private double domainFrom;
    private double domainTo;
    private List<IntervalPreset> presets = new ArrayList<IntervalPreset>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public boolean isDefaultMinimize() {
        return defaultMinimize;
    }

    public void setDefaultMinimize(boolean defaultMinimize) {
        this.defaultMinimize = defaultMinimize;
    }

    public double getDomainFrom() {
        return domainFrom;
    }

    public void setDomainFrom(double domainFrom) {
        this.domainFrom = domainFrom;
    }

    public double getDomainTo() {
        return domainTo;
    }

    public void setDomainTo(double domainTo) {
        this.domainTo = domainTo;
    }

    public List<IntervalPreset> getPresets() {
        return presets;
    }

    public void setPresets(List<IntervalPreset> presets) {
        this.presets = presets;
    }
}
