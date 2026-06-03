package ru.mai.mathoptimization.algorithm;

import ru.mai.mathoptimization.dto.ConstraintBoundaryDto;
import ru.mai.mathoptimization.problem.EqualityConstraint;
import ru.mai.mathoptimization.problem.InequalityConstraint;
import ru.mai.mathoptimization.problem.ProblemDefinition;

import java.util.ArrayList;
import java.util.List;

/** Построение упорядоченных линий границ g(x)=0 для Plotly (без «паутины» из тысяч точек). */
public final class ConstraintCurveBuilder {

    private ConstraintCurveBuilder() {
    }

    public static List<ConstraintBoundaryDto> build(ProblemDefinition problem, int points) {
        return build(problem, points, problem.getPlotXMin(), problem.getPlotXMax(),
                problem.getPlotYMin(), problem.getPlotYMax());
    }

    public static List<ConstraintBoundaryDto> build(ProblemDefinition problem, int points,
                                                    double xMin, double xMax, double yMin, double yMax) {
        List<ConstraintBoundaryDto> curves = new ArrayList<ConstraintBoundaryDto>();
        int n = Math.max(40, points);

        for (InequalityConstraint g : problem.getInequalities()) {
            if (g.getFormula().contains("≥ 0") && !g.getFormula().contains("x₁²") && !g.getFormula().contains("x₂²")) {
                continue;
            }
            ConstraintBoundaryDto curve = traceBoundary(g.getFormula(), g::eval, xMin, xMax, yMin, yMax, n);
            if (curve != null) {
                curves.add(curve);
            }
        }
        for (EqualityConstraint h : problem.getEqualities()) {
            ConstraintBoundaryDto curve = traceBoundary(h.getFormula(), h::eval, xMin, xMax, yMin, yMax, n);
            if (curve != null) {
                curves.add(curve);
            }
        }
        return curves;
    }

    @FunctionalInterface
    interface Eval2 {
        double eval(double x1, double x2);
    }

    private static ConstraintBoundaryDto traceBoundary(String label, Eval2 fn,
                                                       double xMin, double xMax,
                                                       double yMin, double yMax, int points) {
        List<Double> xs = new ArrayList<Double>();
        List<Double> ys = new ArrayList<Double>();

        for (int i = 0; i <= points; i++) {
            double x = xMin + (xMax - xMin) * i / points;
            Double y = solveZeroY(fn, x, yMin, yMax);
            if (y != null) {
                xs.add(x);
                ys.add(y);
            }
        }

        for (int j = 0; j <= points; j++) {
            double y = yMin + (yMax - yMin) * j / points;
            Double x = solveZeroX(fn, y, xMin, xMax);
            if (x != null) {
                boolean dup = false;
                for (int k = 0; k < xs.size(); k++) {
                    if (Math.abs(xs.get(k) - x) < 1e-4 && Math.abs(ys.get(k) - y) < 1e-4) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) {
                    xs.add(x);
                    ys.add(y);
                }
            }
        }

        if (xs.size() < 3) {
            return null;
        }
        ConstraintBoundaryDto dto = new ConstraintBoundaryDto();
        dto.setLabel(label);
        dto.setX(xs);
        dto.setY(ys);
        return dto;
    }

    private static Double solveZeroY(Eval2 fn, double x, double yLo, double yHi) {
        double fLo = fn.eval(x, yLo);
        double fHi = fn.eval(x, yHi);
        if (!Double.isFinite(fLo) || !Double.isFinite(fHi)) {
            return null;
        }
        if (fLo * fHi > 0) {
            return null;
        }
        double lo = yLo;
        double hi = yHi;
        for (int k = 0; k < 50; k++) {
            double mid = (lo + hi) / 2;
            double fMid = fn.eval(x, mid);
            if (Math.abs(fMid) < 1e-7) {
                return mid;
            }
            if (fLo * fMid <= 0) {
                hi = mid;
                fHi = fMid;
            } else {
                lo = mid;
                fLo = fMid;
            }
        }
        return (lo + hi) / 2;
    }

    private static Double solveZeroX(Eval2 fn, double y, double xLo, double xHi) {
        double fLo = fn.eval(xLo, y);
        double fHi = fn.eval(xHi, y);
        if (!Double.isFinite(fLo) || !Double.isFinite(fHi)) {
            return null;
        }
        if (fLo * fHi > 0) {
            return null;
        }
        double lo = xLo;
        double hi = xHi;
        for (int k = 0; k < 50; k++) {
            double mid = (lo + hi) / 2;
            double fMid = fn.eval(mid, y);
            if (Math.abs(fMid) < 1e-7) {
                return mid;
            }
            if (fLo * fMid <= 0) {
                hi = mid;
                fHi = fMid;
            } else {
                lo = mid;
                fLo = fMid;
            }
        }
        return (lo + hi) / 2;
    }
}
