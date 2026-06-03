package ru.mai.mathoptimization.function;

import ru.mai.mathoptimization.dto.FunctionDefinitionDto;
import ru.mai.mathoptimization.dto.IntervalPreset;
import ru.mai.mathoptimization.dto.VariantDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FunctionRegistry {

    private static final Map<String, ObjectiveFunction> FUNCTIONS = new HashMap<String, ObjectiveFunction>();
    private static final List<VariantDto> VARIANTS = new ArrayList<VariantDto>();

    static {
        registerFunctions();
        registerVariants();
    }

    private FunctionRegistry() {
    }

    private static void registerFunctions() {
        FUNCTIONS.put("1:F1", MathFunctions.variant1F1());
        FUNCTIONS.put("1:F2", MathFunctions.variant1F2());
        FUNCTIONS.put("2:F1", MathFunctions.variant2F1());
        FUNCTIONS.put("2:F2", MathFunctions.variant2F2());
        FUNCTIONS.put("3:F1", MathFunctions.variant3F1());
        FUNCTIONS.put("3:F2", MathFunctions.variant3F2());
        FUNCTIONS.put("4:F1", MathFunctions.variant4F1());
        FUNCTIONS.put("4:F2", MathFunctions.variant4F2());
        FUNCTIONS.put("5:F1", MathFunctions.variant5F1());
        FUNCTIONS.put("5:F2", MathFunctions.variant5F2());
        FUNCTIONS.put("6:F1", MathFunctions.variant6F1());
        FUNCTIONS.put("6:F2", MathFunctions.variant6F2());
        FUNCTIONS.put("7:F1", MathFunctions.variant7F1());
        FUNCTIONS.put("7:F2", MathFunctions.variant7F2());
        FUNCTIONS.put("8:F1", MathFunctions.variant8F1());
        FUNCTIONS.put("8:F2", MathFunctions.variant8F2());
        FUNCTIONS.put("9:F1", MathFunctions.variant9F1());
        FUNCTIONS.put("9:F2", MathFunctions.variant9F2());
        FUNCTIONS.put("10:F1", MathFunctions.variant10F1());
        FUNCTIONS.put("10:F2", MathFunctions.variant10F2());
        FUNCTIONS.put("11:F1", MathFunctions.variant11F1());
        FUNCTIONS.put("11:F2", MathFunctions.variant11F2());
        FUNCTIONS.put("12:F1", MathFunctions.variant12F1());
        FUNCTIONS.put("12:F2", MathFunctions.variant12F2());
        FUNCTIONS.put("13:F1", MathFunctions.variant13F1());
        FUNCTIONS.put("13:F2", MathFunctions.variant13F2());
        FUNCTIONS.put("14:F1", MathFunctions.variant14F1());
        FUNCTIONS.put("14:F2", MathFunctions.variant14F2());
        FUNCTIONS.put("15:F1", MathFunctions.variant15F1());
        FUNCTIONS.put("15:F2", MathFunctions.variant15F2());
        FUNCTIONS.put("16:F1", MathFunctions.variant16F1());
        FUNCTIONS.put("16:F2", MathFunctions.variant16F2());
        FUNCTIONS.put("17:F1", MathFunctions.variant17F1());
        FUNCTIONS.put("17:F2", MathFunctions.variant17F2());
        FUNCTIONS.put("18:F1", MathFunctions.variant18F1());
        FUNCTIONS.put("18:F2", MathFunctions.variant18F2());
    }

    private static void registerVariants() {
        VARIANTS.add(buildVariant(1,
                f("F1", "3x − x³ − 1", true, -2, 2,
                        p("MIN [−3; 0]", -3, 0, true),
                        p("MIN [0.8; 5]", 0.8, 5, true),
                        p("MIN [−10; 0.5]", -10, 0.5, true)),
                f("F2", "(4 − x²) / (x(x² + 3))", false, -10, 0,
                        p("MAX [−5; 0]", -5, 0, false),
                        p("MAX [0; 10]", 0, 10, false),
                        p("MIN [−5; 5]", -5, 5, true))));

        VARIANTS.add(buildVariant(2,
                f("F1", "10x − 2x² + 3", false, 0, 10,
                        p("MAX [−10; 10]", -10, 10, false),
                        p("MAX [6; 7]", 6, 7, false),
                        p("MAX [1; 6]", 1, 6, false),
                        p("MAX [−1; 1]", -1, 1, false)),
                f("F2", "(2x² + 3) / (x² + 2x − 8)", false, -4, 2,
                        p("MIN [2; 10]", 2, 10, true),
                        p("MIN [−10; −4]", -10, -4, true))));

        VARIANTS.add(buildVariant(3,
                f("F1", "3x − x³", false, -3, 3,
                        p("MAX [−2; 0.5]", -2, 0.5, false),
                        p("MAX [−1; 5]", -1, 5, false),
                        p("MAX [0; 10]", 0, 10, false)),
                f("F2", "(9 − x²) / (x² + 2x + 3)", true, -10, -3,
                        p("MAX [−3; 3]", -3, 3, false),
                        p("MAX [−10; 10]", -10, 10, false))));

        VARIANTS.add(buildVariant(4,
                f("F1", "2x² − 4x + 5", true, -5, 7,
                        p("MIN [−7; 0.3]", -7, 0.3, true),
                        p("MIN [1; 5]", 1, 5, true)),
                f("F2", "(x² + x + 1) / (x² − x + 1)", false, -0.5, 6,
                        p("MAX [−6; 6]", -6, 6, false),
                        p("MIN [−4; 0]", -4, 0, true),
                        p("MIN [−4; 4]", -4, 4, true))));

        VARIANTS.add(buildVariant(5,
                f("F1", "x³/3 − 2x² + 3x + 5/3", true, -1, 10,
                        p("MIN [−2; 2]", -2, 2, true),
                        p("MIN [−1; 5]", -1, 5, true),
                        p("MIN [−5; 2.5]", -5, 2.5, true)),
                f("F2", "(x² − 2x − 3) / (x⁴ − 5x² + 4)", false, -2, 1,
                        p("MAX [2; 5]", 2, 5, false),
                        p("MIN [−4; −2]", -4, -2, true),
                        p("MIN [−2; 6]", -2, 6, true),
                        p("MIN [1; 2]", 1, 2, true))));

        VARIANTS.add(buildVariant(6,
                f("F1", "(x² + x − 2)² / (x²(x² − x − 2))", false, -1, 2,
                        p("MAX [−1; 0]", -1, 0, false),
                        p("MAX [0; 2]", 0, 2, false),
                        p("MIN [−5; −1]", -5, -1, true),
                        p("MIN [2; 5]", 2, 5, true)),
                f("F2", "x³ − 2x² + x − 1", true, -5, 5,
                        p("MIN [0; 5]", 0, 5, true),
                        p("MIN [−4; 1]", -4, 1, true))));

        VARIANTS.add(buildVariant(7,
                f("F1", "±3/xⁿ (n — чётность ⌊|x|⌋)", true, -2, 2,
                        p("MIN [−3; 0]", -3, 0, true),
                        p("MIN [−1; 5]", -1, 5, true),
                        p("MIN [0; 10]", 0, 10, true)),
                f("F2", "2 − √(x² + 5)", false, -5, 5,
                        p("MAX [−1; 10]", -1, 10, false),
                        p("MAX [3; 8]", 3, 8, false))));

        VARIANTS.add(buildVariant(8,
                f("F1", "(x − 4) / (x − 9)", true, -5, 5,
                        p("MIN [−3; 0]", -3, 0, true),
                        p("MIN [−3; 9]", -3, 9, true),
                        p("MIN [9; 15]", 9, 15, true)),
                f("F2", "|x² − 1|", true, -2, 2,
                        p("MIN [−10; 1]", -10, 1, true),
                        p("MIN [−2; 0]", -2, 0, true),
                        p("MIN [−2; 8]", -2, 8, true))));

        VARIANTS.add(buildVariant(9,
                f("F1", "(x² + 2x − 3) / (x − 2)", true, 2, 10,
                        p("MAX [−6; 2]", -6, 2, false),
                        p("MAX [−5; 6]", -5, 6, false)),
                f("F2", "2 / (x² − 1)", false, -10, 10,
                        p("MAX [0; 5]", 0, 5, false),
                        p("MAX [−4; −1]", -4, -1, false))));

        VARIANTS.add(buildVariant(10,
                f("F1", "x³ / (x² − x − 2)", true, 2, 10,
                        p("MIN [−1; 2]", -1, 2, true),
                        p("MIN [1; 5]", 1, 5, true),
                        p("MAX [−3; −1]", -3, -1, false)),
                f("F2", "x + 36x² − 2x³ − x⁴", false, -5, 5,
                        p("MAX [0; 8]", 0, 8, false),
                        p("MAX [−12; 2]", -12, 2, false),
                        p("MAX [−1; 10]", -1, 10, false))));

        double pi = MathFunctions.getPi();
        VARIANTS.add(buildVariant(11,
                f("F1", "1 / cos(x)", true, -pi, pi,
                        p("MIN [−2π; −π]", -2 * pi, -pi, true),
                        p("MIN [π/2; π]", pi / 2, pi, true),
                        p("MIN [−π/2; 0]", -pi / 2, 0, true),
                        p("MIN [0; π/2]", 0, pi / 2, true)),
                f("F2", "(x² + 3)^(3/2) − x", false, -2, 5,
                        p("MAX [−10; 1]", -10, 1, false),
                        p("MAX [−12; 12]", -12, 12, false),
                        p("MAX [−1; 8]", -1, 8, false))));

        VARIANTS.add(buildVariant(12,
                f("F1", "1 / sin(x)", true, -pi, pi,
                        p("MIN [−2π; −π]", -2 * pi, -pi, true),
                        p("MIN [π/2; π]", pi / 2, pi, true),
                        p("MIN [−π/2; 0]", -pi / 2, 0, true),
                        p("MIN [0; π/2]", 0, pi / 2, true)),
                f("F2", "√(x² + 3x) − x", false, -2, 5,
                        p("MAX [−10; 1]", -10, 1, false),
                        p("MAX [−12; 12]", -12, 12, false),
                        p("MAX [−1; 8]", -1, 8, false))));

        VARIANTS.add(buildVariant(13,
                f("F1", "x⁴ − 2x² + 5", true, -6, 6,
                        p("MIN [−12; 0]", -12, 0, true),
                        p("MIN [0; 10]", 0, 10, true)),
                f("F2", "(x+1)(x−1)(x−3) / (x² + 1)", false, -4, 4,
                        p("MAX [−2; 2]", -2, 2, false),
                        p("MIN [0; 6]", 0, 6, true),
                        p("MIN [−3; 6]", -3, 6, true))));

        VARIANTS.add(buildVariant(14,
                f("F1", "(x⁴ − 5x² + 4) / (x³ − 9x)", true, -6, 6,
                        p("MIN [−3; 0]", -3, 0, true),
                        p("MIN [3; 6]", 3, 6, true),
                        p("MAX [0; 3]", 0, 3, false),
                        p("MAX [−6; −3]", -6, -3, false)),
                f("F2", "x³ + 5x − 10", false, -2, 5,
                        p("MAX [−10; 1]", -10, 1, false),
                        p("MAX [−12; 12]", -12, 12, false),
                        p("MAX [−1; 8]", -1, 8, false))));

        VARIANTS.add(buildVariant(15,
                f("F1", "1 / (x³ − 3x)", true, -5, 5,
                        p("MIN [−1.7; 0]", -1.7, 0, true),
                        p("MIN [1.7; 6]", 1.7, 6, true),
                        p("MAX [0; 1.7]", 0, 1.7, false),
                        p("MAX [−6; −1.7]", -6, -1.7, false)),
                f("F2", "√(x + 4) − x²", false, -2, 5,
                        p("MAX [−10; 1]", -10, 1, false),
                        p("MAX [−12; 12]", -12, 12, false),
                        p("MAX [−1; 8]", -1, 8, false))));

        VARIANTS.add(buildVariant(16,
                f("F1", "((x−3)²/(x+3)²) / (x+1)³", true, -1, 7,
                        p("MAX [−8; −1]", -8, -1, false),
                        p("MAX [−7; 5]", -7, 5, false),
                        p("MIN [0; 10]", 0, 10, true)),
                f("F2", "(x³ + 2)² − x", false, -2, 5,
                        p("MAX [−10; 1]", -10, 1, false),
                        p("MAX [−12; 12]", -12, 12, false),
                        p("MAX [−1; 8]", -1, 8, false))));

        VARIANTS.add(buildVariant(17,
                f("F1", "4(x²−2x−8)(x²−9) / (x²−x⁴)", true, -1, 1,
                        p("MIN [−1; 0]", -1, 0, true),
                        p("MIN [−2; 3]", -2, 3, true),
                        p("MIN [0; 1]", 0, 1, true),
                        p("MIN [−7; −3]", -7, -3, true),
                        p("MAX [−6; −1]", -6, -1, false),
                        p("MAX [1; 7]", 1, 7, false)),
                f("F2", "x³ + 2x² − x + 2", false, -2, 5,
                        p("MAX [−10; 1]", -10, 1, false),
                        p("MAX [−11; 10]", -11, 10, false),
                        p("MAX [−1; 4]", -1, 4, false))));

        VARIANTS.add(buildVariant(18,
                f("F1", "(x² − 1) / (x² − 5x + 6)", true, -3, 7,
                        p("MIN [−4; 2]", -4, 2, true),
                        p("MIN [3; 7]", 3, 7, true),
                        p("MAX [2; 3]", 2, 3, false)),
                f("F2", "x⁴ + 2x² − x + 2", false, -2, 5,
                        p("MAX [−10; 1]", -10, 1, false),
                        p("MAX [−9; 9]", -9, 9, false),
                        p("MAX [−1; 6]", -1, 6, false))));
    }

    private static IntervalPreset p(String label, double a, double b, boolean minimize) {
        return new IntervalPreset(label, a, b, minimize);
    }

    private static FunctionDefinitionDto f(String id, String formula, boolean defaultMinimize,
                                           double from, double to, IntervalPreset... presets) {
        FunctionDefinitionDto dto = new FunctionDefinitionDto();
        dto.setId(id);
        dto.setLabel(id);
        dto.setFormula(formula);
        dto.setDefaultMinimize(defaultMinimize);
        dto.setDomainFrom(from);
        dto.setDomainTo(to);
        dto.setPresets(Arrays.asList(presets));
        return dto;
    }

    private static VariantDto buildVariant(int id, FunctionDefinitionDto f1, FunctionDefinitionDto f2) {
        VariantDto variant = new VariantDto();
        variant.setId(id);
        variant.setTitle("Вариант " + id);
        variant.setF1(f1);
        variant.setF2(f2);
        return variant;
    }

    public static List<VariantDto> getVariants() {
        return VARIANTS;
    }

    public static VariantDto getVariant(int id) {
        for (VariantDto variant : VARIANTS) {
            if (variant.getId() == id) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Вариант " + id + " не найден");
    }

    public static ObjectiveFunction getFunction(int variantId, String functionId) {
        String key = variantId + ":" + functionId.toUpperCase();
        ObjectiveFunction function = FUNCTIONS.get(key);
        if (function == null) {
            throw new IllegalArgumentException("Функция " + functionId + " для варианта " + variantId + " не найдена");
        }
        return function;
    }

    public static FunctionDefinitionDto getFunctionDefinition(int variantId, String functionId) {
        VariantDto variant = getVariant(variantId);
        if ("F2".equalsIgnoreCase(functionId)) {
            return variant.getF2();
        }
        return variant.getF1();
    }
}
