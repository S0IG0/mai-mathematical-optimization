package ru.mai.mathoptimization.function;

import ru.mai.mathoptimization.dto.ConstraintDto;
import ru.mai.mathoptimization.dto.VariantDto;
import ru.mai.mathoptimization.problem.Constraint;
import ru.mai.mathoptimization.problem.ConstraintType;
import ru.mai.mathoptimization.problem.ProblemDefinition;
import ru.mai.mathoptimization.problem.ProblemFunctions;

import java.util.ArrayList;
import java.util.List;

public final class VariantRegistry {

    private static final List<VariantDto> VARIANTS = new ArrayList<VariantDto>();

    static {
        for (int i = 1; i <= 18; i++) {
            VARIANTS.add(toDto(ProblemFunctions.build(i)));
        }
    }

    private VariantRegistry() {
    }

    private static VariantDto toDto(ProblemDefinition p) {
        VariantDto v = new VariantDto();
        v.setId(p.getId());
        v.setTitle(p.getTitle());
        v.setObjectiveFormula(p.getObjectiveFormula());
        v.setMinimize(p.isMinimize());
        v.setVariant1Extended(p.isVariant1Extended());
        v.setTaskDescription(p.getTaskDescription());
        v.setPlotXMin(p.getPlotXMin());
        v.setPlotXMax(p.getPlotXMax());
        v.setPlotYMin(p.getPlotYMin());
        v.setPlotYMax(p.getPlotYMax());
        for (Constraint c : p.getConstraints()) {
            ConstraintDto cd = new ConstraintDto();
            cd.setId(c.getId());
            cd.setLabel(c.getLabel());
            cd.setType(c.getType() == ConstraintType.EQUALITY ? "EQUALITY" : "INEQUALITY");
            v.getConstraints().add(cd);
        }
        return v;
    }

    public static List<VariantDto> getVariants() {
        return VARIANTS;
    }

    public static ProblemDefinition getProblem(int id) {
        return ProblemFunctions.build(id);
    }
}
