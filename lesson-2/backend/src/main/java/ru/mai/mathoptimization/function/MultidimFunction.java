package ru.mai.mathoptimization.function;

public interface MultidimFunction {

    int dimension();

    double apply(double[] x);

    default double[] gradient(double[] x, double h) {
        int n = dimension();
        double[] g = new double[n];
        double f0 = apply(x);
        for (int i = 0; i < n; i++) {
            double[] xp = x.clone();
            xp[i] += h;
            g[i] = (apply(xp) - f0) / h;
        }
        return g;
    }
}
