package ru.mai.mathoptimization.algorithm;

import ru.mai.mathoptimization.dto.MuStepDto;
import ru.mai.mathoptimization.dto.PathPointDto;
import ru.mai.mathoptimization.dto.SolveResultDto;
import ru.mai.mathoptimization.problem.EqualityConstraint;
import ru.mai.mathoptimization.problem.InequalityConstraint;
import ru.mai.mathoptimization.problem.MethodKind;
import ru.mai.mathoptimization.problem.ProblemDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;

public final class PenaltyBarrierSolver {

    private static final double INTERIOR_MARGIN = 1e-4;
    private static final double BARRIER_FLOOR = 1e-8;

    private PenaltyBarrierSolver() {
    }

    public static SolveResultDto solve(ProblemDefinition problem, double[] x0, double[] mus,
                                       String schedule, String domainMode) {
        SolveResultDto result = new SolveResultDto();
        result.setVariantId(problem.getId());
        result.setMethodKind(problem.getMethodKind().name());
        result.setSchedule(schedule);
        result.setDomainMode(domainMode);

        double[] lower = defaultLower(problem);
        double[] upper = defaultUpper(problem);
        if (problem.isBoundedBox() && "BOUNDED_BOX".equals(domainMode)) {
            lower[0] = problem.getBoxXMin();
            upper[0] = problem.getBoxXMax();
            lower[1] = problem.getBoxYMin();
            upper[1] = problem.getBoxYMax();
        }

        double[] current = projectInterior(problem, x0.clone(), lower, upper, domainMode);
        List<double[]> fullPath = new ArrayList<double[]>();
        fullPath.add(current.clone());

        int k = 0;
        if ("TWO_STEP".equals(schedule) && mus.length >= 2) {
            MuStepDto step1 = solveOneMu(problem, current, mus[0], ++k, domainMode, lower, upper);
            result.getSteps().add(step1);
            appendPath(fullPath, step1.getInnerPath());
            current = toArray(step1.getX());
            MuStepDto step2 = solveOneMu(problem, current, mus[1], ++k, domainMode, lower, upper);
            result.getSteps().add(step2);
            appendPath(fullPath, step2.getInnerPath());
            current = toArray(step2.getX());
        } else {
            for (double mu : mus) {
                MuStepDto step = solveOneMu(problem, current, mu, ++k, domainMode, lower, upper);
                result.getSteps().add(step);
                appendPath(fullPath, step.getInnerPath());
                current = toArray(step.getX());
            }
        }

        result.setOptimalX(Arrays.asList(current[0], current[1]));
        result.setOptimalF(problem.evalF(current[0], current[1]));
        result.setPenaltyOrBarrierAtOpt(
                problem.getMethodKind() == MethodKind.PENALTY
                        ? penaltyTerm(problem, current, domainMode)
                        : barrierTerm(problem, current, domainMode));
        double maxViol = maxViolation(problem, current);
        result.setMaxViolation(maxViol);
        result.setFeasible(maxViol <= 1e-2);
        result.getConstraintViolations().addAll(describeViolations(problem, current));

        if (!containsPoint(fullPath, current)) {
            fullPath.add(current.clone());
        }
        for (double[] pt : fullPath) {
            PathPointDto pp = new PathPointDto();
            pp.setX(Arrays.asList(pt[0], pt[1]));
            pp.setF(problem.evalF(pt[0], pt[1]));
            result.getPath().add(pp);
        }

        result.setConclusion(buildConclusion(problem, result));
        return result;
    }

    private static MuStepDto solveOneMu(ProblemDefinition problem, double[] start, double mu, int k,
                                          String domainMode, double[] lower, double[] upper) {
        ToDoubleFunction<double[]> aux = x -> augmented(problem, x[0], x[1], mu, domainMode);
        double[] x0 = projectInterior(problem, start.clone(), lower, upper, domainMode);
        UnconstrainedOptimizer.Result opt = UnconstrainedOptimizer.minimize(
                aux, problem.getOptimizerKind(), x0, lower, upper);

        MuStepDto step = new MuStepDto();
        step.setK(k);
        step.setMu(mu);
        step.setX(Arrays.asList(opt.getX()[0], opt.getX()[1]));
        step.setF(problem.evalF(opt.getX()[0], opt.getX()[1]));
        double auxVal = problem.getMethodKind() == MethodKind.PENALTY
                ? penaltyTerm(problem, opt.getX(), domainMode)
                : barrierTerm(problem, opt.getX(), domainMode);
        step.setAlphaOrB(auxVal);
        step.setTheta(theta(problem, mu));
        step.setMuTimesAux(mu * auxVal);
        step.setInnerPath(toPathPoints(opt.getPath(), problem));
        return step;
    }

