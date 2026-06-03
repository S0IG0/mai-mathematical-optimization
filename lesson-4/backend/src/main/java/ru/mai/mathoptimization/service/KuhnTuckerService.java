package ru.mai.mathoptimization.service;

import org.springframework.stereotype.Service;
import ru.mai.mathoptimization.algorithm.FeasibleRegionBuilder;
import ru.mai.mathoptimization.algorithm.KuhnTuckerSolver;
import ru.mai.mathoptimization.dto.ConstraintBoundaryDto;
import ru.mai.mathoptimization.dto.ContourDataResponse;
import ru.mai.mathoptimization.dto.KuhnTuckerResultDto;
import ru.mai.mathoptimization.dto.SolveRequest;
import ru.mai.mathoptimization.dto.VariantDto;
import ru.mai.mathoptimization.function.VariantRegistry;
import ru.mai.mathoptimization.problem.Constraint;
import ru.mai.mathoptimization.problem.ConstraintType;
import ru.mai.mathoptimization.problem.LinearConstraint;
import ru.mai.mathoptimization.problem.ProblemDefinition;

import java.util.ArrayList;
import java.util.List;

@Service
public class KuhnTuckerService {

    public List<VariantDto> getVariants() {
        return VariantRegistry.getVariants();
    }

    public KuhnTuckerResultDto solve(SolveRequest request) {
        if (request.getVariantId() < 1 || request.getVariantId() > 18) {
            throw new IllegalArgumentException("Номер варианта должен быть от 1 до 18");
        }
        ProblemDefinition problem = VariantRegistry.getProblem(request.getVariantId());
        KuhnTuckerResultDto result = KuhnTuckerSolver.solve(problem);
        result.getFeasiblePolygon().addAll(FeasibleRegionBuilder.buildPolygon(problem));

        if (problem.isVariant1Extended() && request.isIncludeUnconstrainedPart()) {
            KuhnTuckerResultDto unc = KuhnTuckerSolver.solveUnconstrained(
                    problem, request.getUnconstrainedX0(), request.getUnconstrainedY0(), 30);
            result.getDescentPath().addAll(unc.getDescentPath());
            String extra = unc.getConclusion();
            result.setConclusion(result.getConclusion() + " " + extra);
        }
        return result;
    }

