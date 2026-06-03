package ru.mai.mathoptimization.algorithm;

import ru.mai.mathoptimization.problem.OptimizerKind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;

public final class UnconstrainedOptimizer {

    private static final int MAX_ITER = 8000;
    private static final double GRAD_H = 1e-6;
    private static final double TOL = 1e-7;

    private UnconstrainedOptimizer() {
    }

    public static class Result {
        private final double[] x;
        private final double f;
        private final List<double[]> path;

        public Result(double[] x, double f, List<double[]> path) {
            this.x = x;
            this.f = f;
            this.path = path;
        }

        public double[] getX() {
            return x;
        }

        public double getF() {
            return f;
        }

        public List<double[]> getPath() {
            return path;
        }
    }

    public static Result minimize(ToDoubleFunction<double[]> fn, OptimizerKind kind,
                                  double[] x0, double[] lower, double[] upper) {
        switch (kind) {
            case HOOKE_JEEVES:
                return hookeJeeves(fn, x0, lower, upper, 0.05, 1e-6);
            case CYCLIC_COORDINATE:
                return cyclicCoordinate(fn, x0, lower, upper, 1e-6);
            case CONJUGATE_GRADIENT:
                return conjugateGradient(fn, x0, lower, upper, 1e-6);
            default:
                return nelderMead(fn, x0, lower, upper);
        }
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double[] project(double[] x, double[] lower, double[] upper) {
        double[] r = x.clone();
        for (int i = 0; i < r.length; i++) {
            r[i] = clamp(r[i], lower[i], upper[i]);
        }
        return r;
    }

    private static Result nelderMead(ToDoubleFunction<double[]> fn, double[] x0,
                                     double[] lower, double[] upper) {
        int n = x0.length;
        double[][] simplex = new double[n + 1][n];
        simplex[0] = project(x0.clone(), lower, upper);
        double step = 0.25;
        for (int i = 0; i < n; i++) {
            simplex[i + 1] = project(simplex[0].clone(), lower, upper);
            simplex[i + 1][i] += step * Math.max(1, Math.abs(simplex[0][i]));
        }
        double[] f = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            f[i] = fn.applyAsDouble(simplex[i]);
        }
        List<double[]> path = new ArrayList<double[]>();
        path.add(Arrays.copyOf(simplex[0], n));

        double alpha = 1.0;
        double gamma = 2.0;
        double rho = 0.5;
        double sigma = 0.5;

        for (int iter = 0; iter < MAX_ITER; iter++) {
            int lo = 0;
            int hi = 0;
            for (int i = 0; i <= n; i++) {
                if (f[i] < f[lo]) lo = i;
                if (f[i] > f[hi]) hi = i;
            }
            int second = hi == 0 ? 1 : 0;
            for (int i = 0; i <= n; i++) {
                if (i != hi && f[i] > f[second] && i != hi) second = i;
            }
            double spread = 0;
            for (int i = 0; i <= n; i++) {
                spread += Math.abs(f[i] - f[lo]);
            }
            if (spread < TOL * (Math.abs(f[lo]) + 1)) {
                break;
            }

            double[] centroid = new double[n];
            for (int i = 0; i <= n; i++) {
                if (i == hi) continue;
                for (int j = 0; j < n; j++) {
                    centroid[j] += simplex[i][j];
                }
            }
            for (int j = 0; j < n; j++) {
                centroid[j] /= n;
            }

            double[] xr = new double[n];
            for (int j = 0; j < n; j++) {
                xr[j] = centroid[j] + alpha * (centroid[j] - simplex[hi][j]);
            }
            xr = project(xr, lower, upper);
            double fr = fn.applyAsDouble(xr);

            if (fr < f[lo]) {
                double[] xe = new double[n];
                for (int j = 0; j < n; j++) {
                    xe[j] = centroid[j] + gamma * (xr[j] - centroid[j]);
                }
                xe = project(xe, lower, upper);
                double fe = fn.applyAsDouble(xe);
                if (fe < fr) {
                    simplex[hi] = xe;
                    f[hi] = fe;
                } else {
                    simplex[hi] = xr;
                    f[hi] = fr;
                }
            } else if (fr < f[second]) {
                simplex[hi] = xr;
                f[hi] = fr;
            } else {
                double[] xc = new double[n];
                for (int j = 0; j < n; j++) {
                    xc[j] = centroid[j] + rho * (simplex[hi][j] - centroid[j]);
                }
                xc = project(xc, lower, upper);
                double fc = fn.applyAsDouble(xc);
                if (fc < f[hi]) {
                    simplex[hi] = xc;
                    f[hi] = fc;
                } else {
                    for (int i = 0; i <= n; i++) {
                        if (i == lo) continue;
                        for (int j = 0; j < n; j++) {
                            simplex[i][j] = simplex[lo][j] + sigma * (simplex[i][j] - simplex[lo][j]);
                        }
                        simplex[i] = project(simplex[i], lower, upper);
                        f[i] = fn.applyAsDouble(simplex[i]);
                    }
                }
            }
            path.add(Arrays.copyOf(simplex[lo], n));
        }

        int best = 0;
        for (int i = 1; i <= n; i++) {
            if (f[i] < f[best]) best = i;
        }
        return new Result(simplex[best], f[best], path);
    }