    private static double augmented(ProblemDefinition problem, double x1, double x2, double mu,
                                    String domainMode) {
        double f = problem.evalF(x1, x2);
        if (problem.getMethodKind() == MethodKind.PENALTY) {
            return f + mu * penaltyTerm(problem, new double[]{x1, x2}, domainMode);
        }
        double b = barrierTerm(problem, new double[]{x1, x2}, domainMode);
        if (!Double.isFinite(b)) {
            return 1e30;
        }
        return f + mu * b;
    }

    private static double penaltyTerm(ProblemDefinition problem, double[] x, String domainMode) {
        double sum = 0;
        for (InequalityConstraint g : activeInequalities(problem, domainMode)) {
            double v = g.eval(x[0], x[1]);
            double viol = Math.max(v, 0);
            sum += viol * viol;
        }
        for (EqualityConstraint h : problem.getEqualities()) {
            double v = h.eval(x[0], x[1]);
            sum += v * v;
        }
        return sum;
    }

    private static double barrierTerm(ProblemDefinition problem, double[] x, String domainMode) {
        double sum = 0;
        for (InequalityConstraint g : activeInequalities(problem, domainMode)) {
            double v = g.eval(x[0], x[1]);
            if (v >= -INTERIOR_MARGIN) {
                return Double.POSITIVE_INFINITY;
            }
            sum -= Math.log(-v);
        }
        for (EqualityConstraint h : problem.getEqualities()) {
            double v = h.eval(x[0], x[1]);
            sum += v * v / BARRIER_FLOOR;
        }
        return sum;
    }

    private static List<InequalityConstraint> activeInequalities(ProblemDefinition problem, String domainMode) {
        List<InequalityConstraint> list = new ArrayList<InequalityConstraint>();
        if ("UNCONSTRAINED_X".equals(domainMode)) {
            return list;
        }
        if ("NONNEGATIVE".equals(domainMode)) {
            for (InequalityConstraint g : problem.getInequalities()) {
                if (g.getFormula().contains("≥ 0")) {
                    list.add(g);
                }
            }
            return list;
        }
        if ("PARTIAL_X".equals(domainMode)) {
            for (InequalityConstraint g : problem.getInequalities()) {
                String f = g.getFormula();
                if (f.contains("x₁ + x₂") || f.contains("≥ 0")) {
                    list.add(g);
                }
            }
            return list;
        }
        list.addAll(problem.getInequalities());
        return list;
    }

    private static double theta(ProblemDefinition problem, double mu) {
        if (problem.getMethodKind() == MethodKind.PENALTY) {
            return 1.0 / Math.max(mu, 1e-12);
        }
        return mu;
    }

    private static double maxViolation(ProblemDefinition problem, double[] x) {
        double max = 0;
        for (InequalityConstraint g : problem.getInequalities()) {
            max = Math.max(max, Math.max(0, g.eval(x[0], x[1])));
        }
        for (EqualityConstraint h : problem.getEqualities()) {
            max = Math.max(max, Math.abs(h.eval(x[0], x[1])));
        }
        return max;
    }

    private static List<String> describeViolations(ProblemDefinition problem, double[] x) {
        List<String> list = new ArrayList<String>();
        for (InequalityConstraint g : problem.getInequalities()) {
            double v = g.eval(x[0], x[1]);
            if (v > 1e-3) {
                list.add(String.format("%s: g=%.4f > 0", g.getFormula(), v));
            }
        }
        for (EqualityConstraint h : problem.getEqualities()) {
            double v = h.eval(x[0], x[1]);
            if (Math.abs(v) > 1e-3) {
                list.add(String.format("%s: |h|=%.4f", h.getFormula(), Math.abs(v)));
            }
        }
        return list;
    }

