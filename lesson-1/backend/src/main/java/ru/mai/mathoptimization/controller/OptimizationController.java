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
import ru.mai.mathoptimization.dto.OptimizationRequest;
import ru.mai.mathoptimization.dto.OptimizationResult;
import ru.mai.mathoptimization.dto.PlotDataResponse;
import ru.mai.mathoptimization.dto.VariantDto;
import ru.mai.mathoptimization.service.OptimizationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OptimizationController {

    private final OptimizationService optimizationService;

    public OptimizationController(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("status", "ok");
        body.put("lab", "Задание 1: Методы одномерной оптимизации");
        return body;
    }

    @GetMapping("/variants")
    public List<VariantDto> variants() {
        return optimizationService.getVariants();
    }

    @PostMapping("/optimize")
    public OptimizationResult optimize(@RequestBody OptimizationRequest request) {
        return optimizationService.optimize(request);
    }

    @PostMapping("/optimize/all")
    public List<OptimizationResult> optimizeAll(@RequestBody OptimizationRequest request) {
        return optimizationService.optimizeAllMethods(request);
    }

    @GetMapping("/plot")
    public PlotDataResponse plot(@RequestParam int variantId,
                                 @RequestParam String functionId,
                                 @RequestParam double from,
                                 @RequestParam double to,
                                 @RequestParam(defaultValue = "400") int points) {
        return optimizationService.buildPlotData(variantId, functionId, from, to, points);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<String, String>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
