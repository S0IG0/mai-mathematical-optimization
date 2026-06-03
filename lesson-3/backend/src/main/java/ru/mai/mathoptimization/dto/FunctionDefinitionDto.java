package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class FunctionDefinitionDto {

    private String id;
    private String label;
    private String formula;
    private int dimension;
    private boolean plottable2d;
    private double plotXMin;
    private double plotXMax;
    private double plotYMin;
    private double plotYMax;
    private String method;
    private String methodLabel;
    private boolean minimize;
    private List<InitialPointPreset> initialPoints = new ArrayList<InitialPointPreset>();

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

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public boolean isPlottable2d() {
        return plottable2d;
    }

    public void setPlottable2d(boolean plottable2d) {
        this.plottable2d = plottable2d;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public void setMethodLabel(String methodLabel) {
        this.methodLabel = methodLabel;
    }

    public boolean isMinimize() {
        return minimize;
    }

    public void setMinimize(boolean minimize) {
        this.minimize = minimize;
    }

    public List<InitialPointPreset> getInitialPoints() {
        return initialPoints;
    }

    public void setInitialPoints(List<InitialPointPreset> initialPoints) {
        this.initialPoints = initialPoints;
    }
}