    private static Result hookeJeeves(ToDoubleFunction<double[]> fn, double[] x0,
                                      double[] lower, double[] upper, double delta, double eps) {
        int n = x0.length;
        double[] x = project(x0.clone(), lower, upper);
        double fx = fn.applyAsDouble(x);
        List<double[]> path = new ArrayList<double[]>();
        path.add(x.clone());

        while (delta > eps) {
            double[] exploratory = explore(fn, x, fx, delta, lower, upper);
            double fExp = fn.applyAsDouble(exploratory);
            if (fExp < fx - 1e-12) {
                double[] pattern = patternMove(fn, x, exploratory, lower, upper);
                double fPat = fn.applyAsDouble(pattern);
                if (fPat < fExp) {
                    x = pattern;
                    fx = fPat;
                } else {
                    x = exploratory;
                    fx = fExp;
                }
                path.add(x.clone());
            } else {
                delta *= 0.5;
            }
        }
        return new Result(x, fx, path);
    }

    private static double[] explore(ToDoubleFunction<double[]> fn, double[] x, double fx,
                                    double delta, double[] lower, double[] upper) {
        int n = x.length;
        double[] y = x.clone();
        for (int i = 0; i < n; i++) {
            double[] plus = y.clone();
            plus[i] = clamp(plus[i] + delta, lower[i], upper[i]);
            double fPlus = fn.applyAsDouble(plus);
            if (fPlus < fx) {
                y = plus;
                fx = fPlus;
                continue;
            }
            double[] minus = y.clone();
            minus[i] = clamp(minus[i] - delta, lower[i], upper[i]);
            double fMinus = fn.applyAsDouble(minus);
            if (fMinus < fx) {
                y = minus;
                fx = fMinus;
            }
        }
        return y;
    }

    private static double[] patternMove(ToDoubleFunction<double[]> fn, double[] x, double[] y,
                                        double[] lower, double[] upper) {
        int n = x.length;
        double[] z = new double[n];
        for (int i = 0; i < n; i++) {
            z[i] = clamp(2 * y[i] - x[i], lower[i], upper[i]);
        }
        if (fn.applyAsDouble(z) < fn.applyAsDouble(y)) {
            return z;
        }
        return y;
    }

