package ru.mai.mathoptimization.problem;

/**
 * Constraint in standard form g(x) &le; 0 (inequalities) or h(x) = 0 (equalities).
 */
public interface Constraint {

    String getId();

    String getLabel();

    ConstraintType getType();

    /** g(x) for inequalities (should be &le; 0 when feasible) or h(x) for equalities. */
    double value(double x1, double x2);

    /** Gradient [&part;g/&part;x1, &part;g/&part;x2]. */
    double[] gradient(double x1, double x2);

    boolean isLinear();
}
