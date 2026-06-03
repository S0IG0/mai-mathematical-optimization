package ru.mai.mathoptimization.service;

import org.springframework.stereotype.Service;
import ru.mai.mathoptimization.algorithm.MultidimensionalOptimizer;
import ru.mai.mathoptimization.algorithm.OptimizationProgressListener;
import ru.mai.mathoptimization.dto.ContourDataResponse;
import ru.mai.mathoptimization.dto.SurfaceDataResponse;
import ru.mai.mathoptimization.dto.FunctionDefinitionDto;
import ru.mai.mathoptimization.dto.OptimizationRequest;
import ru.mai.mathoptimization.dto.OptimizationResultDto;
import ru.mai.mathoptimization.dto.VariantDto;
import ru.mai.mathoptimization.function.MultidimFunction;
import ru.mai.mathoptimization.function.VariantRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MultidimOptimizationService {

    public List<VariantDto> getVariants() {
        return VariantRegistry.getVariants();
    }

    public OptimizationResultDto optimize(OptimizationRequest request) {
        validateRequest(request);
        VariantDto variant = VariantRegistry.getVariant(request.getVariantId());
        MultidimFunction function = VariantRegistry.getFunction(request.getVariantId(), request.getFunctionId());
        double[] x0 = toArray(request.getX0(), function.dimension());

        boolean continuous = variant.getMethod().contains("CONTINUOUS");
        boolean use1d = request.isUseOneDimensional();
        if (!variant.isSupportsOneDimensional()) {
            use1d = continuous;
        } else if (variant.getMethod().startsWith("GAUSS_SEIDEL_DISCRETE")) {
            use1d = false;
        }

        String label = variant.getMethodLabel();
        if (variant.isSupportsOneDimensional()) {
            label += use1d ? " · одномерная оптимизация" : " · дискретный шаг";
        }

        return MultidimensionalOptimizer.optimize(
                function,
                variant.getMethod(),
                label,
                x0,
                request.getEpsilon(),
                request.getDelta(),
                use1d,
                request.isMinimize()
        );
    }

    public OptimizationResultDto optimizeWithProgress(OptimizationRequest request,
                                                      OptimizationProgressListener listener) {
        validateRequest(request);
        VariantDto variant = VariantRegistry.getVariant(request.getVariantId());
        MultidimFunction function = VariantRegistry.getFunction(request.getVariantId(), request.getFunctionId());
        double[] x0 = toArray(request.getX0(), function.dimension());

        boolean continuous = variant.getMethod().contains("CONTINUOUS");
        boolean use1d = request.isUseOneDimensional();
        if (!variant.isSupportsOneDimensional()) {
            use1d = continuous;
        } else if (variant.getMethod().startsWith("GAUSS_SEIDEL_DISCRETE")) {
            use1d = false;
        }

        String label = variant.getMethodLabel();
        if (variant.isSupportsOneDimensional()) {
            label += use1d ? " · одномерная оптимизация" : " · дискретный шаг";
        }

        return MultidimensionalOptimizer.optimize(
                function,
                variant.getMethod(),
                label,
                x0,
                request.getEpsilon(),
                request.getDelta(),
                use1d,
                request.isMinimize(),
                listener
        );
    }

    public List<OptimizationResultDto> compareGaussModes(OptimizationRequest request) {
        VariantDto variant = VariantRegistry.getVariant(request.getVariantId());
        if (!variant.isSupportsOneDimensional()) {
            return Collections.singletonList(optimize(request));
        }
        List<OptimizationResultDto> results = new ArrayList<OptimizationResultDto>();
        OptimizationRequest with1d = copyRequest(request);
        with1d.setUseOneDimensional(true);
        OptimizationResultDto r1 = optimize(with1d);
        r1.setMethodLabel(r1.getMethodLabel() + " (с 1D-оптимизацией)");
        results.add(r1);

        OptimizationRequest without1d = copyRequest(request);
        without1d.setUseOneDimensional(false);
        OptimizationResultDto r2 = optimize(without1d);
        r2.setMethodLabel(r2.getMethodLabel() + " (без 1D-оптимизации)");
        results.add(r2);
        return results;
    }

    public ContourDataResponse buildContour(int variantId, String functionId, int gridSize) {
        FunctionDefinitionDto def = VariantRegistry.getFunctionDefinition(variantId, functionId);
        if (!def.isPlottable2d()) {
            throw new IllegalArgumentException("Линии уровня доступны только для двумерных функций (F₁ или F₂)");
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
                if (!Double.isFinite(v)) {
                    v = Double.NaN;
                }
                raw.add(v);
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
            if (v != null && Double.isFinite(v)) {
                response.getValues().add(v);
            } else {
                response.getValues().add(null);
            }
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
            throw new IllegalArgumentException("3D-поверхность доступна только для функций F₂ (три переменные)");
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
        if (request.getVariantId() < 1 || request.getVariantId() > 16) {
            throw new IllegalArgumentException("Номер варианта должен быть от 1 до 16");
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

    private OptimizationRequest copyRequest(OptimizationRequest source) {
        OptimizationRequest copy = new OptimizationRequest();
        copy.setVariantId(source.getVariantId());
        copy.setFunctionId(source.getFunctionId());
        copy.setX0(source.getX0());
        copy.setEpsilon(source.getEpsilon());
        copy.setDelta(source.getDelta());
        copy.setUseOneDimensional(source.isUseOneDimensional());
        copy.setMinimize(source.isMinimize());
        return copy;
    }
}
