package ru.mai.mathoptimization.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mai.mathoptimization.dto.ContourDataResponse;
import ru.mai.mathoptimization.dto.SurfaceDataResponse;
import ru.mai.mathoptimization.dto.OptimizationRequest;
import ru.mai.mathoptimization.dto.OptimizationResultDto;
import ru.mai.mathoptimization.dto.VariantDto;
import ru.mai.mathoptimization.service.MultidimOptimizationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MultidimOptimizationController {

    private final MultidimOptimizationService service;

    public MultidimOptimizationController(MultidimOptimizationService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("status", "ok");
        body.put("lab", "Задание 2: Методы многомерной оптимизации");
        return body;
    }

    @GetMapping("/variants")
    public List<VariantDto> variants() {
        return service.getVariants();
    }

    @PostMapping("/optimize")
    public OptimizationResultDto optimize(@RequestBody OptimizationRequest request) {
        return service.optimize(request);
    }

    @PostMapping("/optimize/compare-gauss")
    public List<OptimizationResultDto> compareGauss(@RequestBody OptimizationRequest request) {
        return service.compareGaussModes(request);
    }

    @GetMapping("/contour")
    public ContourDataResponse contour(@RequestParam int variantId,
                                       @RequestParam String functionId,
                                       @RequestParam(defaultValue = "60") int gridSize) {
        return service.buildContour(variantId, functionId, gridSize);
    }

    @GetMapping("/surface")
    public SurfaceDataResponse surface(@RequestParam int variantId,
                                       @RequestParam String functionId,
                                       @RequestParam double x3Slice,
                                       @RequestParam(defaultValue = "50") int gridSize) {
        return service.buildSurface(variantId, functionId, x3Slice, gridSize);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<String, String>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
