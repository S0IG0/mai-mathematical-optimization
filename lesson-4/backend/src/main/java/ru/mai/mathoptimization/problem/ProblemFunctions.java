package ru.mai.mathoptimization.problem;

import java.util.ArrayList;
import java.util.List;

public final class ProblemFunctions {

    private ProblemFunctions() {
    }

    private static ObjectiveFunction obj(String formula,
                                       java.util.function.BiFunction<Double, Double, Double> val,
                                       java.util.function.BiFunction<Double, Double, double[]> grad) {
        return new ObjectiveFunction() {
            @Override
            public double value(double x1, double x2) {
                return val.apply(x1, x2);
            }

            @Override
            public double[] gradient(double x1, double x2) {
                return grad.apply(x1, x2);
            }
        };
    }

    private static List<Constraint> boxNonneg() {
        List<Constraint> c = new ArrayList<Constraint>();
        c.add(LinearConstraint.le("x1_nonneg", "x₁ ≥ 0", -1, 0, 0));
        c.add(LinearConstraint.le("x2_nonneg", "x₂ ≥ 0", 0, -1, 0));
        return c;
    }

    private static List<Constraint> copy(List<Constraint> base) {
        return new ArrayList<Constraint>(base);
    }

    /** F from variant 1: −x₁² + 2x₁x₂ + x₂² + e^(−x₁−x₂) */
    public static ObjectiveFunction variant1Objective() {
        return obj("−x₁² + 2x₁x₂ + x₂² + e^(−x₁−x₂)",
                (x1, x2) -> -x1 * x1 + 2 * x1 * x2 + x2 * x2 + Math.exp(-x1 - x2),
                (x1, x2) -> new double[]{
                        -2 * x1 + 2 * x2 - Math.exp(-x1 - x2),
                        2 * x1 + 2 * x2 - Math.exp(-x1 - x2)
                });
    }

    public static ProblemDefinition build(int id) {
        switch (id) {
            case 1:
                return variant1();
            case 2:
                return variant2();
            case 3:
                return variant3();
            case 4:
                return variant4();
            case 5:
                return variant5();
            case 6:
                return variant6();
            case 7:
                return variant7();
            case 8:
                return variant8();
            case 9:
                return variant9();
            case 10:
                return variant10();
            case 11:
                return variant11();
            case 12:
                return variant12();
            case 13:
                return variant13();
            case 14:
                return variant14();
            case 15:
                return variant15();
            case 16:
                return variant16();
            case 17:
                return variant17();
            case 18:
                return variant18();
            default:
                throw new IllegalArgumentException("Вариант " + id + " не найден");
        }
    }

