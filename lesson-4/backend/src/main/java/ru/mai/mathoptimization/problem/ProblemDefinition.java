package ru.mai.mathoptimization.problem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProblemDefinition {

    private final int id;
    private final String title;
    private final String objectiveFormula;
    private final boolean minimize;
    private final ObjectiveFunction objective;
    private final List<Constraint> constraints;
    private final double plotXMin;
    private final double plotXMax;
    private final double plotYMin;
    private final double plotYMax;
    private final boolean variant1Extended;
    private final String taskDescription;

    public ProblemDefinition(int id, String title, String objectiveFormula, boolean minimize,
                             ObjectiveFunction objective, List<Constraint> constraints,
                             double plotXMin, double plotXMax, double plotYMin, double plotYMax,
                             boolean variant1Extended, String taskDescription) {
        this.id = id;
        this.title = title;
        this.objectiveFormula = objectiveFormula;
        this.minimize = minimize;
        this.objective = objective;
        this.constraints = Collections.unmodifiableList(new ArrayList<Constraint>(constraints));
        this.plotXMin = plotXMin;
        this.plotXMax = plotXMax;
        this.plotYMin = plotYMin;
        this.plotYMax = plotYMax;
        this.variant1Extended = variant1Extended;
        this.taskDescription = taskDescription;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getObjectiveFormula() {
        return objectiveFormula;
    }

    public boolean isMinimize() {
        return minimize;
    }

    public ObjectiveFunction getObjective() {
        return objective;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public double getPlotXMin() {
        return plotXMin;
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

    public boolean isVariant1Extended() {
        return variant1Extended;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public double evalF(double x1, double x2) {
        return objective.value(x1, x2);
    }

    /** Signed objective for KKT: minimize f, maximize -f. */
    public double evalSignedF(double x1, double x2) {
        double v = objective.value(x1, x2);
        return minimize ? v : -v;
    }

    public double[] gradSignedF(double x1, double x2) {
        double[] g = objective.gradient(x1, x2);
        if (!minimize) {
            return new double[]{-g[0], -g[1]};
        }
        return g;
    }
}
