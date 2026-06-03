package ru.mai.mathoptimization.function;

public final class MathFunctions {

    private MathFunctions() {
    }

    public static MultidimFunction f1Variant1() {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 2;
            }

            @Override
            public double apply(double[] x) {
                return 9 * x[0] * x[0] + 16 * x[1] * x[1] - 90 * x[0] - 128 * x[1];
            }
        };
    }

    public static MultidimFunction f2Variant1() {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 3;
            }

            @Override
            public double apply(double[] x) {
                return x[0] * x[0] + 2 * x[0] * x[1] + 2 * x[1] * x[1] + x[2] * x[2]
                        - x[1] * x[2] + x[0] + 3 * x[1] - x[2];
            }
        };
    }

    public static MultidimFunction f1Variant2() {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 2;
            }

            @Override
            public double apply(double[] x) {
                double a = 3 * x[0] * x[0] - x[1];
                double b = 2 * x[0] - 3 * x[1];
                return a * a + b * b;
            }
        };
    }

    public static MultidimFunction f1Variant3() {
        return quad2dSphere();
    }

    public static MultidimFunction f2Variant3() {
        return poly3Fourth();
    }

    public static MultidimFunction f2Variant4() {
        return quad3Mixed();
    }

    public static MultidimFunction f1Variant5() {
        return saddle2d();
    }

    public static MultidimFunction f2Variant5() {
        return rosen2d();
    }

    public static MultidimFunction f1Variant6() {
        return quadSimple2d();
    }

    public static MultidimFunction f2Variant6() {
        return pow4Rosen();
    }

    public static MultidimFunction quad2dSphere() {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 2;
            }

            @Override
            public double apply(double[] x) {
                return -6 * x[0] - 4 * x[1] + x[0] * x[0] + x[1] * x[1] + 18;
            }
        };
    }

    public static MultidimFunction poly3Fourth() {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 3;
            }

            @Override
            public double apply(double[] x) {
                double t = x[1] - 4;
                return x[0] * x[0] * x[0] * x[0] + 2 * x[0] * x[0] * x[0] + t * t + 2 * x[2] * x[2] + 8 * x[2];
            }
        };
    }

    public static MultidimFunction quad3Mixed() {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 3;
            }

            @Override
            public double apply(double[] x) {
                return 4 * x[0] * x[0] + 3 * x[1] * x[1] + x[2] * x[2]
                        + 4 * x[0] * x[1] - 2 * x[1] * x[2] - 16 * x[0] - 4 * x[2];
            }
        };
    }

    public static MultidimFunction saddle2d() {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 2;
            }

            @Override
            public double apply(double[] x) {
                return -x[0] * x[0] - x[1] * x[1] + x[0] * x[1] - x[0] + 2 * x[1];
            }
        };
    }

    public static MultidimFunction rosen2d() {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 2;
            }

            @Override
            public double apply(double[] x) {
                double a = x[0] - x[1];
                double b = x[1] - 2;
                return a * a + b * b;
            }
        };
    }

    public static MultidimFunction quadSimple2d() {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 2;
            }

            @Override
            public double apply(double[] x) {
                return x[0] * x[0] + 2 * x[1] * x[1] - 4 * x[0] + 2 * x[1];
            }
        };
    }

    public static MultidimFunction pow4Rosen() {
        return new MultidimFunction() {
            @Override
            public int dimension() {
                return 2;
            }

            @Override
            public double apply(double[] x) {
                double a = x[0] - 2;
                double b = x[0] - 2 * x[1];
                return a * a * a * a + b * b;
            }
        };
    }
}
