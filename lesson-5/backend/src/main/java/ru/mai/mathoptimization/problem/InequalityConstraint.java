package ru.mai.mathoptimization.problem;

public class InequalityConstraint {

    private final String label;
    private final String formula;
    /** g(x) <= 0 */
    private final ConstraintEvaluator evaluator;

    public InequalityConstraint(String label, String formula, ConstraintEvaluator evaluator) {
        this.label = label;
        this.formula = formula;
        this.evaluator = evaluator;
    }

    public String getLabel() {
        return label;
    }

    public String getFormula() {
        return formula;
    }

    public double eval(double x1, double x2) {
        return evaluator.eval(x1, x2);
    }

    @FunctionalInterface
    public interface ConstraintEvaluator {
        double eval(double x1, double x2);
    }
}
