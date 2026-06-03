package ru.mai.mathoptimization.function;

@FunctionalInterface
public interface ObjectiveFunction {
    double apply(double x);
}
