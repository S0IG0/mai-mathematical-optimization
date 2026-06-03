package ru.mai.mathoptimization.problem;

public class LinearConstraint implements Constraint {

    private final String id;
    private final String label;
    private final ConstraintType type;
    private final double a;
    private final double b;
    private final double c;

    public LinearConstraint(String id, String label, ConstraintType type, double a, double b, double c) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /** g(x) = a*x1 + b*x2 + c &le; 0 */
    public static LinearConstraint le(String id, String label, double a, double b, double c) {
        return new LinearConstraint(id, label, ConstraintType.INEQUALITY, a, b, c);
    }

    public static LinearConstraint eq(String id, String label, double a, double b, double c) {
        return new LinearConstraint(id, label, ConstraintType.EQUALITY, a, b, c);
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
        return a * x1 + b * x2 + c;
    }

    @Override
    public double[] gradient(double x1, double x2) {
        return new double[]{a, b};
    }

    @Override
    public boolean isLinear() {
        return true;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }
}
