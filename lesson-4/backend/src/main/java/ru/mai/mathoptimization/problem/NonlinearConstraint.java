package ru.mai.mathoptimization.problem;

import java.util.function.BiFunction;
import java.util.function.ToDoubleBiFunction;

public class NonlinearConstraint implements Constraint {

    private final String id;
    private final String label;
    private final ConstraintType type;
    private final ToDoubleBiFunction<Double, Double> valueFn;
    private final BiFunction<Double, Double, double[]> gradFn;

    public NonlinearConstraint(String id, String label, ConstraintType type,
                               ToDoubleBiFunction<Double, Double> valueFn,
                               BiFunction<Double, Double, double[]> gradFn) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.valueFn = valueFn;
        this.gradFn = gradFn;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public ConstraintType getType() {
        return type;
    }

    @Override
    public double value(double x1, double x2) {
        return valueFn.applyAsDouble(x1, x2);
    }

    @Override
    public double[] gradient(double x1, double x2) {
        return gradFn.apply(x1, x2);
    }

    @Override
    public boolean isLinear() {
        return false;
    }
}
