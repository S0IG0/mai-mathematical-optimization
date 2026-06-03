package ru.mai.mathoptimization.algorithm;

import ru.mai.mathoptimization.dto.IterationStep;
import ru.mai.mathoptimization.dto.OptimizationResult;
import ru.mai.mathoptimization.function.ObjectiveFunction;

import java.util.ArrayList;
import java.util.List;

public final class OneDimensionalOptimizer {

    private static final double TAU = (Math.sqrt(5.0) - 1.0) / 2.0;
    private static final double GOLDEN_RATIO = 1.0 - TAU;
    private static final int MAX_ITERATIONS = 10_000;

    private OneDimensionalOptimizer() {
    }

    public static OptimizationResult optimize(ObjectiveFunction function, String method, String methodLabel,
                                              double a, double b, double epsilon, double l, boolean minimize) {
        if (a >= b) {
            throw new IllegalArgumentException("Левая граница интервала должна быть меньше правой (a < b)");
        }
        if (epsilon <= 0 || l <= 0) {
            throw new IllegalArgumentException("Параметры ε и l должны быть положительными");
        }

        ObjectiveFunction target = wrap(function, minimize);
        OptimizationResult result;

        if ("DICHOTOMY".equals(method)) {
            result = dichotomy(function, target, a, b, epsilon, l, minimize);
        } else if ("GOLDEN_SECTION".equals(method)) {
            result = goldenSection(function, target, a, b, l, minimize);
        } else if ("FIBONACCI".equals(method)) {
            result = fibonacci(function, target, a, b, l, minimize);
        } else {
            throw new IllegalArgumentException("Неизвестный метод: " + method);
        }

        result.setMethod(method);
        result.setMethodLabel(methodLabel);
        result.setMinimize(minimize);
        result.setInitialA(a);
        result.setInitialB(b);
        return result;
    }

