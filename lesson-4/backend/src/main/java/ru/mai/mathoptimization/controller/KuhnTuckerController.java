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
import ru.mai.mathoptimization.dto.KuhnTuckerResultDto;
import ru.mai.mathoptimization.dto.SolveRequest;
import ru.mai.mathoptimization.dto.VariantDto;
import ru.mai.mathoptimization.service.KuhnTuckerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class KuhnTuckerController {

    private final KuhnTuckerService service;

    public KuhnTuckerController(KuhnTuckerService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("status", "ok");
        body.put("lab", "Задание 4: Условия оптимальности Джона и Куна-Таккера");
        return body;
    }

    @GetMapping("/variants")
    public List<VariantDto> variants() {
        return service.getVariants();
    }

    @PostMapping("/solve")
    public KuhnTuckerResultDto solve(@RequestBody SolveRequest request) {
        return service.solve(request);
    }

    @GetMapping("/contour")
    public ContourDataResponse contour(@RequestParam int variantId,
                                       @RequestParam(defaultValue = "60") int gridSize,
                                       @RequestParam(required = false) Double xMin,
                                       @RequestParam(required = false) Double xMax,
                                       @RequestParam(required = false) Double yMin,
                                       @RequestParam(required = false) Double yMax) {
        return service.buildContour(variantId, gridSize, xMin, xMax, yMin, yMax);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<String, String>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
