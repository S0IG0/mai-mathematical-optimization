package ru.mai.mathoptimization.problem;

import java.util.ArrayList;
import java.util.List;

public class ProblemDefinition {

    private final int id;
    private final MethodKind methodKind;
    private final OptimizerKind optimizerKind;
    private final String objectiveFormula;
    private final ObjectiveEvaluator objective;
    private final List<InequalityConstraint> inequalities = new ArrayList<InequalityConstraint>();
    private final List<EqualityConstraint> equalities = new ArrayList<EqualityConstraint>();
    private double plotXMin = -1;
    private double plotXMax = 6;
    private double plotYMin = -1;
    private double plotYMax = 6;
    private boolean boundedBox;
    private double boxXMin = -5;
    private double boxXMax = 5;
    private double boxYMin = -5;
    private double boxYMax = 5;

    public ProblemDefinition(int id, MethodKind methodKind, OptimizerKind optimizerKind,
                           String objectiveFormula, ObjectiveEvaluator objective) {
        this.id = id;
        this.methodKind = methodKind;
        this.optimizerKind = optimizerKind;
        this.objectiveFormula = objectiveFormula;
        this.objective = objective;
    }

    public int getId() {
        return id;
    }

    public MethodKind getMethodKind() {
        return methodKind;
    }

    public OptimizerKind getOptimizerKind() {
        return optimizerKind;
    }

    public String getObjectiveFormula() {
        return objectiveFormula;
    }

    public double evalF(double x1, double x2) {
        return objective.eval(x1, x2);
    }

    public List<InequalityConstraint> getInequalities() {
        return inequalities;
    }

    public List<EqualityConstraint> getEqualities() {
        return equalities;
    }

    public double getPlotXMin() {
        return plotXMin;
    }

    public ProblemDefinition plotBounds(double xMin, double xMax, double yMin, double yMax) {
        this.plotXMin = xMin;
        this.plotXMax = xMax;
        this.plotYMin = yMin;
        this.plotYMax = yMax;
        return this;
    }

    public double getPlotXMax() {
        return plotXMax;
    }

    public double getPlotYMin() {
        return plotYMin;
    }

    public double getPlotYMax() {
        return plotYMax;
    }

    public ProblemDefinition boundedBox(double xMin, double xMax, double yMin, double yMax) {
        this.boundedBox = true;
        this.boxXMin = xMin;
        this.boxXMax = xMax;
        this.boxYMin = yMin;
        this.boxYMax = yMax;
        return this;
    }

    public boolean isBoundedBox() {
        return boundedBox;
    }

    public double getBoxXMin() {
        return boxXMin;
    }

    public double getBoxXMax() {
        return boxXMax;
    }

    public double getBoxYMin() {
        return boxYMin;
    }

    public double getBoxYMax() {
        return boxYMax;
    }

    public ProblemDefinition addIneq(String label, String formula, InequalityConstraint.ConstraintEvaluator ev) {
        inequalities.add(new InequalityConstraint(label, formula, ev));
        return this;
    }

    public ProblemDefinition addEq(String label, String formula, EqualityConstraint.ConstraintEvaluator ev) {
        equalities.add(new EqualityConstraint(label, formula, ev));
        return this;
    }

    @FunctionalInterface
    public interface ObjectiveEvaluator {
        double eval(double x1, double x2);
    }
}
