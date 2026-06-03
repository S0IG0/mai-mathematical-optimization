package ru.mai.mathoptimization.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mai.mathoptimization.dto.ContourDataResponse;
import ru.mai.mathoptimization.dto.SolveRequest;
import ru.mai.mathoptimization.dto.SolveResultDto;
import ru.mai.mathoptimization.dto.VariantDto;
import ru.mai.mathoptimization.service.PenaltyBarrierService;

import java.util.List;

@RestController
@RequestMapping("/api/penalty-barrier")
public class PenaltyBarrierController {

    private final PenaltyBarrierService service;

    public PenaltyBarrierController(PenaltyBarrierService service) {
        this.service = service;
    }

    @GetMapping("/variants")
    public List<VariantDto> variants() {
        return service.getVariants();
    }

    @PostMapping("/solve")
    public SolveResultDto solve(@RequestBody SolveRequest request) {
        return service.solve(request);
    }

    @GetMapping("/contour/{variantId}")
    public ContourDataResponse contour(
            @PathVariable int variantId,
            @RequestParam(defaultValue = "70") int gridSize,
            @RequestParam(required = false) Double xMin,
            @RequestParam(required = false) Double xMax,
            @RequestParam(required = false) Double yMin,
            @RequestParam(required = false) Double yMax) {
        return service.buildContour(variantId, gridSize, xMin, xMax, yMin, yMax);
    }
}
