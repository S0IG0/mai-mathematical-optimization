package ru.mai.mathoptimization.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InitialPointPreset {

    private String label;
    private List<Double> coordinates;

    public InitialPointPreset() {
    }

    public InitialPointPreset(String label, double[] coords) {
        this.label = label;
        this.coordinates = Arrays.stream(coords).boxed().collect(Collectors.toList());
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }
}