    private static double[] projectInterior(ProblemDefinition problem, double[] x,
                                            double[] lower, double[] upper, String domainMode) {
        x[0] = Math.max(lower[0], Math.min(upper[0], x[0]));
        x[1] = Math.max(lower[1], Math.min(upper[1], x[1]));
        if (problem.getMethodKind() != MethodKind.BARRIER) {
            return x;
        }
        for (int attempt = 0; attempt < 40; attempt++) {
            boolean ok = true;
            for (InequalityConstraint g : activeInequalities(problem, domainMode)) {
                double v = g.eval(x[0], x[1]);
                if (v >= -INTERIOR_MARGIN) {
                    ok = false;
                    x[0] += 0.02 * Math.sin(attempt + x[1] * 3.1);
                    x[1] += 0.02 * Math.cos(attempt + x[0] * 2.7);
                    x[0] = Math.max(lower[0], Math.min(upper[0], x[0]));
                    x[1] = Math.max(lower[1], Math.min(upper[1], x[1]));
                }
            }
            if (ok) return x;
        }
        x[0] = (lower[0] + upper[0]) / 2;
        x[1] = (lower[1] + upper[1]) / 2;
        return x;
    }

    private static double[] defaultLower(ProblemDefinition p) {
        if (p.isBoundedBox()) {
            return new double[]{p.getBoxXMin(), p.getBoxYMin()};
        }
        return new double[]{p.getPlotXMin(), p.getPlotYMin()};
    }

    private static double[] defaultUpper(ProblemDefinition p) {
        if (p.isBoundedBox()) {
            return new double[]{p.getBoxXMax(), p.getBoxYMax()};
        }
        return new double[]{p.getPlotXMax(), p.getPlotYMax()};
    }

    private static double[] toArray(List<Double> list) {
        return new double[]{list.get(0), list.get(1)};
    }

    private static void appendPath(List<double[]> full, List<PathPointDto> inner) {
        for (PathPointDto p : inner) {
            if (p.getX() == null || p.getX().size() < 2) {
                continue;
            }
            double[] pt = new double[]{p.getX().get(0), p.getX().get(1)};
            if (!containsPoint(full, pt)) {
                full.add(pt);
            }
        }
    }

    private static boolean containsPoint(List<double[]> path, double[] pt) {
        if (path.isEmpty()) {
            return false;
        }
        double[] last = path.get(path.size() - 1);
        return Math.abs(last[0] - pt[0]) < 1e-9 && Math.abs(last[1] - pt[1]) < 1e-9;
    }

    private static List<PathPointDto> toPathPoints(List<double[]> path, ProblemDefinition problem) {
        List<PathPointDto> list = new ArrayList<PathPointDto>();
        for (double[] x : path) {
            PathPointDto p = new PathPointDto();
            p.setX(Arrays.asList(x[0], x[1]));
            p.setF(problem.evalF(x[0], x[1]));
            list.add(p);
        }
        return list;
    }

    private static String buildConclusion(ProblemDefinition problem, SolveResultDto result) {
        StringBuilder sb = new StringBuilder();
        sb.append(problem.getMethodKind() == MethodKind.PENALTY
                ? "Метод штрафных функций: " : "Метод барьерных функций: ");
        sb.append(String.format("X* ≈ (%.4f; %.4f), F(X*) = %.4f. ",
                result.getOptimalX().get(0), result.getOptimalX().get(1), result.getOptimalF()));
        if (result.isFeasible()) {
            sb.append("Точка допустима (все ограничения выполнены с точностью 10⁻²). ");
        } else {
            sb.append(String.format("Нарушение ограничений: max=%.4f — увеличьте μ (например до 10³…10⁵) или сузьте область поиска. ", result.getMaxViolation()));
        }
        if (result.getSteps().size() >= 2) {
            MuStepDto first = result.getSteps().get(0);
            MuStepDto last = result.getSteps().get(result.getSteps().size() - 1);
            sb.append(String.format("При μ=%.4g значение F=%.4f; при μ=%.4g — F=%.4f.",
                    first.getMu(), first.getF(), last.getMu(), last.getF()));
        }
        return sb.toString();
    }

}
