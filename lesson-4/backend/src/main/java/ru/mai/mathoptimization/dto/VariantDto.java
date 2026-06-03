package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class VariantDto {

    private int id;
    private String title;
    private String objectiveFormula;
    private boolean minimize;
    private boolean variant1Extended;
    private String taskDescription;
    private double plotXMin;
    private double plotXMax;
    private double plotYMin;
    private double plotYMax;
    private final List<ConstraintDto> constraints = new ArrayList<ConstraintDto>();

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

    public String getObjectiveFormula() {
        return objectiveFormula;
    }

    public void setObjectiveFormula(String objectiveFormula) {
        this.objectiveFormula = objectiveFormula;
    }

    public boolean isMinimize() {
        return minimize;
    }

    public void setMinimize(boolean minimize) {
        this.minimize = minimize;
    }

    public boolean isVariant1Extended() {
        return variant1Extended;
    }

    public void setVariant1Extended(boolean variant1Extended) {
        this.variant1Extended = variant1Extended;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
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

    public List<ConstraintDto> getConstraints() {
        return constraints;
    }
}
