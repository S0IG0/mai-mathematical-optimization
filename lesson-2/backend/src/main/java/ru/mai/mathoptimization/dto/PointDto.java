package ru.mai.mathoptimization.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PointDto {

    private List<Double> coordinates;

    public PointDto() {
    }

    public PointDto(double[] coords) {
        this.coordinates = Arrays.stream(coords).boxed().collect(Collectors.toList());
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }
}
