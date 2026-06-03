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
        registerFunctions();
        registerVariants();
    }

    private VariantRegistry() {
    }

    private static void registerFunctions() {
        put("1:F1", MathFunctions.f1Variant1());
        put("1:F2", MathFunctions.f2Variant1());
        put("2:F1", MathFunctions.f1Variant2());
        put("2:F2", MathFunctions.f1Variant1());
        put("3:F1", MathFunctions.f1Variant3());
        put("3:F2", MathFunctions.f2Variant3());
        put("4:F1", MathFunctions.f1Variant3());
        put("4:F2", MathFunctions.f2Variant4());
        put("5:F1", MathFunctions.f1Variant5());
        put("5:F2", MathFunctions.f2Variant5());
        put("6:F1", MathFunctions.f1Variant6());
        put("6:F2", MathFunctions.f2Variant6());
        put("7:F1", MathFunctions.f1Variant3());
        put("7:F2", MathFunctions.f2Variant3());
        put("8:F1", MathFunctions.f1Variant2());
        put("8:F2", MathFunctions.f1Variant1());
        put("9:F1", MathFunctions.f1Variant1());
        put("9:F2", MathFunctions.f2Variant1());
        put("10:F1", MathFunctions.f1Variant6());
        put("10:F2", MathFunctions.f2Variant6());
        put("11:F1", MathFunctions.f1Variant3());
        put("11:F2", MathFunctions.f2Variant4());
        put("12:F1", MathFunctions.f1Variant6());
        put("12:F2", MathFunctions.f2Variant6());
        put("13:F1", MathFunctions.f1Variant3());
        put("13:F2", MathFunctions.f2Variant3());
        put("14:F1", MathFunctions.f1Variant1());
        put("14:F2", MathFunctions.f2Variant1());
        put("15:F1", MathFunctions.f1Variant6());
        put("15:F2", MathFunctions.f2Variant6());
        put("16:F1", MathFunctions.f1Variant5());
        put("16:F2", MathFunctions.f2Variant5());
    }

    private static void put(String key, MultidimFunction fn) {
        FUNCTIONS.put(key, fn);
    }

    private static void registerVariants() {
        VARIANTS.add(v(1, "ROSENBROCK_DISCRETE", "Метод Розенброка (дискретный шаг)", false,
                f2("F1", "9x₁² + 16x₂² − 90x₁ − 128x₂", -2, 12, -2, 12, ip("(0; 0)", 0, 0)),
                f3("F2", "x₁² + 2x₁x₂ + 2x₂² + x₃² − x₂x₃ + x₁ + 3x₂ − x₃",
                        -1.5, 2.5, -2, 1.5, ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(2, "ROSENBROCK_CONTINUOUS", "Метод Розенброка (непрерывный шаг)", false,
                f2("F1", "(3x₁² − x₂)² + (2x₁ − 3x₂)²", -1, 3, -1, 3, ip("(0; 1)", 0, 1)),
                f2("F2", "9x₁² + 16x₂² − 90x₁ − 128x₂", -2, 12, -2, 12, ip("(0; 0)", 0, 0))));

        VARIANTS.add(v(3, "HOOKE_JEEVES_CONTINUOUS", "Метод Хука и Дживса (непрерывный шаг)", false,
                f2("F1", "−6x₁ − 4x₂ + x₁² + x₂² + 18", -1, 8, -1, 8, ip("(1; 1)", 1, 1)),
                f3("F2", "x₁⁴ + 2x₁³ + (x₂ − 4)² + 2x₃² + 8x₃",
                        ip("(1; 0; 1)", 1, 0, 1), ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(4, "HOOKE_JEEVES_DISCRETE", "Метод Хука и Дживса (дискретный шаг)", false,
                f2("F1", "−6x₁ − 4x₂ + x₁² + x₂² + 18", -1, 8, -1, 8, ip("(1; 1)", 1, 1)),
                f3("F2", "4x₁² + 3x₂² + x₃² + 4x₁x₂ − 2x₂x₃ − 16x₁ − 4x₃", ip("(1; 1; 1)", 1, 1, 1))));

        VARIANTS.add(v(5, "GAUSS_SEIDEL_CONTINUOUS", "Метод Гаусса–Зейделя (непрерывный шаг)", true,
                f2("F1", "−x₁² − x₂² + x₁x₂ − x₁ + 2x₂", -3, 4, -3, 4, ip("(0; 0)", 0, 0)),
                f2("F2", "(x₁ − x₂)² + (x₂ − 2)²", -2, 4, -1, 4, ip("(1; 0)", 1, 0))));

        VARIANTS.add(v(6, "GAUSS_SEIDEL_DISCRETE", "Метод Гаусса–Зейделя (дискретный шаг)", true,
                f2("F1", "x₁² + 2x₂² − 4x₁ + 2x₂", -1, 5, -1, 5, ip("(1; 0)", 1, 0)),
                f2("F2", "(x₁ − 2)⁴ + (x₁ − 2x₂)²", -1, 4, -1, 4, ip("(0; 1)", 0, 1))));

        VARIANTS.add(v(7, "ROSENBROCK_DISCRETE", "Метод Розенброка (дискретный шаг)", false,
                f2("F1", "−6x₁ − 4x₂ + x₁² + x₂² + 18", -1, 8, -1, 8, ip("(1; 1)", 1, 1)),
                f3("F2", "x₁⁴ + 2x₁³ + (x₂ − 4)² + 2x₃² + 8x₃",
                        ip("(1; 0; 1)", 1, 0, 1), ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(8, "ROSENBROCK_CONTINUOUS", "Метод Розенброка (непрерывный шаг)", false,
                f2("F1", "(3x₁² − x₂)² + (2x₁ − 3x₂)²", -1, 3, -1, 3, ip("(0; 1)", 0, 1)),
                f2("F2", "9x₁² + 16x₂² − 90x₁ − 128x₂", -2, 12, -2, 12, ip("(0; 0)", 0, 0))));

        VARIANTS.add(v(9, "HOOKE_JEEVES_CONTINUOUS", "Метод Хука и Дживса (непрерывный шаг)", false,
                f2("F1", "9x₁² + 16x₂² − 90x₁ − 128x₂", -2, 12, -2, 12, ip("(0; 0)", 0, 0)),
                f3("F2", "x₁² + 2x₁x₂ + 2x₂² + x₃² − x₂x₃ + x₁ + 3x₂ − x₃", ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(10, "HOOKE_JEEVES_DISCRETE", "Метод Хука и Дживса (дискретный шаг)", false,
                f2("F1", "x₁² + 2x₂² − 4x₁ + 2x₂", -1, 5, -1, 5, ip("(1; 0)", 1, 0)),
                f2("F2", "(x₁ − 2)⁴ + (x₁ − 2x₂)²", -1, 4, -1, 4, ip("(0; 1)", 0, 1))));

        VARIANTS.add(v(11, "GAUSS_SEIDEL_CONTINUOUS", "Метод Гаусса–Зейделя (непрерывный шаг)", true,
                f2("F1", "−6x₁ − 4x₂ + x₁² + x₂² + 18", -1, 8, -1, 8, ip("(1; 1)", 1, 1)),
                f3("F2", "4x₁² + 3x₂² + x₃² + 4x₁x₂ − 2x₂x₃ − 16x₁ − 4x₃", ip("(1; 1; 1)", 1, 1, 1))));

        VARIANTS.add(v(12, "GAUSS_SEIDEL_DISCRETE", "Метод Гаусса–Зейделя (дискретный шаг)", true,
                f2("F1", "x₁² + 2x₂² − 4x₁ + 2x₂", -1, 5, -1, 5, ip("(1; 0)", 1, 0)),
                f2("F2", "(x₁ − 2)⁴ + (x₁ − 2x₂)²", -1, 4, -1, 4, ip("(0; 1)", 0, 1))));

        VARIANTS.add(v(13, "HOOKE_JEEVES_CONTINUOUS", "Метод Хука и Дживса (непрерывный шаг)", false,
                f2("F1", "−6x₁ − 4x₂ + x₁² + x₂² + 18", -1, 8, -1, 8, ip("(1; 1)", 1, 1)),
                f3("F2", "x₁⁴ + 2x₁³ + (x₂ − 4)² + 2x₃² + 8x₃",
                        ip("(1; 0; 1)", 1, 0, 1), ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(14, "HOOKE_JEEVES_DISCRETE", "Метод Хука и Дживса (дискретный шаг)", false,
                f2("F1", "9x₁² + 16x₂² − 90x₁ − 128x₂", -2, 12, -2, 12, ip("(0; 0)", 0, 0)),
                f3("F2", "x₁² + 2x₁x₂ + 2x₂² + x₃² − x₂x₃ + x₁ + 3x₂ − x₃", ip("(0; 0; 0)", 0, 0, 0))));

        VARIANTS.add(v(15, "GAUSS_SEIDEL_CONTINUOUS", "Метод Гаусса–Зейделя (непрерывный шаг)", true,
                f2("F1", "x₁² + 2x₂² − 4x₁ + 2x₂", -1, 5, -1, 5, ip("(1; 0)", 1, 0)),
                f2("F2", "(x₁ − 2)⁴ + (x₁ − 2x₂)²", -1, 4, -1, 4, ip("(0; 1)", 0, 1))));

        VARIANTS.add(v(16, "GAUSS_SEIDEL_DISCRETE", "Метод Гаусса–Зейделя (дискретный шаг)", true,
                f2("F1", "−x₁² − x₂² + x₁x₂ − x₁ + 2x₂", -3, 4, -3, 4, ip("(0; 0)", 0, 0)),
                f2("F2", "(x₁ − x₂)² + (x₂ − 2)²", -2, 4, -1, 4, ip("(1; 0)", 1, 0))));
    }

    private static InitialPointPreset ip(String label, double... coords) {
        return new InitialPointPreset(label, coords);
    }

    private static FunctionDefinitionDto f2(String id, String formula, double xMin, double xMax,
                                            double yMin, double yMax, InitialPointPreset... presets) {
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
        for (InitialPointPreset p : presets) {
            dto.getInitialPoints().add(p);
        }
        return dto;
    }

    private static FunctionDefinitionDto f3(String id, String formula, InitialPointPreset... presets) {
        return f3(id, formula, -3, 3, -3, 3, presets);
    }

    private static FunctionDefinitionDto f3(String id, String formula,
                                            double xMin, double xMax, double yMin, double yMax,
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
        for (InitialPointPreset p : presets) {
            dto.getInitialPoints().add(p);
        }
        return dto;
    }

    private static VariantDto v(int id, String method, String methodLabel, boolean supports1d,
                                FunctionDefinitionDto f1, FunctionDefinitionDto f2) {
        VariantDto v = new VariantDto();
        v.setId(id);
        v.setTitle("Вариант " + id);
        v.setMethod(method);
        v.setMethodLabel(methodLabel);
        v.setSupportsOneDimensional(supports1d);
        v.setF1(f1);
        v.setF2(f2);
        return v;
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
