package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class PlotDataResponse {
    private List<PlotPoint> points = new ArrayList<PlotPoint>();
    private double plotFrom;
    private double plotTo;

    public List<PlotPoint> getPoints() {
        return points;
    }

    public void setPoints(List<PlotPoint> points) {
        this.points = points;
    }

    public double getPlotFrom() {
        return plotFrom;
    }

    public void setPlotFrom(double plotFrom) {
        this.plotFrom = plotFrom;
    }

    public double getPlotTo() {
        return plotTo;
    }

    public void setPlotTo(double plotTo) {
        this.plotTo = plotTo;
    }
}
