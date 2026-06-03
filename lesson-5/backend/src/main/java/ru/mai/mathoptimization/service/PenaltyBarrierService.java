package ru.mai.mathoptimization.service;

import org.springframework.stereotype.Service;
import ru.mai.mathoptimization.algorithm.ConstraintCurveBuilder;
import ru.mai.mathoptimization.algorithm.PenaltyBarrierSolver;
import ru.mai.mathoptimization.dto.ContourDataResponse;
import ru.mai.mathoptimization.dto.ExperimentPresetDto;
import ru.mai.mathoptimization.dto.SolveRequest;
import ru.mai.mathoptimization.dto.SolveResultDto;
import ru.mai.mathoptimization.dto.VariantDto;
import ru.mai.mathoptimization.function.VariantRegistry;
import ru.mai.mathoptimization.problem.ProblemDefinition;

import java.util.ArrayList;
import java.util.List;

@Service
public class PenaltyBarrierService {

    public List<VariantDto> getVariants() {
        return VariantRegistry.getVariants();
    }

    public SolveResultDto solve(SolveRequest request) {
        if (request.getVariantId() < 1 || request.getVariantId() > 18) {
            throw new IllegalArgumentException("Номер варианта должен быть от 1 до 18");
        }
        ProblemDefinition problem = VariantRegistry.getProblem(request.getVariantId());
        VariantDto variant = VariantRegistry.getVariant(request.getVariantId());

        double[] x0 = toArray(request.getX0(), variant);
        double[] mus = resolveMu(request, variant);
        String schedule = request.getSchedule() != null ? request.getSchedule() : "INCREASING";
        String domainMode = request.getDomainMode() != null ? request.getDomainMode() : "ALL_CONSTRAINTS";

        return PenaltyBarrierSolver.solve(problem, x0, mus, schedule, domainMode);
    }

    public ContourDataResponse buildContour(int variantId, int gridSize,
                                            Double xMinReq, Double xMaxReq,
                                            Double yMinReq, Double yMaxReq) {
        ProblemDefinition problem = VariantRegistry.getProblem(variantId);
        double xMin = xMinReq != null ? xMinReq : problem.getPlotXMin();
        double xMax = xMaxReq != null ? xMaxReq : problem.getPlotXMax();
        double yMin = yMinReq != null ? yMinReq : problem.getPlotYMin();
        double yMax = yMaxReq != null ? yMaxReq : problem.getPlotYMax();

        gridSize = Math.max(40, Math.min(100, gridSize <= 0 ? 70 : gridSize));

        ContourDataResponse response = new ContourDataResponse();
        response.setXMin(xMin);
        response.setXMax(xMax);
        response.setYMin(yMin);
        response.setYMax(yMax);
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
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double v = problem.evalF(response.getXCoords().get(i), response.getYCoords().get(j));
                if (!Double.isFinite(v) || v > 1e8) {
                    response.getValues().add(null);
                } else {
                    finite.add(v);
                    response.getValues().add(v);
                }
            }
        }

        finite.sort(null);
        double zMin = 0;
        double zMax = 1;
        if (!finite.isEmpty()) {
            int lo = (int) Math.floor(finite.size() * 0.03);
            int hi = Math.min(finite.size() - 1, (int) Math.ceil(finite.size() * 0.97));
            zMin = finite.get(lo);
            zMax = finite.get(hi);
            if (zMax - zMin < 1e-9) {
                zMax = zMin + 1;
            }
        }
        response.setZMin(zMin);
        response.setZMax(zMax);
        for (int i = 0; i <= 12; i++) {
            response.getLevels().add(zMin + (zMax - zMin) * i / 12);
        }

        for (int i = 0; i < response.getValues().size(); i++) {
            Double v = response.getValues().get(i);
            if (v != null && Double.isFinite(v)) {
                response.getValues().set(i, Math.max(zMin, Math.min(zMax, v)));
            }
        }

        response.getConstraints().addAll(ConstraintCurveBuilder.build(problem, 80, xMin, xMax, yMin, yMax));
        return response;
    }

    private static double[] resolveMu(SolveRequest request, VariantDto variant) {
        if (request.getMuValues() != null && !request.getMuValues().isEmpty()) {
            double[] mus = new double[request.getMuValues().size()];
            for (int i = 0; i < mus.length; i++) {
                mus[i] = request.getMuValues().get(i);
            }
            return mus;
        }
        if (!variant.getExperiments().isEmpty()) {
            int idx = Math.max(0, Math.min(request.getExperimentIndex(), variant.getExperiments().size() - 1));
            return variant.getExperiments().get(idx).getMuValues();
        }
        return new double[]{1.0, 10.0, 100.0};
    }

    private static double[] toArray(List<Double> coords, VariantDto variant) {
        if (coords != null && coords.size() >= 2) {
            return new double[]{coords.get(0), coords.get(1)};
        }
        if (!variant.getInitialPoints().isEmpty()) {
            List<Double> c = variant.getInitialPoints().get(0).getCoordinates();
            return new double[]{c.get(0), c.get(1)};
        }
        return new double[]{0, 0};
    }
}
