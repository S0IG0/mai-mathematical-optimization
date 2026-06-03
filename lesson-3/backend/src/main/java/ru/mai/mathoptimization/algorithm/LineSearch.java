package ru.mai.mathoptimization.algorithm;

import ru.mai.mathoptimization.function.MultidimFunction;

public final class LineSearch {

    private static final double TAU = (Math.sqrt(5.0) - 1.0) / 2.0;
    private static final int MAX_ITER = 80;

    private LineSearch() {
    }

    public static class LineSearchResult {
        public final double lambda;
        public final double value;
        public final int evaluations;

        public LineSearchResult(double lambda, double value, int evaluations) {
            this.lambda = lambda;
            this.value = value;
            this.evaluations = evaluations;
        }
    }

    public static LineSearchResult goldenSection(MultidimFunction function, double[] base, double[] direction,
                                                 double lambdaMin, double lambdaMax, EvalCounter counter) {
        double a = lambdaMin;
        double b = lambdaMax;
        double c = b - TAU * (b - a);
        double d = a + TAU * (b - a);
        double fc = evalAlong(function, base, direction, c, counter);
        double fd = evalAlong(function, base, direction, d, counter);

        int iter = 0;
        while (Math.abs(b - a) > 1e-6 && ++iter < MAX_ITER) {
            if (fc < fd) {
                b = d;
                d = c;
                fd = fc;
                c = b - TAU * (b - a);
                fc = evalAlong(function, base, direction, c, counter);
            } else {
                a = c;
                c = d;
                fc = fd;
                d = a + TAU * (b - a);
                fd = evalAlong(function, base, direction, d, counter);
            }
        }
        double lambda = (a + b) / 2.0;
        double value = evalAlong(function, base, direction, lambda, counter);
        return new LineSearchResult(lambda, value, counter.count);
    }

    public static double evalAlong(MultidimFunction function, double[] base, double[] direction,
                                   double lambda, EvalCounter counter) {
        double[] point = add(base, scale(direction, lambda));
        counter.count++;
        return function.apply(point);
    }

    public static double[] add(double[] a, double[] b) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            r[i] = a[i] + b[i];
        }
        return r;
    }

    public static double[] scale(double[] v, double s) {
        double[] r = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            r[i] = v[i] * s;
        }
        return r;
    }

    public static double[] unitAxis(int n, int j) {
        double[] e = new double[n];
        e[j] = 1.0;
        return e;
    }

    public static double norm(double[] v) {
        double s = 0;
        for (double x : v) {
            s += x * x;
        }
        return Math.sqrt(s);
    }

    public static double[] negativeGradient(MultidimFunction function, double[] x, double h) {
        double[] g = function.gradient(x, h);
        for (int i = 0; i < g.length; i++) {
            g[i] = -g[i];
        }
        return g;
    }

    public static double distance(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            s += d * d;
        }
        return Math.sqrt(s);
    }

    public static double[] copy(double[] x) {
        return x.clone();
    }

    public static class EvalCounter {
        int count;
    }
}
