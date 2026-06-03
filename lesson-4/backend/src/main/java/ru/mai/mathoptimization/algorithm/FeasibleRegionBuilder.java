package ru.mai.mathoptimization.algorithm;

import ru.mai.mathoptimization.problem.Constraint;
import ru.mai.mathoptimization.problem.ConstraintType;
import ru.mai.mathoptimization.problem.LinearConstraint;
import ru.mai.mathoptimization.problem.ProblemDefinition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Builds a convex polygon approximation of the linear-feasible region (2D).
 */
public final class FeasibleRegionBuilder {

    private FeasibleRegionBuilder() {
    }

    public static List<double[]> buildPolygon(ProblemDefinition problem) {
        Set<String> seen = new HashSet<String>();
        List<double[]> vertices = new ArrayList<double[]>();

        List<LinearConstraint> lines = new ArrayList<LinearConstraint>();
        for (Constraint c : problem.getConstraints()) {
            if (c instanceof LinearConstraint) {
                lines.add((LinearConstraint) c);
            }
        }

        double pad = 0.5;
        double xmin = problem.getPlotXMin() - pad;
        double xmax = problem.getPlotXMax() + pad;
        double ymin = problem.getPlotYMin() - pad;
        double ymax = problem.getPlotYMax() + pad;
        lines.add(LinearConstraint.le("bx0", "", -1, 0, xmin));
        lines.add(LinearConstraint.le("bx1", "", 1, 0, -xmax));
        lines.add(LinearConstraint.le("by0", "", 0, -1, ymin));
        lines.add(LinearConstraint.le("by1", "", 0, 1, -ymax));

        for (int i = 0; i < lines.size(); i++) {
            for (int j = i + 1; j < lines.size(); j++) {
                double[] p = intersect(lines.get(i), lines.get(j));
                if (p != null && isFeasible(problem, p[0], p[1], 1e-5)) {
                    add(vertices, seen, p[0], p[1]);
                }
            }
        }

        if (vertices.size() < 3) {
            return vertices;
        }

        double cx = 0;
        double cy = 0;
        for (double[] v : vertices) {
            cx += v[0];
            cy += v[1];
        }
        cx /= vertices.size();
        cy /= vertices.size();
        final double fcx = cx;
        final double fcy = cy;
        vertices.sort(Comparator.comparingDouble(v -> Math.atan2(v[1] - fcy, v[0] - fcx)));
        return vertices;
    }

    private static double[] intersect(LinearConstraint l1, LinearConstraint l2) {
        double det = l1.getA() * l2.getB() - l2.getA() * l1.getB();
        if (Math.abs(det) < 1e-10) {
            return null;
        }
        double x1 = (l1.getB() * l2.getC() - l2.getB() * l1.getC()) / det;
        double x2 = (l2.getA() * l1.getC() - l1.getA() * l2.getC()) / det;
        return new double[]{x1, x2};
    }

    private static boolean isFeasible(ProblemDefinition problem, double x1, double x2, double tol) {
        for (Constraint c : problem.getConstraints()) {
            if (!c.isLinear()) {
                double v = c.value(x1, x2);
                if (c.getType() == ConstraintType.EQUALITY) {
                    if (Math.abs(v) > tol) {
                        return false;
                    }
                } else if (v > tol) {
                    return false;
                }
            } else {
                double v = c.value(x1, x2);
                if (c.getType() == ConstraintType.EQUALITY) {
                    if (Math.abs(v) > tol) {
                        return false;
                    }
                } else if (v > tol) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void add(List<double[]> out, Set<String> seen, double x1, double x2) {
        String key = String.format(Locale.US, "%.4f|%.4f", x1, x2);
        if (seen.add(key)) {
            out.add(new double[]{x1, x2});
        }
    }
}
