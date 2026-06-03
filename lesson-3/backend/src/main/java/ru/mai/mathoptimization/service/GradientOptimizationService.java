package ru.mai.mathoptimization.service;

import org.springframework.stereotype.Service;
import ru.mai.mathoptimization.algorithm.GradientOptimizer;
import ru.mai.mathoptimization.algorithm.OptimizationProgressListener;
import ru.mai.mathoptimization.dto.ContourDataResponse;
import ru.mai.mathoptimization.dto.FunctionDefinitionDto;
import ru.mai.mathoptimization.dto.OptimizationRequest;
import ru.mai.mathoptimization.dto.OptimizationResultDto;
import ru.mai.mathoptimization.dto.SurfaceDataResponse;
import ru.mai.mathoptimization.dto.VariantDto;
import ru.mai.mathoptimization.function.MultidimFunction;
import ru.mai.mathoptimization.function.VariantRegistry;

import java.util.ArrayList;
import java.util.List;

@Service
public class GradientOptimizationService {

    public List<VariantDto> getVariants() {
        return VariantRegistry.getVariants();
    }

    public OptimizationResultDto optimize(OptimizationRequest request) {
        return optimizeWithProgress(request, null);
    }

    public OptimizationResultDto optimizeWithProgress(OptimizationRequest request,
                                                      OptimizationProgressListener listener) {
        validateRequest(request);
        FunctionDefinitionDto def = VariantRegistry.getFunctionDefinition(
                request.getVariantId(), request.getFunctionId());
        MultidimFunction function = VariantRegistry.getFunction(request.getVariantId(), request.getFunctionId());
        double[] x0 = toArray(request.getX0(), function.dimension());

        boolean minimize = def.isMinimize();

        return GradientOptimizer.optimize(
                function,
                def.getMethod(),
                def.getMethodLabel(),
                x0,
                request.getEpsilon(),
                request.getDelta(),
                minimize,
                listener
        );
    }