    private static Result cyclicCoordinate(ToDoubleFunction<double[]> fn, double[] x0,
                                           double[] lower, double[] upper, double eps) {
        int n = x0.length;
        double[] x = project(x0.clone(), lower, upper);
        List<double[]> path = new ArrayList<double[]>();
        path.add(x.clone());
        double h = 0.05;

        for (int iter = 0; iter < MAX_ITER; iter++) {
            double prev = fn.applyAsDouble(x);
            for (int i = 0; i < n; i++) {
                double bestT = x[i];
                double bestF = prev;
                for (double t = lower[i]; t <= upper[i]; t += h) {
                    double[] trial = x.clone();
                    trial[i] = t;
                    double ft = fn.applyAsDouble(trial);
                    if (ft < bestF) {
                        bestF = ft;
                        bestT = t;
                    }
                }
                double[] left = x.clone();
                left[i] = clamp(bestT - h, lower[i], upper[i]);
                double[] right = x.clone();
                right[i] = clamp(bestT + h, lower[i], upper[i]);
                if (fn.applyAsDouble(left) < bestF) {
                    bestT = left[i];
                    bestF = fn.applyAsDouble(left);
                }
                if (fn.applyAsDouble(right) < bestF) {
                    bestT = right[i];
                    bestF = fn.applyAsDouble(right);
                }
                x[i] = bestT;
            }
            path.add(x.clone());
            double cur = fn.applyAsDouble(x);
            if (Math.abs(cur - prev) < eps * (Math.abs(prev) + 1)) {
                break;
            }
            if (iter > 50 && iter % 100 == 0) {
                h *= 0.7;
            }
        }
        return new Result(x, fn.applyAsDouble(x), path);
    }

    private static Result conjugateGradient(ToDoubleFunction<double[]> fn, double[] x0,
                                            double[] lower, double[] upper, double eps) {
        int n = x0.length;
        double[] x = project(x0.clone(), lower, upper);
        List<double[]> path = new ArrayList<double[]>();
        path.add(x.clone());

        double[] g = gradient(fn, x, GRAD_H);
        double[] d = g.clone();
        double gNorm = norm(g);

        for (int k = 0; k < MAX_ITER && gNorm > eps; k++) {
            double step = lineSearch(fn, x, d, lower, upper);
            for (int i = 0; i < n; i++) {
                x[i] = clamp(x[i] - step * d[i], lower[i], upper[i]);
            }
            path.add(x.clone());
            double[] gNew = gradient(fn, x, GRAD_H);
            double gNewNorm = norm(gNew);
            if (gNewNorm < eps) break;
            double beta = gNewNorm * gNewNorm / (gNorm * gNorm + 1e-30);
            for (int i = 0; i < n; i++) {
                d[i] = gNew[i] + beta * d[i];
            }
            g = gNew;
            gNorm = gNewNorm;
        }
        return new Result(x, fn.applyAsDouble(x), path);
    }

    private static double lineSearch(ToDoubleFunction<double[]> fn, double[] x, double[] d,
                                     double[] lower, double[] upper) {
        double lo = 0;
        double hi = 1.0;
        for (int i = 0; i < 20; i++) {
            hi *= 2;
            double[] trial = new double[x.length];
            boolean ok = true;
            for (int j = 0; j < x.length; j++) {
                trial[j] = clamp(x[j] - hi * d[j], lower[j], upper[j]);
                if (trial[j] <= lower[j] + 1e-12 || trial[j] >= upper[j] - 1e-12) {
                    ok = false;
                }
            }
            if (!ok) break;
            if (fn.applyAsDouble(trial) < fn.applyAsDouble(x)) {
                break;
            }
        }
        for (int i = 0; i < 30; i++) {
            double mid = (lo + hi) / 2;
            double[] trial = new double[x.length];
            for (int j = 0; j < x.length; j++) {
                trial[j] = clamp(x[j] - mid * d[j], lower[j], upper[j]);
            }
            if (fn.applyAsDouble(trial) < fn.applyAsDouble(x)) {
                hi = mid;
            } else {
                lo = mid;
            }
        }
        return hi;
    }

    private static double[] gradient(ToDoubleFunction<double[]> fn, double[] x, double h) {
        int n = x.length;
        double[] g = new double[n];
        double f0 = fn.applyAsDouble(x);
        for (int i = 0; i < n; i++) {
            double[] xp = x.clone();
            xp[i] += h;
            g[i] = (fn.applyAsDouble(xp) - f0) / h;
        }
        return g;
    }

    private static double norm(double[] v) {
        double s = 0;
        for (double c : v) {
            s += c * c;
        }
        return Math.sqrt(s);
    }
}
