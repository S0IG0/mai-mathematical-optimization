package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class VariantDto {

    private int id;
    private String title;
    private String methodKind;
    private String methodLabel;
    private String optimizerKind;
    private String objectiveFormula;
    private double plotXMin;
    private double plotXMax;
    private double plotYMin;
    private double plotYMax;
    private boolean boundedBox;
    private List<String> inequalities = new ArrayList<String>();
    private List<String> equalities = new ArrayList<String>();
    private List<InitialPointPreset> initialPoints = new ArrayList<InitialPointPreset>();
    private List<ExperimentPresetDto> experiments = new ArrayList<ExperimentPresetDto>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMethodKind() {
        return methodKind;
    }

    public void setMethodKind(String methodKind) {
        this.methodKind = methodKind;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public void setMethodLabel(String methodLabel) {
        this.methodLabel = methodLabel;
    }

    public String getOptimizerKind() {
        return optimizerKind;
    }

    public void setOptimizerKind(String optimizerKind) {
        this.optimizerKind = optimizerKind;
    }

    public String getObjectiveFormula() {
        return objectiveFormula;
    }

    public void setObjectiveFormula(String objectiveFormula) {
        this.objectiveFormula = objectiveFormula;
    }

    public double getPlotXMin() {
        return plotXMin;
    }

    public void setPlotXMin(double plotXMin) {
        this.plotXMin = plotXMin;
    }

    public double getPlotXMax() {
        return plotXMax;
    }

    public void setPlotXMax(double plotXMax) {
        this.plotXMax = plotXMax;
    }

    public double getPlotYMin() {
        return plotYMin;
    }

    public void setPlotYMin(double plotYMin) {
        this.plotYMin = plotYMin;
    }

    public double getPlotYMax() {
        return plotYMax;
    }

    public void setPlotYMax(double plotYMax) {
        this.plotYMax = plotYMax;
    }

    public boolean isBoundedBox() {
        return boundedBox;
    }

    public void setBoundedBox(boolean boundedBox) {
        this.boundedBox = boundedBox;
    }

    public List<String> getInequalities() {
        return inequalities;
    }

    public List<String> getEqualities() {
        return equalities;
    }

    public List<InitialPointPreset> getInitialPoints() {
        return initialPoints;
    }

    public List<ExperimentPresetDto> getExperiments() {
        return experiments;
    }
}