    public ContourDataResponse buildContour(int variantId, String functionId, int gridSize) {
        FunctionDefinitionDto def = VariantRegistry.getFunctionDefinition(variantId, functionId);
        if (!def.isPlottable2d()) {
            throw new IllegalArgumentException("Линии уровня доступны только для двумерных функций");
        }
        if (gridSize < 30) {
            gridSize = 30;
        }
        if (gridSize > 120) {
            gridSize = 120;
        }

        MultidimFunction function = VariantRegistry.getFunction(variantId, functionId);
        ContourDataResponse response = new ContourDataResponse();
        response.setXMin(def.getPlotXMin());
        response.setXMax(def.getPlotXMax());
        response.setYMin(def.getPlotYMin());
        response.setYMax(def.getPlotYMax());
        response.setGridSize(gridSize);

        double dx = (def.getPlotXMax() - def.getPlotXMin()) / (gridSize - 1);
        double dy = (def.getPlotYMax() - def.getPlotYMin()) / (gridSize - 1);

        for (int i = 0; i < gridSize; i++) {
            response.getXCoords().add(def.getPlotXMin() + i * dx);
        }
        for (int j = 0; j < gridSize; j++) {
            response.getYCoords().add(def.getPlotYMin() + j * dy);
        }

        List<Double> raw = new ArrayList<Double>();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double x1 = response.getXCoords().get(i);
                double x2 = response.getYCoords().get(j);
                double v = function.apply(new double[]{x1, x2});
                raw.add(Double.isFinite(v) ? v : Double.NaN);
            }
        }

        List<Double> finite = new ArrayList<Double>();
        for (Double v : raw) {
            if (v != null && Double.isFinite(v)) {
                finite.add(v);
            }
        }
        finite.sort(null);

        double zMin = 0;
        double zMax = 1;
        if (!finite.isEmpty()) {
            int loIdx = (int) Math.floor(finite.size() * 0.02);
            int hiIdx = Math.min(finite.size() - 1, (int) Math.ceil(finite.size() * 0.98));
            zMin = finite.get(loIdx);
            zMax = finite.get(hiIdx);
            if (zMax - zMin < 1e-9) {
                zMax = zMin + 1;
            }
        }

        for (Double v : raw) {
            response.getValues().add(v != null && Double.isFinite(v) ? v : null);
        }

        response.setZMin(zMin);
        response.setZMax(zMax);
        int levelCount = 12;
        for (int i = 0; i <= levelCount; i++) {
            response.getLevels().add(zMin + (zMax - zMin) * i / levelCount);
        }
        return response;
    }

    public SurfaceDataResponse buildSurface(int variantId, String functionId, double x3Slice, int gridSize) {
        FunctionDefinitionDto def = VariantRegistry.getFunctionDefinition(variantId, functionId);
        if (def.getDimension() != 3) {
            throw new IllegalArgumentException("3D-поверхность доступна только для функций с тремя переменными");
        }
        if (gridSize < 25) {
            gridSize = 25;
        }
        if (gridSize > 80) {
            gridSize = 80;
        }

        double xMin = def.getPlotXMax() > def.getPlotXMin() ? def.getPlotXMin() : -4;
        double xMax = def.getPlotXMax() > def.getPlotXMin() ? def.getPlotXMax() : 4;
        double yMin = def.getPlotYMax() > def.getPlotYMin() ? def.getPlotYMin() : -4;
        double yMax = def.getPlotYMax() > def.getPlotYMin() ? def.getPlotYMax() : 4;

        MultidimFunction function = VariantRegistry.getFunction(variantId, functionId);
        SurfaceDataResponse response = new SurfaceDataResponse();
        response.setXMin(xMin);
        response.setXMax(xMax);
        response.setYMin(yMin);
        response.setYMax(yMax);
        response.setX3Slice(x3Slice);
        response.setGridSize(gridSize);

        double dx = (xMax - xMin) / (gridSize - 1);
        double dy = (yMax - yMin) / (gridSize - 1);

        for (int i = 0; i < gridSize; i++) {
            response.getXCoords().add(xMin + i * dx);
        }
        for (int j = 0; j < gridSize; j++) {
            response.getYCoords().add(yMin + j * dy);
        }

        List<Double> finite = new ArrayList<Double>();
        for (int j = 0; j < gridSize; j++) {
            for (int i = 0; i < gridSize; i++) {
                double v = function.apply(new double[]{
                        response.getXCoords().get(i),
                        response.getYCoords().get(j),
                        x3Slice
                });
                if (!Double.isFinite(v)) {
                    v = Double.NaN;
                } else {
                    finite.add(v);
                }
                response.getValues().add(Double.isFinite(v) ? v : null);
            }
        }

        finite.sort(null);
        double zMin = 0;
        double zMax = 1;
        if (!finite.isEmpty()) {
            int loIdx = (int) Math.floor(finite.size() * 0.02);
            int hiIdx = Math.min(finite.size() - 1, (int) Math.ceil(finite.size() * 0.98));
            zMin = finite.get(loIdx);
            zMax = finite.get(hiIdx);
            if (zMax - zMin < 1e-9) {
                zMax = zMin + 1;
            }
        }
        response.setZMin(zMin);
        response.setZMax(zMax);
        return response;
    }

    private void validateRequest(OptimizationRequest request) {
        if (request.getVariantId() < 1 || request.getVariantId() > 18) {
            throw new IllegalArgumentException("Номер варианта должен быть от 1 до 18");
        }
        if (request.getEpsilon() <= 0) {
            throw new IllegalArgumentException("Точность ε должна быть положительной");
        }
        if (request.getDelta() <= 0) {
            throw new IllegalArgumentException("Шаг Δ должен быть положительным");
        }
        MultidimFunction function = VariantRegistry.getFunction(request.getVariantId(), request.getFunctionId());
        if (request.getX0() == null || request.getX0().size() != function.dimension()) {
            throw new IllegalArgumentException("Начальная точка X₀ должна иметь размерность " + function.dimension());
        }
    }

    private double[] toArray(List<Double> list, int dim) {
        if (list.size() != dim) {
            throw new IllegalArgumentException("Ожидается размерность " + dim);
        }
        double[] arr = new double[dim];
        for (int i = 0; i < dim; i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}
