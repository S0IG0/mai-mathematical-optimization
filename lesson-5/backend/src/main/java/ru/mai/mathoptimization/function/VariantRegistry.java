package ru.mai.mathoptimization.function;

import ru.mai.mathoptimization.dto.ExperimentPresetDto;
import ru.mai.mathoptimization.dto.InitialPointPreset;
import ru.mai.mathoptimization.dto.VariantDto;
import ru.mai.mathoptimization.problem.EqualityConstraint;
import ru.mai.mathoptimization.problem.InequalityConstraint;
import ru.mai.mathoptimization.problem.MethodKind;
import ru.mai.mathoptimization.problem.OptimizerKind;
import ru.mai.mathoptimization.problem.ProblemDefinition;

import java.util.ArrayList;
import java.util.List;

public final class VariantRegistry {

    private static final List<VariantDto> VARIANTS = new ArrayList<VariantDto>();
    private static final List<ProblemDefinition> PROBLEMS = new ArrayList<ProblemDefinition>();

    static {
        registerAll();
    }

    private VariantRegistry() {
    }

    private static void registerAll() {
        // 1 — штраф, сравнение множеств X
        register(penalty(1, OptimizerKind.NELDER_MEAD,
                "4x₁² − 5x₁x₂ + x₂²",
                (x1, x2) -> 4 * x1 * x1 - 5 * x1 * x2 + x2 * x2)
                .plotBounds(-0.5, 5, -0.5, 5)
                .addIneq("g₁", "x₁² − x₂ + 2 ≤ 0", (x1, x2) -> x1 * x1 - x2 + 2)
                .addIneq("g₂", "x₁ + x₂ − 6 ≤ 0", (x1, x2) -> x1 + x2 - 6)
                .addIneq("x₁≥0", "x₁ ≥ 0", (x1, x2) -> -x1)
                .addIneq("x₂≥0", "x₂ ≥ 0", (x1, x2) -> -x2),
                new InitialPointPreset[]{
                        ip("(0; 0)", 0, 0), ip("(1; 1)", 1, 1), ip("(2; 2)", 2, 2)
                },
                exp("μ = 0.1, 1, 100", new double[]{0.1, 1, 100}, "INCREASING", "ALL_CONSTRAINTS"),
                exp("μ = 1, 100, 10000", new double[]{1, 100, 10000}, "INCREASING", "ALL_CONSTRAINTS"),
                exp("X = ℝⁿ", new double[]{0.1, 1, 100}, "INCREASING", "UNCONSTRAINED_X"),
                exp("X: x₁,x₂≥0", new double[]{0.1, 1, 100}, "INCREASING", "NONNEGATIVE"),
                exp("X: g₂ и x≥0", new double[]{0.1, 1, 100}, "INCREASING", "PARTIAL_X"));

        // 2 — барьер
        register(barrier(2, OptimizerKind.NELDER_MEAD,
                "(x₁−5)² + (x₂−3)²",
                (x1, x2) -> (x1 - 5) * (x1 - 5) + (x2 - 3) * (x2 - 3))
                .plotBounds(-0.5, 5.5, -0.5, 4)
                .addIneq("g₁", "−x₁ + 2x₂ ≤ 4", (x1, x2) -> -x1 + 2 * x2 - 4)
                .addIneq("g₂", "x₁ + x₂ ≤ 3", (x1, x2) -> x1 + x2 - 3),
                ip("(0.1; 0.1)", 0.1, 0.1), ip("(0.5; 0.5)", 0.5, 0.5),
                exp("μ: 10→0.01", new double[]{10, 1, 0.1, 0.01}, "DECREASING", "ALL_CONSTRAINTS"));

        // 3 — барьер, циклический спуск
        register(barrier(3, OptimizerKind.CYCLIC_COORDINATE,
                "(x₁−6)² + (x₂+8)²",
                (x1, x2) -> (x1 - 6) * (x1 - 6) + (x2 + 8) * (x2 + 8))
                .plotBounds(-1, 4, 0, 14)
                .addIneq("g", "x₁² − x₂ ≤ 0", (x1, x2) -> x1 * x1 - x2),
                ip("(0; 12)", 0, 12), ip("(1; 5)", 1, 5),
                exp("μ₁=10, μ₂=0.01", new double[]{10, 0.01}, "TWO_STEP", "ALL_CONSTRAINTS"),
                exp("μ=0.01 из X₁", new double[]{0.01}, "SINGLE", "ALL_CONSTRAINTS"),
                exp("μ: 10→0.01", new double[]{10, 1, 0.1, 0.01}, "DECREASING", "ALL_CONSTRAINTS"),
                exp("μ=0.001", new double[]{0.001}, "SINGLE", "ALL_CONSTRAINTS"));

        // 4 — штраф, сопряжённые градиенты
        register(penalty(4, OptimizerKind.CONJUGATE_GRADIENT,
                "e^x₁ + x₁² + 2x₁x₂ + 4x₂⁴",
                (x1, x2) -> Math.exp(x1) + x1 * x1 + 2 * x1 * x2 + 4 * Math.pow(x2, 4))
                .plotBounds(-1, 4, -1, 4)
                .addEq("h", "x₁ + 2x₂ − 6 = 0", (x1, x2) -> x1 + 2 * x2 - 6),
                ip("(1; 1)", 1, 1), ip("(2; 2)", 2, 2),
                exp("μ=10, CG", new double[]{10}, "SINGLE", "ALL_CONSTRAINTS"),
                exp("μ=0.1,1,100", new double[]{0.1, 1, 100}, "INCREASING", "ALL_CONSTRAINTS"));

        // 5 — штраф, Хук–Дживс
        register(penalty(5, OptimizerKind.HOOKE_JEEVES,
                "x₁² + x₂²",
                (x1, x2) -> x1 * x1 + x2 * x2)
                .plotBounds(-0.5, 3, -0.5, 7)
                .addIneq("g₁", "2x₁ + x₂ − 2 ≤ 0", (x1, x2) -> 2 * x1 + x2 - 2)
                .addIneq("g₂", "−x₂ + 1 ≤ 0", (x1, x2) -> -x2 + 1),
                ip("(2; 6)", 2, 6), ip("(1; 1)", 1, 1),
                exp("μ=1,10,100", new double[]{1, 10, 100}, "INCREASING", "ALL_CONSTRAINTS"),
                exp("μ=0.1,1,10,100", new double[]{0.1, 1, 10, 100}, "INCREASING", "ALL_CONSTRAINTS"));

        // 6 — штраф, неограниченность + box
        register(penalty(6, OptimizerKind.NELDER_MEAD,
                "x₁³ + x₂³",
                (x1, x2) -> x1 * x1 * x1 + x2 * x2 * x2)
                .plotBounds(-1.2, 1.2, -1.2, 1.2)
                .addEq("h", "x₁ + x₂ − 1 = 0", (x1, x2) -> x1 + x2 - 1)
                .boundedBox(-0.99, 0.99, -0.99, 0.99),
                ip("(0; 0)", 0, 0), ip("(0.5; 0.5)", 0.5, 0.5),
                exp("μ=1,10,100", new double[]{1, 10, 100}, "INCREASING", "ALL_CONSTRAINTS"),
                exp("|xᵢ|<1", new double[]{1, 10, 100}, "INCREASING", "BOUNDED_BOX"));

        // 7 — штраф max, циклический
        register(penalty(7, OptimizerKind.CYCLIC_COORDINATE,
                "(x₁−1)² + (x₂+5)²",
                (x1, x2) -> (x1 - 1) * (x1 - 1) + (x2 + 5) * (x2 + 5))
                .plotBounds(-1, 3, -8, 0)
                .addIneq("g", "x₁² − x₂ ≤ 0", (x1, x2) -> x1 * x1 - x2),
                ip("(0; −4)", 0, -4), ip("(1; −5) — безусл. оптимум", 1, -5),
                exp("μ: 0.1→100", new double[]{0.1, 100}, "TWO_STEP", "ALL_CONSTRAINTS"),
                exp("μ=100 из (1;−5)", new double[]{100}, "SINGLE", "FROM_UNCONSTRAINED_OPTIMUM"),
                exp("μ=0.1,1,10,100", new double[]{0.1, 1, 10, 100}, "INCREASING", "ALL_CONSTRAINTS"),
                exp("μ=100 из X₁", new double[]{100}, "SINGLE", "ALL_CONSTRAINTS"));

        // 8 — барьер
        register(barrier(8, OptimizerKind.NELDER_MEAD,
                "e^x₁ − x₁x₂ + x₂²",
                (x1, x2) -> Math.exp(x1) - x1 * x2 + x2 * x2)
                .plotBounds(-2.5, 2.5, -2.5, 2.5)
                .addEq("h", "x₁² + x₂² = 4", (x1, x2) -> x1 * x1 + x2 * x2 - 4)
                .addIneq("g", "2x₁ + x₂ ≤ 2", (x1, x2) -> 2 * x1 + x2 - 2),
                ip("(0.1; 0.1)", 0.1, 0.1), ip("(1; 0)", 1, 0),
                exp("μ: 10→0.01", new double[]{10, 1, 0.1, 0.01}, "DECREASING", "ALL_CONSTRAINTS"));

        // 9 — штраф (как 8)
        register(penalty(9, OptimizerKind.NELDER_MEAD,
                "e^x₁ − x₁x₂ + x₂²",
                (x1, x2) -> Math.exp(x1) - x1 * x2 + x2 * x2)
                .plotBounds(-2.5, 2.5, -2.5, 2.5)
                .addEq("h", "x₁² + x₂² = 4", (x1, x2) -> x1 * x1 + x2 * x2 - 4)
                .addIneq("g", "2x₁ + x₂ ≤ 2", (x1, x2) -> 2 * x1 + x2 - 2),
                ip("(1; 1)", 1, 1), ip("(0; 1)", 0, 1),
                exp("μ=10", new double[]{10}, "SINGLE", "ALL_CONSTRAINTS"),
                exp("μ=0.1,1,100", new double[]{0.1, 1, 100}, "INCREASING", "ALL_CONSTRAINTS"));

        // 10 — барьер
        register(barrier(10, OptimizerKind.NELDER_MEAD,
                "−x₁² + 2x₁x₂ + x₂² + e^(−x₁−x₂)",
                (x1, x2) -> -x1 * x1 + 2 * x1 * x2 + x2 * x2 + Math.exp(-x1 - x2))
                .plotBounds(-1, 3, -1, 3)
                .addEq("h", "x₁ + x₂ − 4 = 0", (x1, x2) -> x1 + x2 - 4)
                .addIneq("g", "x₁ + x₂ ≤ 1", (x1, x2) -> x1 + x2 - 1),
                ip("(0.1; 0.1)", 0.1, 0.1), ip("(0.2; 0.3)", 0.2, 0.3),
                exp("μ: 10→0.01", new double[]{10, 1, 0.1, 0.01}, "DECREASING", "ALL_CONSTRAINTS"));

        // 11 — штраф
        register(penalty(11, OptimizerKind.NELDER_MEAD,
                "x₁² + 2x₁x₂ + x₂² + 2x₁ + 6x₂",
                (x1, x2) -> x1 * x1 + 2 * x1 * x2 + x2 * x2 + 2 * x1 + 6 * x2)
                .plotBounds(-0.5, 5, -0.5, 5)
                .addIneq("g₁", "x₁ + x₂ ≥ 3", (x1, x2) -> 3 - x1 - x2)
                .addIneq("x₁≥0", "x₁ ≥ 0", (x1, x2) -> -x1)
                .addIneq("x₂≥0", "x₂ ≥ 0", (x1, x2) -> -x2),
                ip("(1; 1)", 1, 1), ip("(2; 2)", 2, 2),
                exp("μ=100", new double[]{100}, "SINGLE", "ALL_CONSTRAINTS"),
                exp("μ=0.1,1,10", new double[]{0.1, 1, 10}, "INCREASING", "ALL_CONSTRAINTS"));

        // 12 — барьер
        register(barrier(12, OptimizerKind.NELDER_MEAD,
                "x₁² − x₁x₂ + x₂² + 9x₁ − 6x₂ + 20",
                (x1, x2) -> x1 * x1 - x1 * x2 + x2 * x2 + 9 * x1 - 6 * x2 + 20)
                .plotBounds(-0.5, 5, -0.5, 5)
                .addIneq("g₁", "x₁ + x₂ ≥ 3", (x1, x2) -> 3 - x1 - x2)
                .addIneq("x₁≥0", "x₁ ≥ 0", (x1, x2) -> -x1)
                .addIneq("x₂≥0", "x₂ ≥ 0", (x1, x2) -> -x2),
                ip("(0.5; 0.5)", 0.5, 0.5), ip("(1; 2)", 1, 2),
                exp("μ: 10→0.01", new double[]{10, 1, 0.1, 0.01}, "DECREASING", "ALL_CONSTRAINTS"));

        // 13 — штраф
        register(penalty(13, OptimizerKind.NELDER_MEAD,
                "−x₁² − x₂² + 10x₁ + 16x₂",
                (x1, x2) -> -x1 * x1 - x2 * x2 + 10 * x1 + 16 * x2)
                .plotBounds(-1, 10, -1, 12)
                .addIneq("g₁", "x₁ + 2x₂ ≤ 21", (x1, x2) -> x1 + 2 * x2 - 21)
                .addIneq("g₂", "5x₁ + 2x₂ ≤ 42", (x1, x2) -> 5 * x1 + 2 * x2 - 42)
                .addIneq("x₁≥0", "x₁ ≥ 0", (x1, x2) -> -x1)
                .addIneq("x₂≥0", "x₂ ≥ 0", (x1, x2) -> -x2),
                ip("(1; 1)", 1, 1), ip("(5; 5)", 5, 5),
                exp("μ=100", new double[]{100}, "SINGLE", "ALL_CONSTRAINTS"),
                exp("μ=0.1,1,10", new double[]{0.1, 1, 10}, "INCREASING", "ALL_CONSTRAINTS"));

        // 14 — барьер
        register(barrier(14, OptimizerKind.NELDER_MEAD,
                "3x₁² + 2x₂² − 3x₁ + 1",
                (x1, x2) -> 3 * x1 * x1 + 2 * x2 * x2 - 3 * x1 + 1)
                .plotBounds(-0.5, 2.5, -0.5, 2.5)
                .addIneq("g", "x₁² + x₂² − 4 ≤ 0", (x1, x2) -> x1 * x1 + x2 * x2 - 4)
                .addIneq("x₁≥0", "x₁ ≥ 0", (x1, x2) -> -x1)
                .addIneq("x₂≥0", "x₂ ≥ 0", (x1, x2) -> -x2),
                ip("(0.5; 0.5)", 0.5, 0.5), ip("(1; 1)", 1, 1),
                exp("μ: 10→0.01", new double[]{10, 1, 0.1, 0.01}, "DECREASING", "ALL_CONSTRAINTS"));

        // 15 — штраф
        register(penalty(15, OptimizerKind.NELDER_MEAD,
                "(x₁−2)² + (x₂−3)²",
                (x1, x2) -> (x1 - 2) * (x1 - 2) + (x2 - 3) * (x2 - 3))
                .plotBounds(-6, 6, -6, 6)
                .addEq("h", "x₁² + x₂² − 25 = 0", (x1, x2) -> x1 * x1 + x2 * x2 - 25),
                ip("(1; 1)", 1, 1), ip("(2; 1)", 2, 1),
                exp("μ=100", new double[]{100}, "SINGLE", "ALL_CONSTRAINTS"),
                exp("μ=0.1,1,10", new double[]{0.1, 1, 10}, "INCREASING", "ALL_CONSTRAINTS"));

        // 16 — штраф (максимизация → минимизация −F)
        register(penalty(16, OptimizerKind.NELDER_MEAD,
                "−(x₁−4)² − (x₂−4)²",
                (x1, x2) -> -((x1 - 4) * (x1 - 4) + (x2 - 4) * (x2 - 4)))
                .plotBounds(-0.5, 5, -0.5, 5)
                .addIneq("g₁", "x₁ − 3 ≤ 0", (x1, x2) -> x1 - 3)
                .addIneq("g₂", "−x₁ + x₂ − 2 ≤ 0", (x1, x2) -> -x1 + x2 - 2)
                .addIneq("g₃", "x₁ + x₂ − 4 ≤ 0", (x1, x2) -> x1 + x2 - 4)
                .addIneq("x₁≥0", "x₁ ≥ 0", (x1, x2) -> -x1)
                .addIneq("x₂≥0", "x₂ ≥ 0", (x1, x2) -> -x2),
                ip("(1; 1)", 1, 1), ip("(2; 2)", 2, 2),
                exp("μ=100", new double[]{100}, "SINGLE", "ALL_CONSTRAINTS"),
                exp("μ=0.1,1,10", new double[]{0.1, 1, 10}, "INCREASING", "ALL_CONSTRAINTS"));

        // 17 — штраф
        register(penalty(17, OptimizerKind.NELDER_MEAD,
                "−(x₁−4)² − (x₂−4)² − 4x₁ + 5x₂ − 13",
                (x1, x2) -> -((x1 - 4) * (x1 - 4) + (x2 - 4) * (x2 - 4)) - 4 * x1 + 5 * x2 - 13)
                .plotBounds(-0.5, 5, -0.5, 5)
                .addIneq("g", "x₁ + x₂ − 4 ≤ 0", (x1, x2) -> x1 + x2 - 4)
                .addIneq("x₁≥0", "x₁ ≥ 0", (x1, x2) -> -x1)
                .addIneq("x₂≥0", "x₂ ≥ 0", (x1, x2) -> -x2),
                ip("(1; 1)", 1, 1), ip("(2; 1)", 2, 1),
                exp("μ=100", new double[]{100}, "SINGLE", "ALL_CONSTRAINTS"),
                exp("μ=0.1,1,10", new double[]{0.1, 1, 10}, "INCREASING", "ALL_CONSTRAINTS"));

        // 18 — штраф
        register(penalty(18, OptimizerKind.NELDER_MEAD,
                "(x₁−2)² + x₂²/4",
                (x1, x2) -> (x1 - 2) * (x1 - 2) + x2 * x2 / 4)
                .plotBounds(-1, 4, -2, 3)
                .addEq("h₁", "x₁ − (7/2)x₂² − 1 = 0", (x1, x2) -> x1 - 3.5 * x2 * x2 - 1)
                .addEq("h₂", "2x₁ + 3x₂ − 4 = 0", (x1, x2) -> 2 * x1 + 3 * x2 - 4),
                ip("(1; 0)", 1, 0), ip("(2; 0.5)", 2, 0.5),
                exp("μ=100", new double[]{100}, "SINGLE", "ALL_CONSTRAINTS"),
                exp("μ=0.1,1,10", new double[]{0.1, 1, 10}, "INCREASING", "ALL_CONSTRAINTS"));
    }

