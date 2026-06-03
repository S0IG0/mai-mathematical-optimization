package ru.mai.mathoptimization.service;

import org.springframework.stereotype.Service;
import ru.mai.mathoptimization.algorithm.OneDimensionalOptimizer;
import ru.mai.mathoptimization.dto.OptimizationRequest;
import ru.mai.mathoptimization.dto.OptimizationResult;
import ru.mai.mathoptimization.dto.PlotDataResponse;
import ru.mai.mathoptimization.dto.PlotPoint;
import ru.mai.mathoptimization.dto.VariantDto;
import ru.mai.mathoptimization.function.FunctionRegistry;
import ru.mai.mathoptimization.function.ObjectiveFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OptimizationService {

    private static final Map<String, String> METHOD_LABELS = new HashMap<String, String>();

    static {
        METHOD_LABELS.put("DICHOTOMY", "Дихотомический поиск");
        METHOD_LABELS.put("GOLDEN_SECTION", "Метод золотого сечения");
        METHOD_LABELS.put("FIBONACCI", "Метод Фибоначчи");
    }

    public List<VariantDto> getVariants() {
        return FunctionRegistry.getVariants();
    }

    public OptimizationResult optimize(OptimizationRequest request) {
        validateRequest(request);
        ObjectiveFunction function = FunctionRegistry.getFunction(request.getVariantId(), request.getFunctionId());
        String methodLabel = METHOD_LABELS.get(request.getMethod());
        if (methodLabel == null) {
            throw new IllegalArgumentException("Неизвестный метод: " + request.getMethod());
        }
        return OneDimensionalOptimizer.optimize(
                function,
                request.getMethod(),
                methodLabel,
                request.getA(),
                request.getB(),
                request.getEpsilon(),
                request.getL(),
                request.isMinimize()
        );
    }

    public List<OptimizationResult> optimizeAllMethods(OptimizationRequest request) {
        List<OptimizationResult> results = new ArrayList<OptimizationResult>();
        for (String method : METHOD_LABELS.keySet()) {
            OptimizationRequest copy = copyRequest(request);
            copy.setMethod(method);
            results.add(optimize(copy));
        }
        return results;
    }

    public PlotDataResponse buildPlotData(int variantId, String functionId, double from, double to, int points) {
        if (from >= to) {
            throw new IllegalArgumentException("Некорректный диапазон построения графика");
        }
        if (points < 50) {
            points = 50;
        }
        if (points > 2000) {
            points = 2000;
        }

        ObjectiveFunction function = FunctionRegistry.getFunction(variantId, functionId);
        PlotDataResponse response = new PlotDataResponse();
        response.setPlotFrom(from);
        response.setPlotTo(to);

        double step = (to - from) / (points - 1);
        List<Double> rawValues = new ArrayList<Double>();
        for (int i = 0; i < points; i++) {
            double x = from + i * step;
            double raw = function.apply(x);
            if (!Double.isNaN(raw) && !Double.isInfinite(raw)) {
                rawValues.add(Math.abs(raw));
            }
        }

        double yCap = computePlotCap(rawValues);

        for (int i = 0; i < points; i++) {
            double x = from + i * step;
            double raw = function.apply(x);
            Double y = null;
            if (!Double.isNaN(raw) && !Double.isInfinite(raw) && Math.abs(raw) <= yCap) {
                y = raw;
            }
            response.getPoints().add(new PlotPoint(x, y));
        }
        return response;
    }

    private double computePlotCap(List<Double> absValues) {
        if (absValues.isEmpty()) {
            return 1.0e6;
        }
        absValues.sort(null);
        int idx = (int) Math.min(absValues.size() - 1, Math.floor(absValues.size() * 0.92));
        double cap = absValues.get(idx);
        if (cap < 1.0) {
            cap = 1.0;
        }
        return cap * 1.2;
    }

    private void validateRequest(OptimizationRequest request) {
        if (request.getVariantId() < 1 || request.getVariantId() > 18) {
            throw new IllegalArgumentException("Номер варианта должен быть от 1 до 18");
        }
        if (request.getA() >= request.getB()) {
            throw new IllegalArgumentException("Левая граница a должна быть меньше правой b");
        }
        if (request.getEpsilon() <= 0 || request.getL() <= 0) {
            throw new IllegalArgumentException("Параметры ε и l должны быть положительными");
        }
        if (request.getEpsilon() >= request.getB() - request.getA()) {
            throw new IllegalArgumentException(
                    "Константа различимости ε должна быть меньше длины интервала (b − a)");
        }
        if (request.getL() >= request.getB() - request.getA()) {
            throw new IllegalArgumentException(
                    "Длина конечного интервала l должна быть меньше начального интервала (b − a)");
        }
        FunctionRegistry.getFunction(request.getVariantId(), request.getFunctionId());
    }

    private OptimizationRequest copyRequest(OptimizationRequest source) {
        OptimizationRequest copy = new OptimizationRequest();
        copy.setVariantId(source.getVariantId());
        copy.setFunctionId(source.getFunctionId());
        copy.setA(source.getA());
        copy.setB(source.getB());
        copy.setEpsilon(source.getEpsilon());
        copy.setL(source.getL());
        copy.setMinimize(source.isMinimize());
        return copy;
    }
}
