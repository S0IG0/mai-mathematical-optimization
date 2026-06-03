package ru.mai.mathoptimization.function;

public final class MathFunctions {

    private MathFunctions() {
    }

    public static MultidimFunction f1Variant(int v) {
        switch (v) {
            case 1: return v1f1();
            case 2: return v2f1();
            case 3: return v3f1();
            case 4: return v4f1();
            case 5: return v5f1();
            case 6: return v6f1();
            case 7: return v7f1();
            case 8: return v8f1();
            case 9: return v9f1();
            case 10: return v10f1();
            case 11: return v11f1();
            case 12: return v12f1();
            case 13: return v13f1();
            case 14: return v14f1();
            case 15: return v15f1();
            case 16: return v16f1();
            case 17: return v17f1();
            case 18: return v18f1();
            default: throw new IllegalArgumentException("Вариант " + v);
        }
    }

    public static MultidimFunction f2Variant(int v) {
        switch (v) {
            case 1: return v1f2();
            case 2: return v2f2();
            case 3: return v3f2();
            case 4: return v4f2();
            case 5: return v5f2();
            case 6: return v6f2();
            case 7: return v7f2();
            case 8: return v8f2();
            case 9: return v9f2();
            case 10: return v10f2();
            case 11: return v11f2();
            case 12: return v12f2();
            case 13: return v13f2();
            case 14: return v14f2();
            case 15: return v15f2();
            case 16: return v16f2();
            case 17: return v17f2();
            case 18: return v18f2();
            default: throw new IllegalArgumentException("Вариант " + v);
        }
    }

    private static MultidimFunction v1f1() {
        return fn2((x) -> 4 * x[0] + 2 * x[1] - x[0] * x[0] - x[1] * x[1] + 5);
    }

    private static MultidimFunction v1f2() {
        return fn2((x) -> 100 * x[0] * x[0] + 0.2 * x[1] * x[1] + x[0] + 2 * x[1]);
    }

    private static MultidimFunction v2f1() {
        return fn2((x) -> x[0] * x[0] + x[1] * x[1] - x[0] * x[1] + x[0] - 2 * x[1]);
    }

    private static MultidimFunction v2f2() {
        return fn2((x) -> -2 - x[0] - 2 * x[1] - 0.1 * x[0] * x[0] - 100 * x[1] * x[1]);
    }

    private static MultidimFunction v3f1() {
        return fn2((x) -> {
            double a = x[0] - 2;
            double b = x[1] - 1;
            return a * a + b * b;
        });
    }

    private static MultidimFunction v3f2() {
        return fn3((x) -> -4 * sq(x[0] - 3) - 2 * sq(x[1] - 1) - sq(x[2] - 2));
    }

    private static MultidimFunction v4f1() {
        return fn2((x) -> 200 * x[0] * x[0] + 0.5 * x[1] * x[1] + 2 * x[0] - x[1] + 3);
    }

    private static MultidimFunction v4f2() {
        return fn3((x) -> 4 * x[0] * x[0] + 3 * x[1] * x[1] + x[2] * x[2] - 16 * x[0] - 4 * x[2]);
    }

    private static MultidimFunction v5f1() {
        return fn2((x) -> 6 * x[0] + 32 * x[1] - 0.1 * x[0] * x[0] - 40 * x[1] * x[1]);
    }

    private static MultidimFunction v5f2() {
        return fn2((x) -> 2 * x[0] * x[0] + x[1] * x[1] - 12 * x[0]);
    }

    private static MultidimFunction v6f1() {
        return fn2((x) -> 2 * x[0] - x[0] * x[0] - x[1] * x[1] + 3);
    }

    private static MultidimFunction v6f2() {
        return fn2((x) -> -6 * x[0] + 2 * x[0] * x[0] - 2 * x[1] * x[0] + 2 * x[1] * x[1]);
    }

    private static MultidimFunction v7f1() {
        return fn2((x) -> x[0] * x[0] + x[1] * x[1] - 2 * x[0] - 2 * x[1] + 2);
    }

    private static MultidimFunction v7f2() {
        return fn2((x) -> x[0] * x[0] + 2 * x[1] * x[1] - 4 * x[0] + 2 * x[1]);
    }

    private static MultidimFunction v8f1() {
        return fn2((x) -> 6 * x[0] + 32 * x[1] - x[0] * x[0] - 4 * x[1] * x[1]);
    }

    private static MultidimFunction v8f2() {
        return fn3((x) -> x[0] * x[0] * x[0] + x[1] * x[1] * x[1] + x[2] * x[2] * x[2]
                + x[1] * x[2] - 3 * x[0] + 6 * x[1] + 2);
    }

