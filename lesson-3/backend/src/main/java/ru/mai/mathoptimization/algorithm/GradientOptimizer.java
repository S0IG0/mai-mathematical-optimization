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

public final class GradientOptimizer {

    private static final int MAX_ITERATIONS = 500;
    private static final double GRAD_H = 1e-5;
    private static final double LINE_RANGE = 3.0;
    private static final double HESSIAN_REG = 1e-6;

    private GradientOptimizer() {
    }

    public static OptimizationResultDto optimize(MultidimFunction function, String method, String methodLabel,
                                               double[] x0, double epsilon, double delta,
                                               boolean minimize,
                                               OptimizationProgressListener listener) {
        if (x0.length != function.dimension()) {
            throw new IllegalArgumentException("Размерность начальной точки не совпадает с функцией");
        }
        if (epsilon <= 0) {
            throw new IllegalArgumentException("Точность ε должна быть положительной");
        }
        if (delta <= 0) {
            throw new IllegalArgumentException("Шаг Δ должен быть положительным");
        }

        MultidimFunction target = wrap(function, minimize);
        OptimizationResultDto result = new OptimizationResultDto();
        result.setMethod(method);
        result.setMethodLabel(methodLabel);
        result.setMinimize(minimize);
        OptimizationRunContext runContext = new OptimizationRunContext();

        if ("GRADIENT_FIRST_ORDER".equals(method)) {
            gradientFirstOrder(function, target, x0, epsilon, delta, result, listener, runContext);
        } else if ("STEEPEST_DESCENT".equals(method)) {
            steepestDescent(function, target, x0, epsilon, result, listener, runContext);
        } else if ("CONJUGATE_GRADIENT".equals(method)) {
            conjugateGradient(function, target, x0, epsilon, result, listener, runContext);
        } else if ("GRADIENT_SECOND_ORDER".equals(method)) {
            newtonMethod(function, target, x0, epsilon, result, listener, runContext);
        } else if ("RAVINE".equals(method)) {
            ravineMethod(function, target, x0, epsilon, delta, result, listener, runContext);
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
        result.setOptimalF(function.apply(toArray(lastPoint.getX())));
        result.setIterationsCount(result.getIterations().size());
        return result;
    }

    private static MultidimFunction wrap(MultidimFunction function, boolean minimize) {
        if (minimize) {
            return function;
        }
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return function.dimension();
            }

            @Override
            public double apply(double[] x) {
                return -function.apply(x);
            }
        };
    }

    private static double[] toArray(List<Double> list) {
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    private static List<Double> toList(double[] v) {
        return Arrays.stream(v).boxed().collect(Collectors.toList());
    }

    private static boolean addPath(OptimizationResultDto result, MultidimFunction original,
                                   double[] x, int k,
                                   OptimizationProgressListener listener,
                                   OptimizationRunContext runContext) {
        double f = original.apply(x);
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

    private static double eval(MultidimFunction function, double[] x, LineSearch.EvalCounter counter) {
        counter.count++;
        return function.apply(x);
    }

    private static double[] descentDirection(MultidimFunction function, double[] x, double h) {
        double[] g = function.gradient(x, h);
        for (int i = 0; i < g.length; i++) {
            g[i] = -g[i];
        }
        return g;
    }

    private static double gradNorm(MultidimFunction function, double[] x, double h) {
        return LineSearch.norm(function.gradient(x, h));
    }

    private static void gradientFirstOrder(MultidimFunction original, MultidimFunction target, double[] x0,
                                           double epsilon, double step, OptimizationResultDto result,
                                           OptimizationProgressListener listener,
                                           OptimizationRunContext runContext) {
        LineSearch.EvalCounter counter = new LineSearch.EvalCounter();
        double[] x = LineSearch.copy(x0);
        if (!addPath(result, original, x, 0, listener, runContext)) {
            result.setFunctionEvaluations(counter.count);
            return;
        }
        eval(target, x, counter);

        int k = 0;
        while (k < MAX_ITERATIONS && !runContext.isStopped()) {
            if (gradNorm(target, x, GRAD_H) < epsilon) {
                break;
            }
            k++;
            double fx = original.apply(x);
            IterationDto iter = newIteration(k, x, fx);
            double[] d = descentDirection(target, x, GRAD_H);
            SubStepDto sub = new SubStepDto();
            sub.setJ(1);
            sub.setDj(toList(d));
            sub.setDeltaJ(step);
            sub.setYj(toList(x));
            sub.setFYj(fx);
            double[] next = LineSearch.add(x, LineSearch.scale(d, step));
            sub.setYjPlus(toList(next));
            sub.setFYjPlus(original.apply(next));
            iter.getSubSteps().add(sub);
            addIteration(result, iter, listener);
            eval(target, next, counter);
            if (!addPath(result, original, next, k, listener, runContext)) {
                break;
            }
            if (LineSearch.distance(x, next) < epsilon) {
                break;
            }
            x = next;
        }
        result.setFunctionEvaluations(counter.count);
    }

    private static void steepestDescent(MultidimFunction original, MultidimFunction target, double[] x0,
                                        double epsilon, OptimizationResultDto result,
                                        OptimizationProgressListener listener,
                                        OptimizationRunContext runContext) {
        LineSearch.EvalCounter counter = new LineSearch.EvalCounter();
        double[] x = LineSearch.copy(x0);
        if (!addPath(result, original, x, 0, listener, runContext)) {
            result.setFunctionEvaluations(counter.count);
            return;
        }
        eval(target, x, counter);

        int k = 0;
        while (k < MAX_ITERATIONS && !runContext.isStopped()) {
            if (gradNorm(target, x, GRAD_H) < epsilon) {
                break;
            }
            k++;
            double fx = original.apply(x);
            IterationDto iter = newIteration(k, x, fx);
            double[] d = descentDirection(target, x, GRAD_H);
            SubStepDto sub = new SubStepDto();
            sub.setJ(1);
            sub.setDj(toList(d));
            sub.setYj(toList(x));
            sub.setFYj(fx);
            LineSearch.LineSearchResult ls = LineSearch.goldenSection(target, x, d, 0, LINE_RANGE, counter);
            sub.setLambdaJ(ls.lambda);
            double[] next = LineSearch.add(x, LineSearch.scale(d, ls.lambda));
            sub.setYjPlus(toList(next));
            sub.setFYjPlus(original.apply(next));
            iter.getSubSteps().add(sub);
            addIteration(result, iter, listener);
            if (!addPath(result, original, next, k, listener, runContext)) {
                break;
            }
            if (LineSearch.distance(x, next) < epsilon) {
                break;
            }
            x = next;
        }
        result.setFunctionEvaluations(counter.count);
    }

    private static void conjugateGradient(MultidimFunction original, MultidimFunction target, double[] x0,
                                          double epsilon, OptimizationResultDto result,
                                          OptimizationProgressListener listener,
                                          OptimizationRunContext runContext) {
        LineSearch.EvalCounter counter = new LineSearch.EvalCounter();
        int n = target.dimension();
        double[] x = LineSearch.copy(x0);
        if (!addPath(result, original, x, 0, listener, runContext)) {
            result.setFunctionEvaluations(counter.count);
            return;
        }
        eval(target, x, counter);
        double[] d = descentDirection(target, x, GRAD_H);
        int k = 0;

        while (k < MAX_ITERATIONS && !runContext.isStopped()) {
            if (gradNorm(target, x, GRAD_H) < epsilon) {
                break;
            }
            k++;
            double fx = original.apply(x);
            IterationDto iter = newIteration(k, x, fx);
            SubStepDto sub = new SubStepDto();
            sub.setJ(1);
            sub.setDj(toList(d));
            sub.setYj(toList(x));
            sub.setFYj(fx);
            LineSearch.LineSearchResult ls = LineSearch.goldenSection(target, x, d, 0, LINE_RANGE, counter);
            sub.setLambdaJ(ls.lambda);
            double[] next = LineSearch.add(x, LineSearch.scale(d, ls.lambda));
            sub.setYjPlus(toList(next));
            sub.setFYjPlus(original.apply(next));
            iter.getSubSteps().add(sub);
            addIteration(result, iter, listener);
            if (!addPath(result, original, next, k, listener, runContext)) {
                break;
            }
            if (LineSearch.distance(x, next) < epsilon) {
                break;
            }
            double[] gradNext = target.gradient(next, GRAD_H);
            double[] gradOld = target.gradient(x, GRAD_H);
            counter.count += 2 * n;
            double num = 0;
            double den = 0;
            for (int i = 0; i < n; i++) {
                num += gradNext[i] * gradNext[i];
                den += gradOld[i] * gradOld[i];
            }
            double beta = den > 1e-20 ? num / den : 0;
            double[] newD = new double[n];
            for (int i = 0; i < n; i++) {
                newD[i] = -gradNext[i] + beta * d[i];
            }
            if (k % n == 0) {
                newD = descentDirection(target, next, GRAD_H);
            }
            x = next;
            d = newD;
        }
        result.setFunctionEvaluations(counter.count);
    }

    private static void newtonMethod(MultidimFunction original, MultidimFunction target, double[] x0,
                                     double epsilon, OptimizationResultDto result,
                                     OptimizationProgressListener listener,
                                     OptimizationRunContext runContext) {
        LineSearch.EvalCounter counter = new LineSearch.EvalCounter();
        int n = target.dimension();
        double[] x = LineSearch.copy(x0);
        if (!addPath(result, original, x, 0, listener, runContext)) {
            result.setFunctionEvaluations(counter.count);
            return;
        }
        eval(target, x, counter);

        int k = 0;
        while (k < MAX_ITERATIONS && !runContext.isStopped()) {
            if (gradNorm(target, x, GRAD_H) < epsilon) {
                break;
            }
            k++;
            double fx = original.apply(x);
            IterationDto iter = newIteration(k, x, fx);
            double[] g = target.gradient(x, GRAD_H);
            double[][] h = target.hessian(x, GRAD_H);
            counter.count += n * (n + 1);
            for (int i = 0; i < n; i++) {
                h[i][i] += HESSIAN_REG;
            }
            double[] d = solveSymmetric(h, negate(g));
            if (LineSearch.norm(d) < 1e-14) {
                d = descentDirection(target, x, GRAD_H);
            }
            SubStepDto sub = new SubStepDto();
            sub.setJ(1);
            sub.setDj(toList(d));
            sub.setYj(toList(x));
            sub.setFYj(fx);
            LineSearch.LineSearchResult ls = LineSearch.goldenSection(target, x, d, 0, 1.0, counter);
            sub.setLambdaJ(ls.lambda);
            double[] next = LineSearch.add(x, LineSearch.scale(d, ls.lambda));
            sub.setYjPlus(toList(next));
            sub.setFYjPlus(original.apply(next));
            iter.getSubSteps().add(sub);
            addIteration(result, iter, listener);
            if (!addPath(result, original, next, k, listener, runContext)) {
                break;
            }
            if (LineSearch.distance(x, next) < epsilon) {
                break;
            }
            x = next;
        }
        result.setFunctionEvaluations(counter.count);
    }

    private static void ravineMethod(MultidimFunction original, MultidimFunction target, double[] x0,
                                     double epsilon, double step, OptimizationResultDto result,
                                     OptimizationProgressListener listener,
                                     OptimizationRunContext runContext) {
        LineSearch.EvalCounter counter = new LineSearch.EvalCounter();
        double[] x = LineSearch.copy(x0);
        double[] xPrev = null;
        if (!addPath(result, original, x, 0, listener, runContext)) {
            result.setFunctionEvaluations(counter.count);
            return;
        }
        eval(target, x, counter);

        int k = 0;
        while (k < MAX_ITERATIONS && !runContext.isStopped()) {
            if (gradNorm(target, x, GRAD_H) < epsilon) {
                break;
            }
            k++;
            double fx = original.apply(x);
            IterationDto iter = newIteration(k, x, fx);
            SubStepDto gradStep = new SubStepDto();
            gradStep.setJ(1);
            double[] d = descentDirection(target, x, GRAD_H);
            gradStep.setDj(toList(d));
            gradStep.setDeltaJ(step);
            gradStep.setYj(toList(x));
            gradStep.setFYj(fx);
            double[] y = LineSearch.add(x, LineSearch.scale(d, step));
            eval(target, y, counter);
            gradStep.setYjPlus(toList(y));
            gradStep.setFYjPlus(original.apply(y));
            iter.getSubSteps().add(gradStep);

            double[] next = y;
            if (xPrev != null) {
                double[] ravineDir = new double[x.length];
                for (int i = 0; i < x.length; i++) {
                    ravineDir[i] = y[i] - xPrev[i];
                }
                if (LineSearch.norm(ravineDir) > 1e-12) {
                    SubStepDto ravineStep = new SubStepDto();
                    ravineStep.setJ(2);
                    ravineStep.setDj(toList(ravineDir));
                    ravineStep.setYj(toList(y));
                    ravineStep.setFYj(original.apply(y));
                    LineSearch.LineSearchResult ls = LineSearch.goldenSection(
                            target, y, ravineDir, 0, LINE_RANGE, counter);
                    ravineStep.setLambdaJ(ls.lambda);
                    next = LineSearch.add(y, LineSearch.scale(ravineDir, ls.lambda));
                    ravineStep.setYjPlus(toList(next));
                    ravineStep.setFYjPlus(original.apply(next));
                    iter.getSubSteps().add(ravineStep);
                }
            }
            addIteration(result, iter, listener);
            if (!addPath(result, original, next, k, listener, runContext)) {
                break;
            }
            if (LineSearch.distance(x, next) < epsilon) {
                break;
            }
            xPrev = LineSearch.copy(x);
            x = next;
        }
        result.setFunctionEvaluations(counter.count);
    }

    private static double[] negate(double[] v) {
        double[] r = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            r[i] = -v[i];
        }
        return r;
    }

    private static double[] solveSymmetric(double[][] a, double[] b) {
        int n = b.length;
        double[][] m = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(a[i], 0, m[i], 0, n);
            m[i][n] = b[i];
        }
        for (int col = 0; col < n; col++) {
            int pivot = col;
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(m[row][col]) > Math.abs(m[pivot][col])) {
                    pivot = row;
                }
            }
            double[] tmp = m[col];
            m[col] = m[pivot];
            m[pivot] = tmp;
            if (Math.abs(m[col][col]) < 1e-14) {
                continue;
            }
            for (int row = col + 1; row < n; row++) {
                double factor = m[row][col] / m[col][col];
                for (int j = col; j <= n; j++) {
                    m[row][j] -= factor * m[col][j];
                }
            }
        }
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = m[i][n];
            for (int j = i + 1; j < n; j++) {
                sum -= m[i][j] * x[j];
            }
            x[i] = Math.abs(m[i][i]) > 1e-14 ? sum / m[i][i] : 0;
        }
        return x;
    }

    private static IterationDto newIteration(int k, double[] x, double fx) {
        IterationDto iter = new IterationDto();
        iter.setK(k);
        iter.setXk(toList(x));
        iter.setFXk(fx);
        return iter;
    }
}
