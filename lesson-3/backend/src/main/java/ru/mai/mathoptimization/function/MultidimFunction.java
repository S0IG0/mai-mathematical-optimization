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

    default double[][] hessian(double[] x, double h) {
        int n = dimension();
        double[][] hMatrix = new double[n][n];
        double[] g0 = gradient(x, h);
        for (int i = 0; i < n; i++) {
            double[] xp = x.clone();
            xp[i] += h;
            double[] g1 = gradient(xp, h);
            for (int j = 0; j < n; j++) {
                hMatrix[i][j] = (g1[j] - g0[j]) / h;
            }
        }
        return hMatrix;
    }
}
