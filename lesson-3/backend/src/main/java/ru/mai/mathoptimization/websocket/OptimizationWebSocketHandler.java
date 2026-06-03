package ru.mai.mathoptimization.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.mai.mathoptimization.dto.ContourDataResponse;
import ru.mai.mathoptimization.dto.SurfaceDataResponse;
import ru.mai.mathoptimization.dto.FunctionDefinitionDto;
import ru.mai.mathoptimization.dto.IterationDto;
import ru.mai.mathoptimization.dto.OptimizationRequest;
import ru.mai.mathoptimization.dto.OptimizationResultDto;
import ru.mai.mathoptimization.dto.PathPointDto;
import ru.mai.mathoptimization.function.VariantRegistry;
import ru.mai.mathoptimization.service.GradientOptimizationService;

import java.util.HashMap;
import java.util.Map;

@Component
public class OptimizationWebSocketHandler extends TextWebSocketHandler {

    private static final int STREAM_DELAY_MS = 60;

    private final GradientOptimizationService optimizationService;
    private final ObjectMapper objectMapper;

    public OptimizationWebSocketHandler(GradientOptimizationService optimizationService,
                                        ObjectMapper objectMapper) {
        this.optimizationService = optimizationService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Thread worker = new Thread(() -> runOptimization(session, message.getPayload()), "ws-optimize");
        worker.setDaemon(true);
        worker.start();
    }

    private void runOptimization(WebSocketSession session, String payload) {
        try {
            OptimizationRequest request = objectMapper.readValue(payload, OptimizationRequest.class);
            FunctionDefinitionDto def = VariantRegistry.getFunctionDefinition(
                    request.getVariantId(), request.getFunctionId());

            if (def.isPlottable2d()) {
                ContourDataResponse contour = optimizationService.buildContour(
                        request.getVariantId(), request.getFunctionId(), 80);
                send(session, "CONTOUR", contour);
            } else if (def.getDimension() == 3 && request.getX0() != null && request.getX0().size() >= 3) {
                double x3 = request.getX0().get(2);
                SurfaceDataResponse surface = optimizationService.buildSurface(
                        request.getVariantId(), request.getFunctionId(), x3, 50);
                send(session, "SURFACE", surface);
            }

            send(session, "STARTED", Map.of("method", def.getMethodLabel()));

            OptimizationResultDto result = optimizationService.optimizeWithProgress(request,
                    new StreamingListener(session));

            send(session, "DONE", result);
        } catch (Exception ex) {
            try {
                send(session, "ERROR", Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Ошибка расчёта"));
            } catch (Exception ignored) {
                // session closed
            }
        }
    }

    private void send(WebSocketSession session, String type, Object data) throws Exception {
        if (!session.isOpen()) {
            return;
        }
        Map<String, Object> envelope = new HashMap<String, Object>();
        envelope.put("type", type);
        envelope.put("data", data);
        synchronized (session) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(envelope)));
        }
    }

    private class StreamingListener implements ru.mai.mathoptimization.algorithm.OptimizationProgressListener {

        private final WebSocketSession session;

        StreamingListener(WebSocketSession session) {
            this.session = session;
        }

        @Override
        public void onPathPoint(PathPointDto point) {
            try {
                send(session, "PATH_POINT", point);
                Thread.sleep(STREAM_DELAY_MS);
            } catch (Exception ignored) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void onIteration(IterationDto iteration) {
            try {
                send(session, "ITERATION", iteration);
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close(CloseStatus.SERVER_ERROR);
    }
}
