package ru.mai.mathoptimization.algorithm;

import ru.mai.mathoptimization.dto.IterationDto;
import ru.mai.mathoptimization.dto.PathPointDto;

public interface OptimizationProgressListener {

    void onPathPoint(PathPointDto point);

    void onIteration(IterationDto iteration);
}