    private static ProblemDefinition penalty(int id, OptimizerKind opt, String formula,
                                             ProblemDefinition.ObjectiveEvaluator fn) {
        return new ProblemDefinition(id, MethodKind.PENALTY, opt, formula, fn);
    }

    private static ProblemDefinition barrier(int id, OptimizerKind opt, String formula,
                                             ProblemDefinition.ObjectiveEvaluator fn) {
        return new ProblemDefinition(id, MethodKind.BARRIER, opt, formula, fn);
    }

    private static InitialPointPreset ip(String label, double x1, double x2) {
        return new InitialPointPreset(label, new double[]{x1, x2});
    }

    private static ExperimentPresetDto exp(String label, double[] mus, String schedule, String domainMode) {
        ExperimentPresetDto e = new ExperimentPresetDto();
        e.setLabel(label);
        e.setMuValues(mus);
        e.setSchedule(schedule);
        e.setDomainMode(domainMode);
        return e;
    }

    private static void register(ProblemDefinition problem, InitialPointPreset[] points,
                                 ExperimentPresetDto... experiments) {
        PROBLEMS.add(problem);
        VariantDto dto = new VariantDto();
        dto.setId(problem.getId());
        dto.setTitle("Вариант " + problem.getId());
        dto.setMethodKind(problem.getMethodKind().name());
        dto.setMethodLabel(problem.getMethodKind() == MethodKind.PENALTY
                ? "Метод штрафных функций" : "Метод барьерных функций");
        dto.setOptimizerKind(problem.getOptimizerKind().name());
        dto.setObjectiveFormula(problem.getObjectiveFormula());
        dto.setPlotXMin(problem.getPlotXMin());
        dto.setPlotXMax(problem.getPlotXMax());
        dto.setPlotYMin(problem.getPlotYMin());
        dto.setPlotYMax(problem.getPlotYMax());
        dto.setBoundedBox(problem.isBoundedBox());
        for (InequalityConstraint ic : problem.getInequalities()) {
            dto.getInequalities().add(ic.getFormula());
        }
        for (EqualityConstraint ec : problem.getEqualities()) {
            dto.getEqualities().add(ec.getFormula());
        }
        for (InitialPointPreset p : points) {
            dto.getInitialPoints().add(p);
        }
        for (ExperimentPresetDto ex : experiments) {
            dto.getExperiments().add(ex);
        }
        VARIANTS.add(dto);
    }

    private static void register(ProblemDefinition problem, InitialPointPreset p1,
                                 InitialPointPreset p2, ExperimentPresetDto... experiments) {
        register(problem, new InitialPointPreset[]{p1, p2}, experiments);
    }

    public static List<VariantDto> getVariants() {
        return VARIANTS;
    }

    public static VariantDto getVariant(int id) {
        for (VariantDto v : VARIANTS) {
            if (v.getId() == id) {
                return v;
            }
        }
        throw new IllegalArgumentException("Вариант " + id + " не найден");
    }

    public static ProblemDefinition getProblem(int id) {
        for (ProblemDefinition p : PROBLEMS) {
            if (p.getId() == id) {
                return p;
            }
        }
        throw new IllegalArgumentException("Вариант " + id + " не найден");
    }
}
