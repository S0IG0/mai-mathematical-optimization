package ru.mai.mathoptimization.dto;

import java.util.ArrayList;
import java.util.List;

public class KuhnTuckerResultDto {

    private int variantId;
    private String objectiveFormula;
    private boolean minimize;
    private KuhnTuckerSystemDto kktSystem;
    private final List<CandidatePointDto> candidates = new ArrayList<CandidatePointDto>();
    private PointDto optimalPoint;
    private Double optimalValue;
    private Boolean optimalKktSatisfied;
    private String conclusion;
    private final List<PointDto> descentPath = new ArrayList<PointDto>();
    private final List<double[]> feasiblePolygon = new ArrayList<double[]>();

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public String getObjectiveFormula() {
        return objectiveFormula;
    }

    public void setObjectiveFormula(String objectiveFormula) {
        this.objectiveFormula = objectiveFormula;
    }

    public boolean isMinimize() {
        return minimize;
    }

    public void setMinimize(boolean minimize) {
        this.minimize = minimize;
    }

    public KuhnTuckerSystemDto getKktSystem() {
        return kktSystem;
    }

    public void setKktSystem(KuhnTuckerSystemDto kktSystem) {
        this.kktSystem = kktSystem;
    }

    public List<CandidatePointDto> getCandidates() {
        return candidates;
    }

    public PointDto getOptimalPoint() {
        return optimalPoint;
    }

    public void setOptimalPoint(PointDto optimalPoint) {
        this.optimalPoint = optimalPoint;
    }

    public Double getOptimalValue() {
        return optimalValue;
    }

    public void setOptimalValue(Double optimalValue) {
        this.optimalValue = optimalValue;
    }

    public Boolean getOptimalKktSatisfied() {
        return optimalKktSatisfied;
    }

    public void setOptimalKktSatisfied(Boolean optimalKktSatisfied) {
        this.optimalKktSatisfied = optimalKktSatisfied;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public List<PointDto> getDescentPath() {
        return descentPath;
    }

    public List<double[]> getFeasiblePolygon() {
        return feasiblePolygon;
    }
}
