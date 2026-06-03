package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class ContourDataResponse {

    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;
    private int gridSize;
    private double zMin;
    private double zMax;
    private final List<Double> xCoords = new ArrayList<Double>();
    private final List<Double> yCoords = new ArrayList<Double>();
    private final List<Double> values = new ArrayList<Double>();
    private final List<Double> levels = new ArrayList<Double>();
    private final List<double[]> feasiblePolygon = new ArrayList<double[]>();
    private final List<ConstraintBoundaryDto> constraintBoundaries = new ArrayList<ConstraintBoundaryDto>();

    public double getXMin() {
        return xMin;
    }

    public void setXMin(double xMin) {
        this.xMin = xMin;
    }

    public double getXMax() {
        return xMax;
    }

    public void setXMax(double xMax) {
        this.xMax = xMax;
    }

    public double getYMin() {
        return yMin;
    }

    public void setYMin(double yMin) {
        this.yMin = yMin;
    }

    public double getYMax() {
        return yMax;
    }

    public void setYMax(double yMax) {
        this.yMax = yMax;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public double getZMin() {
        return zMin;
    }

    public void setZMin(double zMin) {
        this.zMin = zMin;
    }

    public double getZMax() {
        return zMax;
    }

    public void setZMax(double zMax) {
        this.zMax = zMax;
    }

    public List<Double> getXCoords() {
        return xCoords;
    }

    public List<Double> getYCoords() {
        return yCoords;
    }

    public List<Double> getValues() {
        return values;
    }

    public List<Double> getLevels() {
        return levels;
    }

    public List<double[]> getFeasiblePolygon() {
        return feasiblePolygon;
    }

    public List<ConstraintBoundaryDto> getConstraintBoundaries() {
        return constraintBoundaries;
    }
}