    private static MultidimFunction v9f1() {
        return fn2((x) -> 4 * x[0] + 8 * x[1] - 2 * x[0] * x[0] - 2 * x[1] * x[1] + 2);
    }

    private static MultidimFunction v9f2() {
        return fn3((x) -> 4 * x[0] + 6 * x[1] - 2 * x[0] * x[0] - x[1] * x[1] - x[2] * x[2] + 11);
    }

    private static MultidimFunction v10f1() {
        return fn2((x) -> -4 * x[0] + 4 * x[0] * x[0] - 6 * x[0] * x[1] + 2 * x[1] * x[1]);
    }

    private static MultidimFunction v10f2() {
        return fn3((x) -> pow4(x[0]) + 2 * x[0] * x[0] + sq(x[1] - 3) + 2 * x[2] * x[2] + 8 * x[2]);
    }

    private static MultidimFunction v11f1() {
        return fn2((x) -> 4 * x[0] + 2 * x[1] * x[1] - x[0] * x[0] - x[1] * x[1] + 5);
    }

    private static MultidimFunction v11f2() {
        return fn3((x) -> -pow4(x[0]) + 2 * x[0] * x[0] - x[1] * x[1] + 2 * x[2] * x[2] + 8 * x[2]);
    }

    private static MultidimFunction v12f1() {
        return fn2((x) -> -2 * x[0] + 2 * x[0] * x[0] - 8 * x[0] * x[1] + 2 * x[1] * x[1]);
    }

    private static MultidimFunction v12f2() {
        return fn3((x) -> -pow4(x[0]) + 2 * x[0] * x[0] - sq(x[1] - 2) + 2 * x[2] * x[2] + 8 * x[2]);
    }

    private static MultidimFunction v13f1() {
        return fn2((x) -> 80 * x[0] * x[0] + 0.4 * x[1] * x[1] + x[0] + 2 * x[1]);
    }

    private static MultidimFunction v13f2() {
        return fn2((x) -> 6 * x[0] + 32 * x[1] - x[0] * x[0] - 4 * x[1] * x[1]);
    }

    private static MultidimFunction v14f1() {
        return fn2((x) -> 2 * x[0] - 4 * x[0] * x[0] - x[1] * x[1] + 3);
    }

    private static MultidimFunction v14f2() {
        return fn3((x) -> 4 * sq(x[0] - 2) + 2 * sq(x[1] - 1) + sq(x[2] - 3) + 2 * x[2] * x[2]);
    }

    private static MultidimFunction v15f1() {
        return fn2((x) -> 2 - 8 * x[0] + 3 * x[1] - 0.1 * x[0] * x[0] - 100 * x[1] * x[1]);
    }

    private static MultidimFunction v15f2() {
        return fn3((x) -> pow4(x[0]) - 2 * x[0] * x[0] - sq(x[1] - 3) + 2 * x[2] * x[2] + x[2]);
    }

    private static MultidimFunction v16f1() {
        return fn2((x) -> 2 - 2 * x[0] + 3 * x[1] - 10 * x[0] * x[0] - 2 * x[0] * x[1] + 4 * x[1] * x[1]);
    }

    private static MultidimFunction v16f2() {
        return fn3((x) -> -2 * pow4(x[0]) + 12 * x[0] * x[0] - 5 * pow4(x[1]) + 2 * x[2] * x[2] + 8 * x[2]);
    }

    private static MultidimFunction v17f1() {
        return fn2((x) -> 2 - 8 * x[0] + 3 * x[1] + pow4(x[0]) + 10 * x[1] * x[1]);
    }

    private static MultidimFunction v17f2() {
        return fn2((x) -> 6 * x[0] + 32 * x[1] - x[0] * x[0] - 4 * x[1] * x[1]);
    }

    private static MultidimFunction v18f1() {
        return fn2((x) -> x[0] - x[0] * x[0] - 2 * x[1] + x[0] * x[0] * x[0] + 3 * x[1] * x[1] * x[1]);
    }

    private static MultidimFunction v18f2() {
        return fn3((x) -> x[0] * x[0] * x[0] - 2 * x[0] * x[0] - sq(x[1] - 3) + 2 * x[2] * x[2] + x[2]);
    }

    private interface Fn2 {
        double eval(double[] x);
    }

    private interface Fn3 {
        double eval(double[] x);
    }

    private static MultidimFunction fn2(Fn2 f) {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 2;
            }

            @Override
            public double apply(double[] x) {
                return f.eval(x);
            }
        };
    }

    private static MultidimFunction fn3(Fn3 f) {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 3;
            }

            @Override
            public double apply(double[] x) {
                return f.eval(x);
            }
        };
    }

    private static double sq(double v) {
        return v * v;
    }

    private static double pow4(double v) {
        double s = v * v;
        return s * s;
    }
}
