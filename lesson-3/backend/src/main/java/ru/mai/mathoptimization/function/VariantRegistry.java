package ru.mai.mathoptimization.function;

import ru.mai.mathoptimization.dto.FunctionDefinitionDto;
import ru.mai.mathoptimization.dto.InitialPointPreset;
import ru.mai.mathoptimization.dto.VariantDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VariantRegistry {

    private static final Map<String, MultidimFunction> FUNCTIONS = new HashMap<String, MultidimFunction>();
    private static final List<VariantDto> VARIANTS = new ArrayList<VariantDto>();

    static {
        for (int v = 1; v <= 18; v++) {
            put(v + ":F1", MathFunctions.f1Variant(v));
            put(v + ":F2", MathFunctions.f2Variant(v));
        }
        registerVariants();
    }

    private VariantRegistry() {
    }

    private static void put(String key, MultidimFunction fn) {
        FUNCTIONS.put(key, fn);
    }

    private static InitialPointPreset ip(String label, double... coords) {
        return new InitialPointPreset(label, coords);
    }

    private static FunctionDefinitionDto f2(String id, String formula, String method, String methodLabel,
                                            boolean minimize, double xMin, double xMax, double yMin, double yMax,
                                            InitialPointPreset... presets) {
        FunctionDefinitionDto dto = new FunctionDefinitionDto();
        dto.setId(id);
        dto.setLabel(id);
        dto.setFormula(formula);
        dto.setDimension(2);
        dto.setPlottable2d(true);
        dto.setPlotXMin(xMin);
        dto.setPlotXMax(xMax);
        dto.setPlotYMin(yMin);
        dto.setPlotYMax(yMax);
        dto.setMethod(method);
        dto.setMethodLabel(methodLabel);
        dto.setMinimize(minimize);
        for (InitialPointPreset p : presets) {
            dto.getInitialPoints().add(p);
        }
        return dto;
    }

    private static FunctionDefinitionDto f3(String id, String formula, String method, String methodLabel,
                                            boolean minimize, double xMin, double xMax, double yMin, double yMax,
                                            InitialPointPreset... presets) {
        FunctionDefinitionDto dto = new FunctionDefinitionDto();
        dto.setId(id);
        dto.setLabel(id);
        dto.setFormula(formula);
        dto.setDimension(3);
        dto.setPlottable2d(false);
        dto.setPlotXMin(xMin);
        dto.setPlotXMax(xMax);
        dto.setPlotYMin(yMin);
        dto.setPlotYMax(yMax);
        dto.setMethod(method);
        dto.setMethodLabel(methodLabel);
        dto.setMinimize(minimize);
        for (InitialPointPreset p : presets) {
            dto.getInitialPoints().add(p);
        }
        return dto;
    }

    private static VariantDto v(int id, FunctionDefinitionDto f1, FunctionDefinitionDto f2) {
        VariantDto v = new VariantDto();
        v.setId(id);
        v.setTitle("Вариант " + id);
        v.setF1(f1);
        v.setF2(f2);
        return v;
    }

    private static void registerVariants() {
        VARIANTS.add(v(1,
                f2("F1", "4x₁ + 2x₂ − x₁² − x₂² + 5", "GRADIENT_FIRST_ORDER",
                        "Градиентный метод 1-го порядка", false, -2, 12, -2, 12, ip("(5; 10)", 5, 10)),
                f2("F2", "100x₁² + 0.2x₂² + x₁ + 2x₂", "RAVINE", "Овражный метод", true,
                        0, 5, 0, 8, ip("(2; 4)", 2, 4))));

        VARIANTS.add(v(2,
                f2("F1", "x₁² + x₂² − x₁x₂ + x₁ − 2x₂", "GRADIENT_FIRST_ORDER",
                        "Градиентный метод 1-го порядка", false, -2, 4, -2, 4, ip("(0; 0)", 0, 0)),
                f2("F2", "−2 − x₁ − 2x₂ − 0.1x₁² − 100x₂²", "CONJUGATE_GRADIENT",
                        "Метод сопряжённых градиентов", false, -1, 3, -1, 3, ip("(1; 1)", 1, 1))));

        VARIANTS.add(v(3,
                f2("F1", "(x₁ − 2)² + (x₂ − 1)²", "STEEPEST_DESCENT", "Метод наискорейшего спуска", true,
                        -1, 5, -1, 4, ip("(0; 0)", 0, 0), ip("(3; 2)", 3, 2), ip("(1; 0)", 1, 0)),
                f3("F2", "−4(x₁−3)² − 2(x₂−1)² − (x₃−2)²", "GRADIENT_SECOND_ORDER",
                        "Градиентный метод 2-го порядка (Ньютон)", false, -1, 5, -1, 4,
                        ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(4,
                f2("F1", "200x₁² + 0.5x₂² + 2x₁ − x₂ + 3", "STEEPEST_DESCENT",
                        "Метод наискорейшего спуска", true, -3, 2, -2, 4, ip("(-1; 1)", -1, 1)),
                f3("F2", "4x₁² + 3x₂² + x₃² − 16x₁ − 4x₃", "GRADIENT_SECOND_ORDER",
                        "Градиентный метод 2-го порядка (Ньютон)", false, -2, 4, -2, 4,
                        ip("(1; 1; 1)", 1, 1, 1))));

        VARIANTS.add(v(5,
                f2("F1", "6x₁ + 32x₂ − 0.1x₁² − 40x₂²", "RAVINE", "Овражный метод", false,
                        -2, 6, -2, 6, ip("(0; 0)", 0, 0), ip("(2; 1)", 2, 1)),
                f2("F2", "2x₁² + x₂² − 12x₁", "GRADIENT_FIRST_ORDER",
                        "Градиентный метод 1-го порядка", true, -6, 4, -2, 8, ip("(-3; 5)", -3, 5))));

        VARIANTS.add(v(6,
                f2("F1", "2x₁ − x₁² − x₂² + 3", "GRADIENT_FIRST_ORDER",
                        "Градиентный метод 1-го порядка", false, -4, 4, -2, 6, ip("(-1; 2)", -1, 2)),
                f2("F2", "−6x₁ + 2x₁² − 2x₁x₂ + 2x₂²", "CONJUGATE_GRADIENT",
                        "Метод сопряжённых градиентов", true, -1, 4, -1, 5, ip("(1; 2)", 1, 2))));

        VARIANTS.add(v(7,
                f2("F1", "x₁² + x₂² − 2x₁ − 2x₂ + 2", "CONJUGATE_GRADIENT",
                        "Метод сопряжённых градиентов", true, -2, 4, -2, 4,
                        ip("(0; 0)", 0, 0), ip("(2; 2)", 2, 2), ip("(3; 1)", 3, 1)),
                f2("F2", "x₁² + 2x₂² − 4x₁ + 2x₂", "STEEPEST_DESCENT",
                        "Метод наискорейшего спуска", false, -2, 6, -2, 4, ip("(1; 0)", 1, 0))));

        VARIANTS.add(v(8,
                f2("F1", "6x₁ + 32x₂ − x₁² − 4x₂²", "CONJUGATE_GRADIENT",
                        "Метод сопряжённых градиентов", false, -2, 8, -2, 8, ip("(0; 0)", 0, 0)),
                f3("F2", "x₁³ + x₂³ + x₃³ + x₂x₃ − 3x₁ + 6x₂ + 2", "GRADIENT_SECOND_ORDER",
                        "Градиентный метод 2-го порядка (Ньютон)", true, -1, 3, -1, 3,
                        ip("(1; 1; 1)", 1, 1, 1))));

        VARIANTS.add(v(9,
                f2("F1", "4x₁ + 8x₂ − 2x₁² − 2x₂² + 2", "STEEPEST_DESCENT",
                        "Метод наискорейшего спуска", false, -2, 6, -2, 6,
                        ip("(0; 0)", 0, 0), ip("(2; 2)", 2, 2)),
                f3("F2", "4x₁ + 6x₂ − 2x₁² − x₂² − x₃² + 11", "GRADIENT_SECOND_ORDER",
                        "Градиентный метод 2-го порядка (Ньютон)", false, -3, 2, -3, 2,
                        ip("(-1; -1; -1)", -1, -1, -1))));

        VARIANTS.add(v(10,
                f2("F1", "−4x₁ + 4x₁² − 6x₁x₂ + 2x₂²", "GRADIENT_FIRST_ORDER",
                        "Градиентный метод 1-го порядка", true, 0, 5, 1, 6, ip("(2; 4)", 2, 4)),
                f3("F2", "x₁⁴ + 2x₁² + (x₂−3)² + 2x₃² + 8x₃", "GRADIENT_SECOND_ORDER",
                        "Градиентный метод 2-го порядка (Ньютон)", false, -2, 3, -1, 5,
                        ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(11,
                f2("F1", "4x₁ + 2x₂² − x₁² − x₂² + 5", "GRADIENT_FIRST_ORDER",
                        "Градиентный метод 1-го порядка", false, -2, 8, -4, 4, ip("(5; -2)", 5, -2)),
                f3("F2", "−x₁⁴ + 2x₁² − x₂² + 2x₃² + 8x₃", "GRADIENT_SECOND_ORDER",
                        "Градиентный метод 2-го порядка (Ньютон)", true, -2, 3, -2, 4,
                        ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(12,
                f2("F1", "−2x₁ + 2x₁² − 8x₁x₂ + 2x₂²", "STEEPEST_DESCENT",
                        "Метод наискорейшего спуска", true, 0, 5, 0, 5, ip("(2; 2)", 2, 2)),
                f3("F2", "−x₁⁴ + 2x₁² − (x₂−2)² + 2x₃² + 8x₃", "GRADIENT_SECOND_ORDER",
                        "Градиентный метод 2-го порядка (Ньютон)", true, -2, 3, -1, 4,
                        ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(13,
                f2("F1", "80x₁² + 0.4x₂² + x₁ + 2x₂", "RAVINE", "Овражный метод", true,
                        0, 5, 0, 8, ip("(2; 4)", 2, 4)),
                f2("F2", "6x₁ + 32x₂ − x₁² − 4x₂²", "CONJUGATE_GRADIENT",
                        "Метод сопряжённых градиентов", false, -2, 8, -2, 8,
                        ip("(0; 0)", 0, 0), ip("(1; 1)", 1, 1))));

        VARIANTS.add(v(14,
                f2("F1", "2x₁ − 4x₁² − x₂² + 3", "CONJUGATE_GRADIENT",
                        "Метод сопряжённых градиентов", false, -3, 2, 0, 6, ip("(-1; 4)", -1, 4)),
                f3("F2", "4(x₁−2)² + 2(x₂−1)² + (x₃−3)² + 2x₃²", "GRADIENT_SECOND_ORDER",
                        "Градиентный метод 2-го порядка (Ньютон)", false, -1, 4, -1, 4,
                        ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(15,
                f2("F1", "2 − 8x₁ + 3x₂ − 0.1x₁² − 100x₂²", "RAVINE", "Овражный метод", false,
                        -1, 3, -1, 3, ip("(1; 1)", 1, 1)),
                f3("F2", "x₁⁴ − 2x₁² − (x₂−3)² + 2x₃² + x₃", "STEEPEST_DESCENT",
                        "Метод наискорейшего спуска", true, -2, 3, -1, 5,
                        ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(16,
                f2("F1", "2 − 2x₁ + 3x₂ − 10x₁² − 2x₁x₂ + 4x₂²", "CONJUGATE_GRADIENT",
                        "Метод сопряжённых градиентов", false, -1, 3, -1, 3, ip("(1; 1)", 1, 1)),
                f3("F2", "−2x₁⁴ + 12x₁² − 5x₂⁴ + 2x₃² + 8x₃", "GRADIENT_SECOND_ORDER",
                        "Градиентный метод 2-го порядка (Ньютон)", true, -2, 3, -2, 3,
                        ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(17,
                f2("F1", "2 − 8x₁ + 3x₂ + x₁⁴ + 10x₂²", "RAVINE", "Овражный метод", false,
                        -1, 3, -1, 3, ip("(1; 1)", 1, 1)),
                f2("F2", "6x₁ + 32x₂ − x₁² − 4x₂²", "CONJUGATE_GRADIENT",
                        "Метод сопряжённых градиентов", false, -2, 8, -2, 8,
                        ip("(0; 0)", 0, 0), ip("(2; 1)", 2, 1))));

        VARIANTS.add(v(18,
                f2("F1", "x₁ − x₁² − 2x₂ + x₁³ + 3x₂³", "RAVINE", "Овражный метод", false,
                        -1, 3, -1, 3, ip("(1; 1)", 1, 1)),
                f3("F2", "x₁³ − 2x₁² − (x₂−3)² + 2x₃² + x₃", "STEEPEST_DESCENT",
                        "Метод наискорейшего спуска", true, -2, 3, -1, 5,
                        ip("(0; 0; 0)", 0, 0, 0))));
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

    public static MultidimFunction getFunction(int variantId, String functionId) {
        String key = variantId + ":" + functionId.toUpperCase();
        MultidimFunction fn = FUNCTIONS.get(key);
        if (fn == null) {
            throw new IllegalArgumentException("Функция " + functionId + " для варианта " + variantId + " не найдена");
        }
        return fn;
    }

    public static FunctionDefinitionDto getFunctionDefinition(int variantId, String functionId) {
        VariantDto variant = getVariant(variantId);
        if ("F2".equalsIgnoreCase(functionId)) {
            return variant.getF2();
        }
        return variant.getF1();
    }
}