    private static ObjectiveFunction wrap(ObjectiveFunction function, boolean minimize) {
        if (minimize) {
            return function;
        }
        return new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return -function.apply(x);
            }
        };
    }

    private static double evalTarget(ObjectiveFunction target, double x, Counter counter) {
        counter.count++;
        return target.apply(x);
    }

    private static double toOriginalValue(ObjectiveFunction original, double x) {
        return original.apply(x);
    }

    private static double unwrapForDisplay(ObjectiveFunction original, ObjectiveFunction target,
                                           double x, double targetValue, boolean minimize) {
        if (minimize) {
            return targetValue;
        }
        return toOriginalValue(original, x);
    }

    private static OptimizationResult dichotomy(ObjectiveFunction original, ObjectiveFunction target,
                                                double a, double b, double epsilon, double l,
                                                boolean minimize) {
        Counter counter = new Counter();
        List<IterationStep> steps = new ArrayList<IterationStep>();
        int k = 0;

        while (b - a > l) {
            if (++k > MAX_ITERATIONS) {
                throw new IllegalArgumentException(
                        "Превышено максимальное число итераций. Проверьте параметры ε и l");
            }

            double length = b - a;
            // ε должно быть меньше длины интервала, иначе точки λ и μ выходят за [a; b] и интервал расширяется
            if (length <= epsilon) {
                break;
            }

            double effectiveEpsilon = Math.min(epsilon, length * 0.99);
            double lambda = (a + b - effectiveEpsilon) / 2.0;
            double mu = (a + b + effectiveEpsilon) / 2.0;

            double prevA = a;
            double prevB = b;

            double fLambdaTarget = evalTarget(target, lambda, counter);
            double fMuTarget = evalTarget(target, mu, counter);
            double fLambda = unwrapForDisplay(original, target, lambda, fLambdaTarget, minimize);
            double fMu = unwrapForDisplay(original, target, mu, fMuTarget, minimize);

            steps.add(new IterationStep(k, prevA, prevB, lambda, mu, fLambda, fMu, null, null));

            if (fLambdaTarget < fMuTarget) {
                b = mu;
            } else if (fLambdaTarget > fMuTarget) {
                a = lambda;
            } else {
                a = lambda;
                b = mu;
            }

            if (b - a >= length - 1.0e-15) {
                break;
            }
        }

        double optimalX = (a + b) / 2.0;
        double optimalF = toOriginalValue(original, optimalX);
        counter.count++;

        return buildResult(steps, a, b, optimalX, optimalF, counter.count, k);
    }

    private static OptimizationResult goldenSection(ObjectiveFunction original, ObjectiveFunction target,
                                                    double a, double b, double l, boolean minimize) {
        Counter counter = new Counter();
        List<IterationStep> steps = new ArrayList<IterationStep>();

        double lambda = a + GOLDEN_RATIO * (b - a);
        double mu = a + TAU * (b - a);
        double fLambdaTarget = evalTarget(target, lambda, counter);
        double fMuTarget = evalTarget(target, mu, counter);
        double fLambda = unwrapForDisplay(original, target, lambda, fLambdaTarget, minimize);
        double fMu = unwrapForDisplay(original, target, mu, fMuTarget, minimize);
        int k = 0;

        while (b - a > l) {
            if (++k > MAX_ITERATIONS) {
                throw new IllegalArgumentException(
                        "Превышено максимальное число итераций. Проверьте параметр l");
            }
            steps.add(new IterationStep(k, a, b, lambda, mu, fLambda, fMu, null, null));

            if (fLambdaTarget < fMuTarget) {
                b = mu;
                mu = lambda;
                fMuTarget = fLambdaTarget;
                fMu = fLambda;
                lambda = a + GOLDEN_RATIO * (b - a);
                fLambdaTarget = evalTarget(target, lambda, counter);
                fLambda = unwrapForDisplay(original, target, lambda, fLambdaTarget, minimize);
            } else {
                a = lambda;
                lambda = mu;
                fLambdaTarget = fMuTarget;
                fLambda = fMu;
                mu = a + TAU * (b - a);
                fMuTarget = evalTarget(target, mu, counter);
                fMu = unwrapForDisplay(original, target, mu, fMuTarget, minimize);
            }
        }

        double optimalX = (a + b) / 2.0;
        double optimalF = toOriginalValue(original, optimalX);
        counter.count++;

        return buildResult(steps, a, b, optimalX, optimalF, counter.count, k);
    }

    private static OptimizationResult fibonacci(ObjectiveFunction original, ObjectiveFunction target,
                                                double a, double b, double l, boolean minimize) {
        Counter counter = new Counter();
        List<IterationStep> steps = new ArrayList<IterationStep>();

        int n = findFibonacciSteps(b - a, l);
        double lambda = a + (fib(n - 2) / (double) fib(n)) * (b - a);
        double mu = a + (fib(n - 1) / (double) fib(n)) * (b - a);
        double fLambdaTarget = evalTarget(target, lambda, counter);
        double fMuTarget = evalTarget(target, mu, counter);
        double fLambda = unwrapForDisplay(original, target, lambda, fLambdaTarget, minimize);
        double fMu = unwrapForDisplay(original, target, mu, fMuTarget, minimize);
        int k = 0;

        for (int i = 1; i <= n - 1; i++) {
            if (++k > MAX_ITERATIONS) {
                throw new IllegalArgumentException(
                        "Превышено максимальное число итераций. Проверьте параметр l");
            }
            steps.add(new IterationStep(k, a, b, lambda, mu, fLambda, fMu, null, null));

            if (fLambdaTarget < fMuTarget) {
                b = mu;
                mu = lambda;
                fMuTarget = fLambdaTarget;
                fMu = fLambda;
                lambda = a + (fib(n - i - 1) / (double) fib(n - i + 1)) * (b - a);
                if (i < n - 1) {
                    fLambdaTarget = evalTarget(target, lambda, counter);
                    fLambda = unwrapForDisplay(original, target, lambda, fLambdaTarget, minimize);
                }
            } else {
                a = lambda;
                lambda = mu;
                fLambdaTarget = fMuTarget;
                fLambda = fMu;
                mu = a + (fib(n - i) / (double) fib(n - i + 1)) * (b - a);
                if (i < n - 1) {
                    fMuTarget = evalTarget(target, mu, counter);
                    fMu = unwrapForDisplay(original, target, mu, fMuTarget, minimize);
                }
            }
        }

        double optimalX = (a + b) / 2.0;
        double optimalF = toOriginalValue(original, optimalX);
        counter.count++;

        return buildResult(steps, a, b, optimalX, optimalF, counter.count, k);
    }

    private static int findFibonacciSteps(double length, double l) {
        int n = 2;
        while (fib(n) < length / l) {
            n++;
            if (n > 100) {
                throw new IllegalArgumentException("Слишком большой интервал для заданного l");
            }
        }
        return n;
    }

    private static int fib(int n) {
        if (n <= 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        int f0 = 0;
        int f1 = 1;
        for (int i = 2; i <= n; i++) {
            int next = f0 + f1;
            f0 = f1;
            f1 = next;
        }
        return f1;
    }

    private static OptimizationResult buildResult(List<IterationStep> steps, double a, double b,
                                                  double optimalX, double optimalF,
                                                  int evaluations, int iterations) {
        OptimizationResult result = new OptimizationResult();
        result.setIterations(steps);
        result.setFinalA(a);
        result.setFinalB(b);
        result.setOptimalX(optimalX);
        result.setOptimalF(optimalF);
        result.setFunctionEvaluations(evaluations);
        result.setIterationsCount(iterations);
        return result;
    }

    private static final class Counter {
        private int count;
    }
}
