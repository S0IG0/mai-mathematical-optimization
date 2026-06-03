package ru.mai.mathoptimization.algorithm;

import ru.mai.mathoptimization.dto.IterationDto;
import ru.mai.mathoptimization.dto.OptimizationResultDto;
import ru.mai.mathoptimization.dto.PathPointDto;
import ru.mai.mathoptimization.dto.SubStepDto;
import ru.mai.mathoptimization.function.MultidimFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class MultidimensionalOptimizer {

    private static final int MAX_OUTER_ITERATIONS = 500;
    private static final double GRAD_H = 1e-5;
    private static final double LINE_RANGE = 3.0;

    private MultidimensionalOptimizer() {
    }

    public static OptimizationResultDto optimize(MultidimFunction function, String method, String methodLabel,
                                               double[] x0, double epsilon, double delta,
                                               boolean useOneDimensional, boolean minimize) {
        return optimize(function, method, methodLabel, x0, epsilon, delta, useOneDimensional, minimize, null);
    }

    public static OptimizationResultDto optimize(MultidimFunction function, String method, String methodLabel,
                                               double[] x0, double epsilon, double delta,
                                               boolean useOneDimensional, boolean minimize,
                                               OptimizationProgressListener listener) {
        if (x0.length != function.dimension()) {
            throw new IllegalArgumentException("Размерность начальной точки не совпадает с функцией");
        }
        if (epsilon <= 0) {
            throw new IllegalArgumentException("Точность ε должна быть положительной");
        }
        if (delta <= 0) {
            throw new IllegalArgumentException("Начальный шаг Δ должен быть положительным");
        }

        OptimizationResultDto result = new OptimizationResultDto();
        result.setMethod(method);
        result.setMethodLabel(methodLabel);
        result.setMinimize(minimize);
        OptimizationRunContext runContext = new OptimizationRunContext();

        if ("ROSENBROCK_DISCRETE".equals(method)) {
            rosenbrockDiscrete(function, x0, epsilon, delta, result, listener, runContext);
        } else if ("ROSENBROCK_CONTINUOUS".equals(method)) {
            rosenbrockContinuous(function, x0, epsilon, result, listener, runContext);
        } else if ("HOOKE_JEEVES_DISCRETE".equals(method)) {
            hookeJeevesDiscrete(function, x0, epsilon, delta, result, listener, runContext);
        } else if ("HOOKE_JEEVES_CONTINUOUS".equals(method)) {
            hookeJeevesContinuous(function, x0, epsilon, delta, result, listener, runContext);
        } else if ("GAUSS_SEIDEL_DISCRETE".equals(method)) {
            gaussSeidel(function, x0, epsilon, delta, false, result, listener, runContext);
        } else if ("GAUSS_SEIDEL_CONTINUOUS".equals(method)) {
            gaussSeidel(function, x0, epsilon, delta, useOneDimensional, result, listener, runContext);
        } else {
            throw new IllegalArgumentException("Неизвестный метод: " + method);
        }

        if (runContext.isStopped()) {
            result.setDiverged(true);
            result.setStatusMessage(runContext.getStopMessage());
        }

        if (result.getPath().isEmpty()) {
            throw new IllegalStateException("Метод не выполнил ни одной итерации");
        }

        PathPointDto lastPoint = result.getPath().get(result.getPath().size() - 1);
        result.setOptimalX(new ArrayList<Double>(lastPoint.getX()));
        result.setOptimalF(lastPoint.getF());
        result.setIterationsCount(result.getIterations().size());
        return result;
    }

    private static boolean addPath(OptimizationResultDto result, double[] x, double f, int k,
                                   OptimizationProgressListener listener,
                                   OptimizationRunContext runContext) {
        if (!runContext.checkState(x, f)) {
            return false;
        }
        if (!runContext.checkPathLimit(result.getPath().size())) {
            return false;
        }
        PathPointDto point = new PathPointDto(x, f, k);
        result.getPath().add(point);
        if (listener != null) {
            listener.onPathPoint(point);
        }
        return true;
    }

    private static void addIteration(OptimizationResultDto result, IterationDto iter,
                                     OptimizationProgressListener listener) {
        result.getIterations().add(iter);
        if (listener != null) {
            listener.onIteration(iter);
        }
    }

    private static List<Double> toList(double[] v) {
        return Arrays.stream(v).boxed().collect(Collectors.toList());
    }

    private static void rosenbrockDiscrete(MultidimFunction function, double[] x0, double epsilon,
                                           double delta, OptimizationResultDto result,
                                           OptimizationProgressListener listener,
                                           OptimizationRunContext runContext) {
        LineSearch.EvalCounter counter = new LineSearch.EvalCounter();
        int n = function.dimension();
        double[] x = LineSearch.copy(x0);
        double[] deltas = new double[n];
        Arrays.fill(deltas, delta);

        double fx = eval(function, x, counter);
        if (!addPath(result, x, fx, 0, listener, runContext)) {
            result.setFunctionEvaluations(counter.count);
            return;
        }
        int k = 0;

        while (k < MAX_OUTER_ITERATIONS && !runContext.isStopped()) {
            k++;
            IterationDto iter = newIteration(k, x, fx);
            double[] y = LineSearch.copy(x);

            for (int j = 0; j < n; j++) {
                SubStepDto sub = new SubStepDto();
                sub.setJ(j + 1);
                double[] dj = LineSearch.unitAxis(n, j);
                sub.setDj(toList(dj));
                sub.setYj(toList(y));
                sub.setFYj(eval(function, y, counter));

                boolean improved;
                int inner = 0;
                do {
                    improved = false;
                    inner++;
                    double[] plus = LineSearch.add(y, LineSearch.scale(dj, deltas[j]));
                    double fPlus = eval(function, plus, counter);
                    if (fPlus < eval(function, y, counter)) {
                        sub.setDeltaJ(deltas[j]);
                        sub.setYjPlus(toList(plus));
                        sub.setFYjPlus(fPlus);
                        y = plus;
                        improved = true;
                        continue;
                    }
                    double[] minus = LineSearch.add(y, LineSearch.scale(dj, -deltas[j]));
                    double fMinus = eval(function, minus, counter);
                    if (fMinus < eval(function, y, counter)) {
                        sub.setDeltaJ(-deltas[j]);
                        sub.setYjMinus(toList(minus));
                        sub.setFYjMinus(fMinus);
                        y = minus;
                        improved = true;
                    }
                } while (improved && inner < 50);

                sub.setYj(toList(y));
                sub.setFYj(eval(function, y, counter));
                iter.getSubSteps().add(sub);
            }

            double dist = LineSearch.distance(x, y);
            fx = eval(function, y, counter);
            addIteration(result, iter, listener);
            if (!addPath(result, y, fx, k, listener, runContext)) {
                break;
            }
            x = y;

            if (dist < epsilon) {
                break;
            }
        }
        result.setFunctionEvaluations(counter.count);
    }

    private static void rosenbrockContinuous(MultidimFunction function, double[] x0, double epsilon,
                                             OptimizationResultDto result,
                                             OptimizationProgressListener listener,
                                             OptimizationRunContext runContext) {
        LineSearch.EvalCounter counter = new LineSearch.EvalCounter();
        int n = function.dimension();
        double[] x = LineSearch.copy(x0);
        double fx = eval(function, x, counter);
        if (!addPath(result, x, fx, 0, listener, runContext)) {
            result.setFunctionEvaluations(counter.count);
            return;
        }
        int k = 0;

        while (k < MAX_OUTER_ITERATIONS && !runContext.isStopped()) {
            k++;
            IterationDto iter = newIteration(k, x, fx);
            double[] y = LineSearch.copy(x);

            for (int j = 0; j < n; j++) {
                SubStepDto sub = new SubStepDto();
                sub.setJ(j + 1);
                double[] dj = LineSearch.unitAxis(n, j);
                sub.setDj(toList(dj));
                sub.setYj(toList(y));
                sub.setFYj(eval(function, y, counter));

                LineSearch.LineSearchResult ls = LineSearch.goldenSection(
                        function, y, dj, -LINE_RANGE, LINE_RANGE, counter);
                sub.setLambdaJ(ls.lambda);
                double[] next = LineSearch.add(y, LineSearch.scale(dj, ls.lambda));
                sub.setYjPlus(toList(next));
                sub.setFYjPlus(ls.value);
                y = next;
                iter.getSubSteps().add(sub);
            }

            double dist = LineSearch.distance(x, y);
            fx = eval(function, y, counter);
            addIteration(result, iter, listener);
            if (!addPath(result, y, fx, k, listener, runContext)) {
                break;
            }
            x = y;
            if (dist < epsilon) {
                break;
            }
        }
        result.setFunctionEvaluations(counter.count);
    }

    private static void hookeJeevesDiscrete(MultidimFunction function, double[] x0, double epsilon,
                                              double delta, OptimizationResultDto result,
                                              OptimizationProgressListener listener,
                                              OptimizationRunContext runContext) {
        LineSearch.EvalCounter counter = new LineSearch.EvalCounter();
        int n = function.dimension();
        double step = delta;
        double[] base = LineSearch.copy(x0);
        double fBase = eval(function, base, counter);
        if (!addPath(result, base, fBase, 0, listener, runContext)) {
            result.setFunctionEvaluations(counter.count);
            return;
        }
        int k = 0;
        double[] prevBase = null;

        while (step >= epsilon && k < MAX_OUTER_ITERATIONS && !runContext.isStopped()) {
            k++;
            IterationDto iter = newIteration(k, base, fBase);
            double[] explored = exploratoryDiscrete(function, base, step, n, counter, iter);

            if (eval(function, explored, counter) < fBase - 1e-12) {
                if (prevBase != null) {
                    double[] direction = new double[n];
                    for (int i = 0; i < n; i++) {
                        direction[i] = explored[i] - prevBase[i];
                    }
                    double[] pattern = LineSearch.add(explored, direction);
                    SubStepDto patternStep = new SubStepDto();
                    patternStep.setJ(0);
                    patternStep.setDj(toList(direction));
                    patternStep.setYj(toList(explored));
                    patternStep.setFYj(eval(function, explored, counter));
                    patternStep.setYjPlus(toList(pattern));
                    patternStep.setFYjPlus(eval(function, pattern, counter));
                    iter.getSubSteps().add(patternStep);

                    double[] afterPattern = exploratoryDiscrete(function, pattern, step, n, counter, iter);
                    if (eval(function, afterPattern, counter) < eval(function, explored, counter)) {
                        explored = afterPattern;
                    }
                }
                prevBase = LineSearch.copy(base);
                base = explored;
                fBase = eval(function, base, counter);
                if (!addPath(result, base, fBase, k, listener, runContext)) {
                    break;
                }
            } else {
                step *= 0.5;
                prevBase = null;
            }
            addIteration(result, iter, listener);
        }
        result.setFunctionEvaluations(counter.count);
    }

    private static double[] exploratoryDiscrete(MultidimFunction function, double[] base, double step, int n,
                                                LineSearch.EvalCounter counter, IterationDto iter) {
        double[] y = LineSearch.copy(base);
        for (int j = 0; j < n; j++) {
            SubStepDto sub = new SubStepDto();
            sub.setJ(j + 1);
            double[] ej = LineSearch.unitAxis(n, j);
            sub.setDj(toList(ej));
            sub.setDeltaJ(step);
            sub.setYj(toList(y));
            sub.setFYj(eval(function, y, counter));

            double[] plus = LineSearch.add(y, LineSearch.scale(ej, step));
            double[] minus = LineSearch.add(y, LineSearch.scale(ej, -step));
            double fY = eval(function, y, counter);
            double fPlus = eval(function, plus, counter);
            double fMinus = eval(function, minus, counter);

            sub.setYjPlus(toList(plus));
            sub.setFYjPlus(fPlus);
            sub.setYjMinus(toList(minus));
            sub.setFYjMinus(fMinus);

            if (fPlus < fY && fPlus <= fMinus) {
                y = plus;
            } else if (fMinus < fY) {
                y = minus;
            }
            iter.getSubSteps().add(sub);
        }
        return y;
    }

    private static void hookeJeevesContinuous(MultidimFunction function, double[] x0, double epsilon,
                                                double delta, OptimizationResultDto result,
                                                OptimizationProgressListener listener,
                                                OptimizationRunContext runContext) {
        LineSearch.EvalCounter counter = new LineSearch.EvalCounter();
        int n = function.dimension();
        double step = delta;
        double[] base = LineSearch.copy(x0);
        double fBase = eval(function, base, counter);
        if (!addPath(result, base, fBase, 0, listener, runContext)) {
            result.setFunctionEvaluations(counter.count);
            return;
        }
        int k = 0;
        double[] prevBase = null;

        while (step >= epsilon && k < MAX_OUTER_ITERATIONS && !runContext.isStopped()) {
            k++;
            IterationDto iter = newIteration(k, base, fBase);
            double[] explored = exploratoryContinuous(function, base, step, n, counter, iter);

            if (eval(function, explored, counter) < fBase - 1e-12) {
                if (prevBase != null) {
                    double[] direction = new double[n];
                    for (int i = 0; i < n; i++) {
                        direction[i] = explored[i] - prevBase[i];
                    }
                    if (LineSearch.norm(direction) > 1e-12) {
                        LineSearch.LineSearchResult ls = LineSearch.goldenSection(
                                function, explored, direction, 0, 2.0, counter);
                        SubStepDto sub = new SubStepDto();
                        sub.setJ(0);
                        sub.setDj(toList(direction));
                        sub.setLambdaJ(ls.lambda);
                        double[] pattern = LineSearch.add(explored, LineSearch.scale(direction, ls.lambda));
                        sub.setYjPlus(toList(pattern));
                        sub.setFYjPlus(ls.value);
                        iter.getSubSteps().add(sub);
                        explored = exploratoryContinuous(function, pattern, step, n, counter, iter);
                    }
                }
                prevBase = LineSearch.copy(base);
                base = explored;
                fBase = eval(function, base, counter);
                if (!addPath(result, base, fBase, k, listener, runContext)) {
                    break;
                }
            } else {
                step *= 0.5;
                prevBase = null;
            }
            addIteration(result, iter, listener);
        }
        result.setFunctionEvaluations(counter.count);
    }

    private static double[] exploratoryContinuous(MultidimFunction function, double[] base, double step, int n,
                                                  LineSearch.EvalCounter counter, IterationDto iter) {
        double[] y = LineSearch.copy(base);
        for (int j = 0; j < n; j++) {
            SubStepDto sub = new SubStepDto();
            sub.setJ(j + 1);
            double[] ej = LineSearch.unitAxis(n, j);
            sub.setDj(toList(ej));
            sub.setYj(toList(y));
            sub.setFYj(eval(function, y, counter));

            LineSearch.LineSearchResult ls = LineSearch.goldenSection(function, y, ej, -step, step, counter);
            sub.setLambdaJ(ls.lambda);
            y = LineSearch.add(y, LineSearch.scale(ej, ls.lambda));
            sub.setYjPlus(toList(y));
            sub.setFYjPlus(ls.value);
            iter.getSubSteps().add(sub);
        }
        return y;
    }

    private static void gaussSeidel(MultidimFunction function, double[] x0, double epsilon, double delta,
                                    boolean continuous, OptimizationResultDto result,
                                    OptimizationProgressListener listener,
                                    OptimizationRunContext runContext) {
        LineSearch.EvalCounter counter = new LineSearch.EvalCounter();
        int n = function.dimension();
        double[] x = LineSearch.copy(x0);
        double fx = eval(function, x, counter);
        if (!addPath(result, x, fx, 0, listener, runContext)) {
            result.setFunctionEvaluations(counter.count);
            return;
        }
        int k = 0;
        double lineRange = continuous ? LINE_RANGE : delta;

        while (k < MAX_OUTER_ITERATIONS && !runContext.isStopped()) {
            k++;
            IterationDto iter = newIteration(k, x, fx);
            double[] y = LineSearch.copy(x);

            for (int j = 0; j < n; j++) {
                SubStepDto sub = new SubStepDto();
                sub.setJ(j + 1);
                double[] dj = LineSearch.negativeGradient(function, y, GRAD_H);
                double dNorm = LineSearch.norm(dj);
                if (dNorm < 1e-14) {
                    dj = LineSearch.unitAxis(n, j);
                } else {
                    for (int i = 0; i < n; i++) {
                        dj[i] /= dNorm;
                    }
                }
                sub.setDj(toList(dj));
                sub.setYj(toList(y));
                sub.setFYj(eval(function, y, counter));

                if (continuous) {
                    LineSearch.LineSearchResult ls = LineSearch.goldenSection(
                            function, y, dj, -lineRange, lineRange, counter);
                    sub.setLambdaJ(ls.lambda);
                    y = LineSearch.add(y, LineSearch.scale(dj, ls.lambda));
                    sub.setYjPlus(toList(y));
                    sub.setFYjPlus(ls.value);
                } else {
                    double[] plus = LineSearch.add(y, LineSearch.scale(dj, delta));
                    double[] minus = LineSearch.add(y, LineSearch.scale(dj, -delta));
                    double fY = eval(function, y, counter);
                    double fPlus = eval(function, plus, counter);
                    double fMinus = eval(function, minus, counter);
                    sub.setDeltaJ(delta);
                    sub.setYjPlus(toList(plus));
                    sub.setFYjPlus(fPlus);
                    sub.setYjMinus(toList(minus));
                    sub.setFYjMinus(fMinus);
                    if (fPlus < fY && fPlus <= fMinus) {
                        y = plus;
                    } else if (fMinus < fY) {
                        y = minus;
                    }
                    sub.setYj(toList(y));
                    sub.setFYj(eval(function, y, counter));
                }
                iter.getSubSteps().add(sub);

                double fAfterJ = eval(function, y, counter);
                if (!addPath(result, LineSearch.copy(y), fAfterJ, k, listener, runContext)) {
                    fx = fAfterJ;
                    addIteration(result, iter, listener);
                    result.setFunctionEvaluations(counter.count);
                    return;
                }
            }

            double dist = LineSearch.distance(x, y);
            fx = eval(function, y, counter);
            addIteration(result, iter, listener);
            x = y;
            if (dist < epsilon) {
                break;
            }
        }
        result.setFunctionEvaluations(counter.count);
    }

    private static IterationDto newIteration(int k, double[] x, double fx) {
        IterationDto iter = new IterationDto();
        iter.setK(k);
        iter.setXk(toList(x));
        iter.setFXk(fx);
        return iter;
    }

    private static double eval(MultidimFunction function, double[] x, LineSearch.EvalCounter counter) {
        counter.count++;
        return function.apply(x);
    }
}