    public ContourDataResponse buildContour(int variantId, int gridSize,
                                            Double xMinReq, Double xMaxReq, Double yMinReq, Double yMaxReq) {
        if (variantId < 1 || variantId > 18) {
            throw new IllegalArgumentException("Номер варианта должен быть от 1 до 18");
        }
        ProblemDefinition problem = VariantRegistry.getProblem(variantId);
        double[] view = resolveViewBounds(problem, xMinReq, xMaxReq, yMinReq, yMaxReq);
        gridSize = adaptiveGridSize(gridSize, view[0], view[1], view[2], view[3]);
        if (gridSize < 35) {
            gridSize = 35;
        }
        if (gridSize > 110) {
            gridSize = 110;
        }
        double xMin = view[0];
        double xMax = view[1];
        double yMin = view[2];
        double yMax = view[3];

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
                double x1 = response.getXCoords().get(i);
                double x2 = response.getYCoords().get(j);
                double v = problem.evalF(x1, x2);
                if (!Double.isFinite(v)) {
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
        for (int i = 0; i <= 12; i++) {
            response.getLevels().add(zMin + (zMax - zMin) * i / 12);
        }

        for (int i = 0; i < response.getValues().size(); i++) {
            Double v = response.getValues().get(i);
            if (v != null && Double.isFinite(v)) {
                double clamped = Math.max(zMin, Math.min(zMax, v));
                response.getValues().set(i, clamped);
            }
        }

        response.getFeasiblePolygon().addAll(FeasibleRegionBuilder.buildPolygon(problem));
        response.getConstraintBoundaries().addAll(buildConstraintLines(problem, xMin, xMax, yMin, yMax));
        return response;
    }

    private static double[] resolveViewBounds(ProblemDefinition problem,
                                              Double xMinReq, Double xMaxReq,
                                              Double yMinReq, Double yMaxReq) {
        double defXMin = problem.getPlotXMin();
        double defXMax = problem.getPlotXMax();
        double defYMin = problem.getPlotYMin();
        double defYMax = problem.getPlotYMax();

        if (xMinReq == null || xMaxReq == null || yMinReq == null || yMaxReq == null) {
            return new double[]{defXMin, defXMax, defYMin, defYMax};
        }

        double xMin = Math.min(xMinReq, xMaxReq);
        double xMax = Math.max(xMinReq, xMaxReq);
        double yMin = Math.min(yMinReq, yMaxReq);
        double yMax = Math.max(yMinReq, yMaxReq);

        double[] x = normalizeAxis(xMin, xMax, 0.35, 150);
        double[] y = normalizeAxis(yMin, yMax, 0.35, 150);
        return new double[]{x[0], x[1], y[0], y[1]};
    }

    private static double[] normalizeAxis(double lo, double hi, double minSpan, double maxSpan) {
        if (hi - lo < minSpan) {
            double mid = (lo + hi) / 2;
            lo = mid - minSpan / 2;
            hi = mid + minSpan / 2;
        }
        if (hi - lo > maxSpan) {
            double mid = (lo + hi) / 2;
            lo = mid - maxSpan / 2;
            hi = mid + maxSpan / 2;
        }
        return new double[]{lo, hi};
    }

    private static int adaptiveGridSize(int requested, double xMin, double xMax, double yMin, double yMax) {
        double span = Math.max(xMax - xMin, yMax - yMin);
        int bySpan = (int) Math.round(40 + span * 4);
        return Math.max(requested, bySpan);
    }

    private List<ConstraintBoundaryDto> buildConstraintLines(ProblemDefinition problem,
                                                             double xMin, double xMax,
                                                             double yMin, double yMax) {
        List<ConstraintBoundaryDto> lines = new ArrayList<ConstraintBoundaryDto>();
        int n = 40;

        for (Constraint c : problem.getConstraints()) {
            if (c.getType() != ConstraintType.INEQUALITY) {
                continue;
            }
            ConstraintBoundaryDto b = new ConstraintBoundaryDto();
            b.setId(c.getId());
            b.setLabel(c.getLabel());

            if (c instanceof LinearConstraint) {
                LinearConstraint lc = (LinearConstraint) c;
                if (Math.abs(lc.getB()) > 1e-9) {
                    for (int i = 0; i <= n; i++) {
                        double x1 = xMin + (xMax - xMin) * i / n;
                        double x2 = -(lc.getA() * x1 + lc.getC()) / lc.getB();
                        b.getX().add(x1);
                        b.getY().add(x2);
                    }
                } else if (Math.abs(lc.getA()) > 1e-9) {
                    double x1 = -lc.getC() / lc.getA();
                    for (int j = 0; j <= n; j++) {
                        double x2 = yMin + (yMax - yMin) * j / n;
                        b.getX().add(x1);
                        b.getY().add(x2);
                    }
                }
            } else {
                for (int i = 0; i <= n; i++) {
                    double x1 = xMin + (xMax - xMin) * i / n;
                    double x2 = solveOnBoundary(c, x1, yMin, yMax);
                    if (Double.isFinite(x2)) {
                        b.getX().add(x1);
                        b.getY().add(x2);
                    }
                }
            }
            if (!b.getX().isEmpty()) {
                lines.add(b);
            }
        }
        return lines;
    }

    private double solveOnBoundary(Constraint c, double x1, double yMin, double yMax) {
        double lo = yMin;
        double hi = yMax;
        for (int i = 0; i < 50; i++) {
            double mid = 0.5 * (lo + hi);
            if (c.value(x1, mid) <= 0) {
                hi = mid;
            } else {
                lo = mid;
            }
        }
        return hi;
    }
}
