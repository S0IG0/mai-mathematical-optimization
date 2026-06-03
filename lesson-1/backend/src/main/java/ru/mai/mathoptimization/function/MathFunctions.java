package ru.mai.mathoptimization.function;

public final class MathFunctions {

    private static final double PI = Math.PI;
    private static final double PENALTY = 1.0e12;

    private MathFunctions() {
    }

    public static double safe(ObjectiveFunction function, double x) {
        try {
            double value = function.apply(x);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return PENALTY;
            }
            return value;
        } catch (Exception ex) {
            return PENALTY;
        }
    }

    public static ObjectiveFunction safeWrap(ObjectiveFunction function) {
        return new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return safe(function, x);
            }
        };
    }

    public static ObjectiveFunction variant1F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return 3.0 * x - x * x * x - 1.0;
            }
        });
    }

    public static ObjectiveFunction variant1F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                if (Math.abs(x) < 1.0e-9) {
                    return PENALTY;
                }
                return (4.0 - x * x) / (x * (x * x + 3.0));
            }
        });
    }

    public static ObjectiveFunction variant2F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return 10.0 * x - 2.0 * x * x + 3.0;
            }
        });
    }

    public static ObjectiveFunction variant2F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double denom = x * x + 2.0 * x - 8.0;
                if (Math.abs(denom) < 1.0e-9) {
                    return PENALTY;
                }
                return (2.0 * x * x + 3.0) / denom;
            }
        });
    }

    public static ObjectiveFunction variant3F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return 3.0 * x - x * x * x;
            }
        });
    }

    public static ObjectiveFunction variant3F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double denom = x * x + 2.0 * x + 3.0;
                return (9.0 - x * x) / denom;
            }
        });
    }

    public static ObjectiveFunction variant4F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return 2.0 * x * x - 4.0 * x + 5.0;
            }
        });
    }

    public static ObjectiveFunction variant4F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double denom = x * x - x + 1.0;
                return (x * x + x + 1.0) / denom;
            }
        });
    }

    public static ObjectiveFunction variant5F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return x * x * x / 3.0 - 2.0 * x * x + 3.0 * x + 5.0 / 3.0;
            }
        });
    }

    public static ObjectiveFunction variant5F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double denom = Math.pow(x, 4) - 5.0 * x * x + 4.0;
                if (Math.abs(denom) < 1.0e-9) {
                    return PENALTY;
                }
                return (x * x - 2.0 * x - 3.0) / denom;
            }
        });
    }

    public static ObjectiveFunction variant6F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double num = Math.pow(x * x + x - 2.0, 2);
                double denom = x * x * (x * x - x - 2.0);
                if (Math.abs(denom) < 1.0e-9) {
                    return PENALTY;
                }
                return num / denom;
            }
        });
    }

    public static ObjectiveFunction variant6F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return x * x * x - 2.0 * x * x + x - 1.0;
            }
        });
    }

    public static ObjectiveFunction variant7F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                if (Math.abs(x) < 1.0e-6) {
                    return PENALTY;
                }
                int n = (int) Math.floor(Math.abs(x));
                double sign = n % 2 == 0 ? 3.0 : -3.0;
                return sign / Math.pow(x, n == 0 ? 2 : Math.max(1, n));
            }
        });
    }

    public static ObjectiveFunction variant7F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return -Math.sqrt(x * x + 5.0) + 2.0;
            }
        });
    }

    public static ObjectiveFunction variant8F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                if (Math.abs(x - 9.0) < 1.0e-9) {
                    return PENALTY;
                }
                return (x - 4.0) / (x - 9.0);
            }
        });
    }

    public static ObjectiveFunction variant8F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return Math.abs(x * x - 1.0);
            }
        });
    }

    public static ObjectiveFunction variant9F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                if (Math.abs(x - 2.0) < 1.0e-9) {
                    return PENALTY;
                }
                return (x * x + 2.0 * x - 3.0) / (x - 2.0);
            }
        });
    }

    public static ObjectiveFunction variant9F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                if (Math.abs(x * x - 1.0) < 1.0e-9) {
                    return PENALTY;
                }
                return 2.0 / (x * x - 1.0);
            }
        });
    }

    public static ObjectiveFunction variant10F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double denom = x * x - x - 2.0;
                if (Math.abs(denom) < 1.0e-9) {
                    return PENALTY;
                }
                return x * x * x / denom;
            }
        });
    }

    public static ObjectiveFunction variant10F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return x + 36.0 * x * x - 2.0 * x * x * x - Math.pow(x, 4);
            }
        });
    }

    public static ObjectiveFunction variant11F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double c = Math.cos(x);
                if (Math.abs(c) < 1.0e-9) {
                    return PENALTY;
                }
                return 1.0 / c;
            }
        });
    }

    public static ObjectiveFunction variant11F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return Math.pow(x * x + 3.0, 1.5) - x;
            }
        });
    }

    public static ObjectiveFunction variant12F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double s = Math.sin(x);
                if (Math.abs(s) < 1.0e-9) {
                    return PENALTY;
                }
                return 1.0 / s;
            }
        });
    }

    public static ObjectiveFunction variant12F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double inner = x * x + 3.0 * x;
                if (inner < 0) {
                    return PENALTY;
                }
                return Math.sqrt(inner) - x;
            }
        });
    }

    public static ObjectiveFunction variant13F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return Math.pow(x, 4) - 2.0 * x * x + 5.0;
            }
        });
    }

    public static ObjectiveFunction variant13F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double denom = x * x + 1.0;
                return (x + 1.0) * (x - 1.0) * (x - 3.0) / denom;
            }
        });
    }

    public static ObjectiveFunction variant14F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double denom = x * x * x - 9.0 * x;
                if (Math.abs(denom) < 1.0e-9) {
                    return PENALTY;
                }
                return (Math.pow(x, 4) - 5.0 * x * x + 4.0) / denom;
            }
        });
    }

    public static ObjectiveFunction variant14F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return x * x * x + 5.0 * x - 10.0;
            }
        });
    }

    public static ObjectiveFunction variant15F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double denom = x * x * x - 3.0 * x;
                if (Math.abs(denom) < 1.0e-9) {
                    return PENALTY;
                }
                return 1.0 / denom;
            }
        });
    }

    public static ObjectiveFunction variant15F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                if (x + 4.0 < 0) {
                    return PENALTY;
                }
                return Math.sqrt(x + 4.0) - x * x;
            }
        });
    }

    public static ObjectiveFunction variant16F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                if (Math.abs(x + 1.0) < 1.0e-9) {
                    return PENALTY;
                }
                double frac = (x * x - 6.0 * x + 9.0) / (x * x + 6.0 * x + 9.0);
                return frac / Math.pow(x + 1.0, 3);
            }
        });
    }

    public static ObjectiveFunction variant16F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return Math.pow(x * x * x + 2.0, 2) - x;
            }
        });
    }

    public static ObjectiveFunction variant17F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double denom = x * x - Math.pow(x, 4);
                if (Math.abs(denom) < 1.0e-9) {
                    return PENALTY;
                }
                return 4.0 * (x * x - 2.0 * x - 8.0) * (x * x - 9.0) / denom;
            }
        });
    }

    public static ObjectiveFunction variant17F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return x * x * x + 2.0 * x * x - x + 2.0;
            }
        });
    }

    public static ObjectiveFunction variant18F1() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                double denom = x * x - 5.0 * x + 6.0;
                if (Math.abs(denom) < 1.0e-9) {
                    return PENALTY;
                }
                return (x * x - 1.0) / denom;
            }
        });
    }

    public static ObjectiveFunction variant18F2() {
        return safeWrap(new ObjectiveFunction() {
            @Override
            public double apply(double x) {
                return Math.pow(x, 4) + 2.0 * x * x - x + 2.0;
            }
        });
    }

    public static double getPi() {
        return PI;
    }
}
