package ru.mai.mathoptimization.algorithm;

import ru.mai.mathoptimization.dto.CandidatePointDto;
import ru.mai.mathoptimization.dto.ConstraintStatusDto;
import ru.mai.mathoptimization.dto.KuhnTuckerResultDto;
import ru.mai.mathoptimization.dto.KuhnTuckerSystemDto;
import ru.mai.mathoptimization.dto.PointDto;
import ru.mai.mathoptimization.problem.Constraint;
import ru.mai.mathoptimization.problem.ConstraintType;
import ru.mai.mathoptimization.problem.LinearConstraint;
import ru.mai.mathoptimization.problem.ProblemDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class KuhnTuckerSolver {

    private static final double FEAS_TOL = 1e-6;
    private static final double ACTIVE_TOL = 1e-4;
    private static final double KKT_TOL = 1e-3;

    private KuhnTuckerSolver() {
    }

    public static KuhnTuckerResultDto solve(ProblemDefinition problem) {
        List<double[]> candidates = collectCandidates(problem);
        List<CandidatePointDto> evaluated = new ArrayList<CandidatePointDto>();

        CandidatePointDto best = null;
        for (double[] p : candidates) {
            CandidatePointDto dto = evaluateCandidate(problem, p[0], p[1]);
            evaluated.add(dto);
            if (!dto.isFeasible()) {
                continue;
            }
            if (best == null || compareObjective(problem, dto, best) < 0) {
                best = dto;
            }
        }

        // interior stationary point
        double[] stationary = findStationaryPoint(problem);
        if (stationary != null) {
            CandidatePointDto dto = evaluateCandidate(problem, stationary[0], stationary[1]);
            if (!containsCandidate(evaluated, dto)) {
                evaluated.add(dto);
                if (dto.isFeasible() && (best == null || compareObjective(problem, dto, best) < 0)) {
                    best = dto;
                }
            }
        }

        // refine on nonlinear boundaries
        for (Constraint c : problem.getConstraints()) {
            if (!c.isLinear() && c.getType() == ConstraintType.INEQUALITY) {
                sampleNonlinearBoundary(problem, c, evaluated);
            }
        }
        for (CandidatePointDto dto : evaluated) {
            if (dto.isFeasible() && (best == null || compareObjective(problem, dto, best) < 0)) {
                best = dto;
            }
        }

        KuhnTuckerResultDto result = new KuhnTuckerResultDto();
        result.setVariantId(problem.getId());
        result.setObjectiveFormula(problem.getObjectiveFormula());
        result.setMinimize(problem.isMinimize());
        result.setKktSystem(buildKktSystem(problem));
        result.getCandidates().addAll(evaluated);
        result.setConclusion(buildConclusion(problem, evaluated, best));
        if (best != null) {
            result.setOptimalPoint(best.getPoint());
            result.setOptimalValue(best.getObjectiveValue());
            result.setOptimalKktSatisfied(best.isKktSatisfied());
        }
        return result;
    }

    public static KuhnTuckerResultDto solveUnconstrained(ProblemDefinition problem, double x0, double y0, int maxSteps) {
        double x = x0;
        double y = y0;
        List<PointDto> path = new ArrayList<PointDto>();
        path.add(point(x, y, problem.evalF(x, y)));

        double xLo = problem.getPlotXMin() - 0.25;
        double xHi = problem.getPlotXMax() + 0.25;
        double yLo = problem.getPlotYMin() - 0.25;
        double yHi = problem.getPlotYMax() + 0.25;

        double xFull = x;
        double yFull = y;
        int steps = Math.min(maxSteps, 40);

        for (int k = 0; k < steps; k++) {
            double[] g = problem.getObjective().gradient(xFull, yFull);
            double norm = Math.hypot(g[0], g[1]);
            if (norm < 1e-9) {
                break;
            }
            double step = lineSearch(problem, xFull, yFull, g, 0.25);
            double nx = xFull - step * g[0];
            double ny = yFull - step * g[1];
            if (!Double.isFinite(nx) || !Double.isFinite(ny)) {
                break;
            }
            double nf = problem.evalF(nx, ny);
            if (!Double.isFinite(nf)) {
                break;
            }
            xFull = nx;
            yFull = ny;

            if (nx >= xLo && nx <= xHi && ny >= yLo && ny <= yHi) {
                path.add(point(nx, ny, nf));
            } else {
                break;
            }
        }

        KuhnTuckerResultDto result = new KuhnTuckerResultDto();
        result.setVariantId(problem.getId());
        result.setObjectiveFormula(problem.getObjectiveFormula());
        result.setMinimize(true);
        result.getDescentPath().addAll(path);
        result.setConclusion(String.format(Locale.US,
                "Безусловный спуск от (%.2f; %.2f) (на графике — пока точка в области построения): последняя видимая (%.4f; %.4f), F = %.6f. "
                        + "Полный спуск уходит за пределы графика (функция неограничена снизу). Точка (0;0): F = %.6f, ∇F = (%.4f; %.4f).",
                x0, y0,
                path.get(path.size() - 1).getX1(), path.get(path.size() - 1).getX2(),
                path.get(path.size() - 1).getF(),
                problem.evalF(0, 0),
                problem.getObjective().gradient(0, 0)[0],
                problem.getObjective().gradient(0, 0)[1]));
        return result;
    }

    private static double lineSearch(ProblemDefinition problem, double x, double y, double[] dir, double t0) {
        double t = t0;
        double f0 = problem.evalF(x, y);
        for (int i = 0; i < 40; i++) {
            double nx = x - t * dir[0];
            double ny = y - t * dir[1];
            double f1 = problem.evalF(nx, ny);
            if (!Double.isFinite(f1) || f1 >= f0 - 1e-6 * t) {
                t *= 0.5;
            } else {
                return t;
            }
        }
        return t;
    }

    private static List<double[]> collectCandidates(ProblemDefinition problem) {
        Set<String> seen = new HashSet<String>();
        List<double[]> out = new ArrayList<double[]>();

        List<LinearConstraint> lines = new ArrayList<LinearConstraint>();
        for (Constraint c : problem.getConstraints()) {
            if (c instanceof LinearConstraint) {
                lines.add((LinearConstraint) c);
            }
        }

        double xmin = problem.getPlotXMin();
        double xmax = problem.getPlotXMax();
        double ymin = problem.getPlotYMin();
        double ymax = problem.getPlotYMax();
        lines.add(LinearConstraint.le("bbox_xmin", "x₁=min", -1, 0, xmin));
        lines.add(LinearConstraint.le("bbox_xmax", "x₁=max", 1, 0, -xmax));
        lines.add(LinearConstraint.le("bbox_ymin", "x₂=min", 0, -1, ymin));
        lines.add(LinearConstraint.le("bbox_ymax", "x₂=max", 0, 1, -ymax));

        for (int i = 0; i < lines.size(); i++) {
            for (int j = i + 1; j < lines.size(); j++) {
                double[] p = intersect(lines.get(i), lines.get(j));
                if (p != null) {
                    addUnique(out, seen, p[0], p[1]);
                }
            }
        }

        // equality line samples
        for (Constraint c : problem.getConstraints()) {
            if (c.getType() == ConstraintType.EQUALITY && c instanceof LinearConstraint) {
                LinearConstraint eq = (LinearConstraint) c;
                sampleEqualityLine(problem, eq, out, seen, 24);
            }
        }

        return out;
    }

    private static void sampleEqualityLine(ProblemDefinition problem, LinearConstraint eq,
                                           List<double[]> out, Set<String> seen, int n) {
        double a = eq.getA();
        double b = eq.getB();
        double c = eq.getC();
        if (Math.abs(b) > 1e-9) {
            double x1Start = problem.getPlotXMin();
            double x1End = problem.getPlotXMax();
            for (int i = 0; i <= n; i++) {
                double x1 = x1Start + (x1End - x1Start) * i / n;
                double x2 = -(a * x1 + c) / b;
                addUnique(out, seen, x1, x2);
            }
        } else if (Math.abs(a) > 1e-9) {
            double x1 = -c / a;
            double yStart = problem.getPlotYMin();
            double yEnd = problem.getPlotYMax();
            for (int i = 0; i <= n; i++) {
                double x2 = yStart + (yEnd - yStart) * i / n;
                addUnique(out, seen, x1, x2);
            }
        }
    }

    private static void sampleNonlinearBoundary(ProblemDefinition problem, Constraint c,
                                                List<CandidatePointDto> evaluated) {
        int n = 40;
        double xMin = problem.getPlotXMin();
        double xMax = problem.getPlotXMax();
        for (int i = 0; i <= n; i++) {
            double x1 = xMin + (xMax - xMin) * i / n;
            double x2 = solveX2OnBoundary(c, x1);
            if (Double.isFinite(x2)) {
                CandidatePointDto dto = evaluateCandidate(problem, x1, x2);
                if (!containsCandidate(evaluated, dto)) {
                    evaluated.add(dto);
                }
            }
        }
    }

    private static double solveX2OnBoundary(Constraint c, double x1) {
        double lo = -2;
        double hi = 20;
        for (int i = 0; i < 60; i++) {
            double mid = 0.5 * (lo + hi);
            if (c.value(x1, mid) <= 0) {
                hi = mid;
            } else {
                lo = mid;
            }
        }
        return Math.abs(c.value(x1, hi)) < 0.05 ? hi : Double.NaN;
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

    private static void addUnique(List<double[]> out, Set<String> seen, double x1, double x2) {
        if (!Double.isFinite(x1) || !Double.isFinite(x2)) {
            return;
        }
        String key = String.format(Locale.US, "%.5f|%.5f", x1, x2);
        if (seen.add(key)) {
            out.add(new double[]{x1, x2});
        }
    }

    private static boolean isFeasible(ProblemDefinition problem, double x1, double x2) {
        for (Constraint c : problem.getConstraints()) {
            double v = c.value(x1, x2);
            if (c.getType() == ConstraintType.EQUALITY) {
                if (Math.abs(v) > FEAS_TOL) {
                    return false;
                }
            } else if (v > FEAS_TOL) {
                return false;
            }
        }
        return true;
    }

    private static CandidatePointDto evaluateCandidate(ProblemDefinition problem, double x1, double x2) {
        CandidatePointDto dto = new CandidatePointDto();
        dto.setPoint(point(x1, x2, problem.evalF(x1, x2)));
        dto.setObjectiveValue(problem.evalF(x1, x2));
        dto.setFeasible(isFeasible(problem, x1, x2));
        dto.setGradient(gradientDto(problem.gradSignedF(x1, x2)));

        List<ConstraintStatusDto> statuses = new ArrayList<ConstraintStatusDto>();
        List<Constraint> active = new ArrayList<Constraint>();
        for (Constraint c : problem.getConstraints()) {
            double v = c.value(x1, x2);
            boolean activeIneq = c.getType() == ConstraintType.INEQUALITY && Math.abs(v) <= ACTIVE_TOL;
            boolean activeEq = c.getType() == ConstraintType.EQUALITY;
            ConstraintStatusDto cs = new ConstraintStatusDto();
            cs.setId(c.getId());
            cs.setLabel(c.getLabel());
            cs.setValue(v);
            cs.setActive(activeIneq || activeEq);
            statuses.add(cs);
            if (cs.isActive()) {
                active.add(c);
            }
        }
        dto.getConstraints().addAll(statuses);

        double[] lambda = solveLambdas(problem, x1, x2, active, statuses);
        dto.setMultipliers(lambda);
        boolean kkt = dto.isFeasible() && checkKkt(problem, x1, x2, active, lambda);
        dto.setKktSatisfied(kkt);
        dto.setDescription(buildCandidateDescription(problem, dto, active));
        return dto;
    }

    private static double[] solveLambdas(ProblemDefinition problem, double x1, double x2,
                                         List<Constraint> active, List<ConstraintStatusDto> statuses) {
        if (active.isEmpty()) {
            double[] g = problem.gradSignedF(x1, x2);
            return Math.hypot(g[0], g[1]) < KKT_TOL ? new double[0] : null;
        }
        int m = active.size();
        double[][] A = new double[2][m];
        for (int j = 0; j < m; j++) {
            double[] gj = active.get(j).gradient(x1, x2);
            A[0][j] = gj[0];
            A[1][j] = gj[1];
        }
        double[] g = problem.gradSignedF(x1, x2);
        double[] b = new double[]{-g[0], -g[1]};
        return solveNonNegativeLeastSquares(A, b, active, x1, x2);
    }

    private static double[] solveNonNegativeLeastSquares(double[][] A, double[] b,
                                                         List<Constraint> active, double x1, double x2) {
        int m = active.size();
        if (m == 1) {
            double[] gj = active.get(0).gradient(x1, x2);
            double denom = gj[0] * gj[0] + gj[1] * gj[1];
            if (denom < 1e-12) {
                return new double[]{0};
            }
            double lam = (-b[0] * gj[0] - b[1] * gj[1]) / denom;
            if (active.get(0).getType() == ConstraintType.INEQUALITY && lam < 0) {
                lam = 0;
            }
            return new double[]{lam};
        }

        double[] lambda = new double[m];
        Arrays.fill(lambda, 0.1);
        for (int iter = 0; iter < 80; iter++) {
            double[] residual = new double[]{b[0], b[1]};
            for (int j = 0; j < m; j++) {
                residual[0] += lambda[j] * A[0][j];
                residual[1] += lambda[j] * A[1][j];
            }
            for (int j = 0; j < m; j++) {
                double dot = A[0][j] * residual[0] + A[1][j] * residual[1];
                lambda[j] -= 0.2 * dot;
                if (active.get(j).getType() == ConstraintType.INEQUALITY && lambda[j] < 0) {
                    lambda[j] = 0;
                }
            }
        }
        return lambda;
    }

    private static boolean checkKkt(ProblemDefinition problem, double x1, double x2,
                                    List<Constraint> active, double[] lambda) {
        double[] g = problem.gradSignedF(x1, x2);
        double sx = g[0];
        double sy = g[1];
        if (lambda != null) {
            for (int j = 0; j < active.size() && j < lambda.length; j++) {
                double[] gj = active.get(j).gradient(x1, x2);
                sx += lambda[j] * gj[0];
                sy += lambda[j] * gj[1];
                if (active.get(j).getType() == ConstraintType.INEQUALITY && lambda[j] < -1e-6) {
                    return false;
                }
            }
        }
        return Math.hypot(sx, sy) < KKT_TOL * (1 + Math.hypot(x1, x2));
    }

    private static double[] findStationaryPoint(ProblemDefinition problem) {
        double x = 1;
        double y = 1;
        for (int i = 0; i < 200; i++) {
            double[] g = problem.gradSignedF(x, y);
            double norm = Math.hypot(g[0], g[1]);
            if (norm < 1e-9) {
                return new double[]{x, y};
            }
            double t = 0.1 / (1 + 0.1 * i);
            x -= t * g[0];
            y -= t * g[1];
        }
        double[] g = problem.gradSignedF(x, y);
        if (Math.hypot(g[0], g[1]) < 0.05) {
            return new double[]{x, y};
        }
        return null;
    }

    private static int compareObjective(ProblemDefinition problem, CandidatePointDto a, CandidatePointDto b) {
        if (problem.isMinimize()) {
            return Double.compare(a.getObjectiveValue(), b.getObjectiveValue());
        }
        return Double.compare(b.getObjectiveValue(), a.getObjectiveValue());
    }

    private static boolean containsCandidate(List<CandidatePointDto> list, CandidatePointDto dto) {
        for (CandidatePointDto c : list) {
            if (Math.abs(c.getPoint().getX1() - dto.getPoint().getX1()) < 1e-3
                    && Math.abs(c.getPoint().getX2() - dto.getPoint().getX2()) < 1e-3) {
                return true;
            }
        }
        return false;
    }

    private static KuhnTuckerSystemDto buildKktSystem(ProblemDefinition problem) {
        KuhnTuckerSystemDto sys = new KuhnTuckerSystemDto();
        sys.setStationarity("∇F(x) + Σ λᵢ∇gᵢ(x) + Σ μⱼ∇hⱼ(x) = 0");
        sys.setFeasibility("gᵢ(x) ≤ 0, hⱼ(x) = 0");
        sys.setComplementarity("λᵢ·gᵢ(x) = 0, λᵢ ≥ 0");
        StringBuilder lagrange = new StringBuilder("L = ");
        lagrange.append(problem.isMinimize() ? "F" : "−F");
        lagrange.append(" + Σλᵢgᵢ + Σμⱼhⱼ");
        sys.setLagrangian(lagrange.toString());
        List<String> constraints = new ArrayList<String>();
        for (Constraint c : problem.getConstraints()) {
            constraints.add(c.getLabel() + (c.getType() == ConstraintType.EQUALITY ? " = 0" : " ≤ 0"));
        }
        sys.getConstraintForms().addAll(constraints);
        return sys;
    }

    private static String buildConclusion(ProblemDefinition problem, List<CandidatePointDto> all, CandidatePointDto best) {
        long feasible = all.stream().filter(CandidatePointDto::isFeasible).count();
        long kktOk = all.stream().filter(c -> c.isFeasible() && c.isKktSatisfied()).count();
        if (best == null) {
            return "Допустимых экстремальных точек не найдено — проверьте ограничения.";
        }
        return String.format(Locale.US,
                "Проверено %d кандидатов: допустимых %d, из них удовлетворяют ККТ %d. " +
                        "Оптимум (%s): (%.4f; %.4f), F* = %.6f%s.",
                all.size(), feasible, kktOk,
                problem.isMinimize() ? "минимум" : "максимум",
                best.getPoint().getX1(), best.getPoint().getX2(), best.getObjectiveValue(),
                best.isKktSatisfied() ? " (условия ККТ выполнены)" : " (ККТ — приближённо)");
    }

    private static String buildCandidateDescription(ProblemDefinition problem, CandidatePointDto dto,
                                                    List<Constraint> active) {
        if (!dto.isFeasible()) {
            return "Недопустимая точка";
        }
        String type = active.isEmpty() ? "внутренняя" : "граничная (" + active.size() + " активных ограничений)";
        return type + (dto.isKktSatisfied() ? ", ККТ ✓" : ", ККТ ≈");
    }

    private static PointDto point(double x1, double x2, double f) {
        PointDto p = new PointDto();
        p.setX1(x1);
        p.setX2(x2);
        p.setF(f);
        return p;
    }

    private static PointDto gradientDto(double[] g) {
        PointDto p = new PointDto();
        p.setX1(g[0]);
        p.setX2(g[1]);
        return p;
    }
}