    private static ProblemDefinition variant1() {
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "−x₁ + x₂ ≤ 3", -1, 1, -3));
        c.add(LinearConstraint.le("c2", "x₁ + x₂ ≤ 5", 1, 1, -5));
        return new ProblemDefinition(1, "Вариант 1",
                "MIN F = −x₁² + 2x₁x₂ + x₂² + e^(−x₁−x₂)",
                true, variant1Objective(), c,
                0, 5, 0, 5, true,
                "Расширенный вариант: безусловная задача, проверка (0,0), спуск от (0,0), затем задача с ограничениями.");
    }

    private static ProblemDefinition variant2() {
        ObjectiveFunction f = obj("e^(−x₁+x₂) + x₁² + 2x₁x₂ + x₂² + 2x₁ + 6x₂",
                (x1, x2) -> Math.exp(-x1 + x2) + x1 * x1 + 2 * x1 * x2 + x2 * x2 + 2 * x1 + 6 * x2,
                (x1, x2) -> new double[]{
                        Math.exp(-x1 + x2) * (-1) + 2 * x1 + 2 * x2 + 2,
                        Math.exp(-x1 + x2) + 2 * x1 + 2 * x2 + 6
                });
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "x₁ + x₂ ≤ 4", 1, 1, -4));
        c.add(LinearConstraint.le("c2", "−x₁ + x₂ ≤ 2", -1, 1, -2));
        return new ProblemDefinition(2, "Вариант 2",
                "MIN F = e^(−x₁+x₂) + x₁² + 2x₁x₂ + x₂² + 2x₁ + 6x₂", true, f, c,
                0, 4, 0, 4, false, null);
    }

    private static ProblemDefinition variant3() {
        ObjectiveFunction f = quadForm(1, 2, 1, 2, 6);
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "x₁ + x₂ ≥ 3 → 3−x₁−x₂ ≤ 0", -1, -1, 3));
        c.add(LinearConstraint.eq("c2", "x₁ + x₂ = 4", 1, 1, -4));
        return new ProblemDefinition(3, "Вариант 3",
                "MIN F = x₁² + 2x₁x₂ + x₂² + 2x₁ + 6x₂", true, f, c,
                0, 4, 0, 4, false, null);
    }

    private static ProblemDefinition variant4() {
        ObjectiveFunction f = obj("x₁² − x₁x₂ + 2x₂² − 4x₁ − 5x₂",
                (x1, x2) -> x1 * x1 - x1 * x2 + 2 * x2 * x2 - 4 * x1 - 5 * x2,
                (x1, x2) -> new double[]{2 * x1 - x2 - 4, -x1 + 4 * x2 - 5});
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "x₁ + 2x₂ ≤ 6", 1, 2, -6));
        c.add(LinearConstraint.le("c2", "x₁ ≤ 2", 1, 0, -2));
        return new ProblemDefinition(4, "Вариант 4",
                "MIN F = x₁² − x₁x₂ + 2x₂² − 4x₁ − 5x₂", true, f, c,
                0, 3, 0, 4, false, null);
    }

    private static ProblemDefinition variant5() {
        ObjectiveFunction f = obj("(x₁ + 3x₂ + 3) / (2x₁ + x₂ + 6)",
                (x1, x2) -> {
                    double d = 2 * x1 + x2 + 6;
                    return (x1 + 3 * x2 + 3) / d;
                },
                (x1, x2) -> {
                    double num = x1 + 3 * x2 + 3;
                    double den = 2 * x1 + x2 + 6;
                    double den2 = den * den;
                    double dn1 = 1 - 2 * num / den;
                    double dn2 = 3 - num / den;
                    return new double[]{dn1 / den, dn2 / den};
                });
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "2x₁ + x₂ ≤ 12", 2, 1, -12));
        c.add(LinearConstraint.le("c2", "−x₁ + 2x₂ ≤ 4", -1, 2, -4));
        return new ProblemDefinition(5, "Вариант 5",
                "MIN F = (x₁ + 3x₂ + 3) / (2x₁ + x₂ + 6)", true, f, c,
                0, 6, 0, 4, false, null);
    }

    private static ProblemDefinition variant6() {
        ObjectiveFunction f = obj("(x₁ − 9/4)² + (x₂ − 2)²",
                (x1, x2) -> Math.pow(x1 - 2.25, 2) + Math.pow(x2 - 2, 2),
                (x1, x2) -> new double[]{2 * (x1 - 2.25), 2 * (x2 - 2)});
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "x₁ + x₂ ≤ 6", 1, 1, -6));
        c.add(new NonlinearConstraint("c2", "x₂ − x₁² ≤ 4", ConstraintType.INEQUALITY,
                (x1, x2) -> x2 - x1 * x1 - 4,
                (x1, x2) -> new double[]{-2 * x1, 1}));
        return new ProblemDefinition(6, "Вариант 6",
                "MIN F = (x₁ − 9/4)² + (x₂ − 2)²", true, f, c,
                0, 5, 0, 5, false, null);
    }

    private static ProblemDefinition variant7() {
        ObjectiveFunction f = obj("x₁²/2 + x₂²/2 − x₁ − 2x₂",
                (x1, x2) -> 0.5 * x1 * x1 + 0.5 * x2 * x2 - x1 - 2 * x2,
                (x1, x2) -> new double[]{x1 - 1, x2 - 2});
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "2x₁ + 3x₂ ≤ 6", 2, 3, -6));
        c.add(LinearConstraint.le("c2", "x₁ + 4x₂ ≤ 5", 1, 4, -5));
        return new ProblemDefinition(7, "Вариант 7",
                "MIN F = x₁²/2 + x₂²/2 − x₁ − 2x₂", true, f, c,
                0, 3, 0, 2, false, null);
    }

    private static ProblemDefinition variant8() {
        ObjectiveFunction f = obj("x₁² + x₁x₂ + 2x₂² − 12x₁ − 18x₂",
                (x1, x2) -> x1 * x1 + x1 * x2 + 2 * x2 * x2 - 12 * x1 - 18 * x2,
                (x1, x2) -> new double[]{2 * x1 + x2 - 12, x1 + 4 * x2 - 18});
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "−3x₁ + 6x₂ ≤ 9", -3, 6, -9));
        c.add(LinearConstraint.le("c2", "−x₁ + x₂ ≤ 1", -1, 1, -1));
        return new ProblemDefinition(8, "Вариант 8",
                "MIN F = x₁² + x₁x₂ + 2x₂² − 12x₁ − 18x₂", true, f, c,
                0, 6, 0, 4, false, null);
    }

    private static ProblemDefinition variant9() {
        ObjectiveFunction f = obj("x₁² − x₁x₂ + 2x₂² − 4x₁ − 5x₂",
                (x1, x2) -> x1 * x1 - x1 * x2 + 2 * x2 * x2 - 4 * x1 - 5 * x2,
                (x1, x2) -> new double[]{2 * x1 - x2 - 4, -x1 + 4 * x2 - 5});
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "x₁ + 2x₂ ≤ 6", 1, 2, -6));
        c.add(LinearConstraint.le("c2", "x₂ ≤ 2", 0, 1, -2));
        return new ProblemDefinition(9, "Вариант 9",
                "MIN F = x₁² − x₁x₂ + 2x₂² − 4x₁ − 5x₂", true, f, c,
                0, 3, 0, 3, false, null);
    }

    private static ProblemDefinition variant10() {
        ObjectiveFunction f = obj("e^(−x₁) + x₁² − x₁x₂ − 3x₂² + 4x₁ − 6x₂",
                (x1, x2) -> Math.exp(-x1) + x1 * x1 - x1 * x2 - 3 * x2 * x2 + 4 * x1 - 6 * x2,
                (x1, x2) -> new double[]{
                        -Math.exp(-x1) + 2 * x1 - x2 + 4,
                        -x1 - 6 * x2 - 6
                });
        List<Constraint> c = new ArrayList<Constraint>();
        c.add(LinearConstraint.le("x1_lb", "x₁ ≥ 1", -1, 0, 1));
        c.add(LinearConstraint.le("x2_ub", "x₂ ≤ 3", 0, 1, -3));
        c.add(LinearConstraint.le("c1", "2x₁ + x₂ ≤ 8", 2, 1, -8));
        c.add(LinearConstraint.le("c2", "−x₁ + x₂ ≤ 2", -1, 1, -2));
        return new ProblemDefinition(10, "Вариант 10",
                "MIN F = e^(−x₁) + x₁² − x₁x₂ − 3x₂² + 4x₁ − 6x₂", true, f, c,
                0, 4, 0, 4, false, null);
    }

    private static ProblemDefinition variant11() {
        ObjectiveFunction f = obj("−(x₁−2)² − (x₂−1)²",
                (x1, x2) -> -Math.pow(x1 - 2, 2) - Math.pow(x2 - 1, 2),
                (x1, x2) -> new double[]{-2 * (x1 - 2), -2 * (x2 - 1)});
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "2x₁ + x₂ ≤ 12", 2, 1, -12));
        c.add(LinearConstraint.le("c2", "−x₁ + x₂ ≤ 4", -1, 1, -4));
        return new ProblemDefinition(11, "Вариант 11",
                "MIN F = −(x₁−2)² − (x₂−1)²", true, f, c,
                0, 6, 0, 5, false, null);
    }

    private static ProblemDefinition variant12() {
        ObjectiveFunction f = obj("2x₁² + 3x₁x₂ − 3x₁ + 3x₂²",
                (x1, x2) -> 2 * x1 * x1 + 3 * x1 * x2 - 3 * x1 + 3 * x2 * x2,
                (x1, x2) -> new double[]{4 * x1 + 3 * x2 - 3, 3 * x1 + 6 * x2});
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "2x₁ + x₂ ≤ 12", 2, 1, -12));
        c.add(LinearConstraint.le("c2", "−x₁ + x₂ ≤ 4", -1, 1, -4));
        c.add(LinearConstraint.le("c3", "3x₁ − x₂ ≤ 12", 3, -1, -12));
        return new ProblemDefinition(12, "Вариант 12",
                "MIN F = 2x₁² + 3x₁x₂ − 3x₁ + 3x₂²", true, f, c,
                0, 6, 0, 5, false, null);
    }

    private static ProblemDefinition variant13() {
        ObjectiveFunction f = obj("x₁² − 2x₂ − 2x₁ + x₂² + 2",
                (x1, x2) -> x1 * x1 - 2 * x2 - 2 * x1 + x2 * x2 + 2,
                (x1, x2) -> new double[]{2 * x1 - 2, 2 * x2 - 2});
        List<Constraint> c = new ArrayList<Constraint>();
        c.add(LinearConstraint.le("x1_lb", "x₁ ≥ 1", -1, 0, 1));
        c.add(LinearConstraint.le("x2_ub", "x₂ ≤ 3", 0, 1, -3));
        c.add(LinearConstraint.le("c1", "2x₁ + x₂ ≤ 8", 2, 1, -8));
        c.add(LinearConstraint.le("c2", "−x₁ + x₂ ≤ 2", -1, 1, -2));
        return new ProblemDefinition(13, "Вариант 13",
                "MIN F = x₁² − 2x₂ − 2x₁ + x₂² + 2", true, f, c,
                0, 4, 0, 4, false, null);
    }

    private static ProblemDefinition variant14() {
        ObjectiveFunction raw = obj("−x₁² + 18x₂ + 6x₁ − 4x₂²",
                (x1, x2) -> -x1 * x1 + 18 * x2 + 6 * x1 - 4 * x2 * x2,
                (x1, x2) -> new double[]{-2 * x1 + 6, 18 - 8 * x2});
        ObjectiveFunction signed = maximizeWrapper(raw);
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "−3x₁ + 6x₂ ≤ 9", -3, 6, -9));
        c.add(LinearConstraint.le("c2", "−x₁ + x₂ ≤ 1", -1, 1, -1));
        return new ProblemDefinition(14, "Вариант 14",
                "MAX F = −x₁² + 18x₂ + 6x₁ − 4x₂²", false, raw, c,
                0, 4, 0, 5, false, null);
    }

    private static ProblemDefinition variant15() {
        ObjectiveFunction raw = obj("5 − x₁² − 2x₂ − x₁ − 10x₂²",
                (x1, x2) -> 5 - x1 * x1 - 2 * x2 - x1 - 10 * x2 * x2,
                (x1, x2) -> new double[]{-2 * x1 - 1, -2 - 20 * x2});
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "x₁ + x₂ ≤ 6", 1, 1, -6));
        c.add(new NonlinearConstraint("c2", "x₂ − x₁² ≤ 4", ConstraintType.INEQUALITY,
                (x1, x2) -> x2 - x1 * x1 - 4,
                (x1, x2) -> new double[]{-2 * x1, 1}));
        return new ProblemDefinition(15, "Вариант 15",
                "MAX F = 5 − x₁² − 2x₂ − x₁ − 10x₂²", false, raw, c,
                0, 4, 0, 4, false, null);
    }

    private static ProblemDefinition variant16() {
        ObjectiveFunction f = obj("3 − 200x₁² − x₂ + 2x₁ + x₂²",
                (x1, x2) -> 3 - 200 * x1 * x1 - x2 + 2 * x1 + x2 * x2,
                (x1, x2) -> new double[]{-400 * x1 + 2, -1 + 2 * x2});
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "2x₁ + x₂ ≤ 12", 2, 1, -12));
        c.add(LinearConstraint.le("c2", "−x₁ + 2x₂ ≤ 4", -1, 2, -4));
        return new ProblemDefinition(16, "Вариант 16",
                "MIN F = 3 − 200x₁² − x₂ + 2x₁ + x₂²", true, f, c,
                0, 2, 0, 5, false, null);
    }

    private static ProblemDefinition variant17() {
        ObjectiveFunction raw = obj("(x₁−3)² + (x₂−5)²",
                (x1, x2) -> Math.pow(x1 - 3, 2) + Math.pow(x2 - 5, 2),
                (x1, x2) -> new double[]{2 * (x1 - 3), 2 * (x2 - 5)});
        List<Constraint> c = boxNonneg();
        c.add(new NonlinearConstraint("c1", "x₁² − x₂ ≤ 0", ConstraintType.INEQUALITY,
                (x1, x2) -> x1 * x1 - x2,
                (x1, x2) -> new double[]{2 * x1, -1}));
        c.add(LinearConstraint.le("c2", "−x₁ ≤ 1 → x₁ ≥ −1", -1, 0, -1));
        c.add(LinearConstraint.le("c3", "x₁ + 2x₂ ≤ 0", 1, 2, 0));
        return new ProblemDefinition(17, "Вариант 17",
                "MAX F = (x₁−3)² + (x₂−5)²", false, raw, c,
                0, 2, 0, 3, false, null);
    }

    private static ProblemDefinition variant18() {
        ObjectiveFunction f = obj("2x₁² − 2x₁x₂ − 4x₁ + x₂² − 6x₂",
                (x1, x2) -> 2 * x1 * x1 - 2 * x1 * x2 - 4 * x1 + x2 * x2 - 6 * x2,
                (x1, x2) -> new double[]{4 * x1 - 2 * x2 - 4, -2 * x1 + 2 * x2 - 6});
        List<Constraint> c = boxNonneg();
        c.add(LinearConstraint.le("c1", "x₁ + x₂ ≤ 8", 1, 1, -8));
        c.add(LinearConstraint.le("c2", "−x₁ + 2x₂ ≤ 10", -1, 2, -10));
        return new ProblemDefinition(18, "Вариант 18",
                "MIN F = 2x₁² − 2x₁x₂ − 4x₁ + x₂² − 6x₂", true, f, c,
                0, 6, 0, 6, false, null);
    }

    private static ObjectiveFunction quadForm(double a11, double a12, double a22, double b1, double b2) {
        return obj("x₁² + 2x₁x₂ + x₂² + 2x₁ + 6x₂",
                (x1, x2) -> a11 * x1 * x1 + a12 * x1 * x2 + a22 * x2 * x2 + b1 * x1 + b2 * x2,
                (x1, x2) -> new double[]{2 * a11 * x1 + a12 * x2 + b1, a12 * x1 + 2 * a22 * x2 + b2});
    }

    private static ObjectiveFunction maximizeWrapper(ObjectiveFunction raw) {
        return raw;
    }
}
