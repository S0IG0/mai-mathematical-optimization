package ru.mai.mathoptimization.problem;

public interface ObjectiveFunction {

    double value(double x1, double x2);

    double[] gradient(double x1, double x2);

    /** Optional Hessian for convexity notes; may return null. */
    default double[][] hessian(double x1, double x2) {
        return null;
    }
}
