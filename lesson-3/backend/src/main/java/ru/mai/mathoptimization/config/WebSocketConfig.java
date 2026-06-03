package ru.mai.mathoptimization.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ru.mai.mathoptimization.websocket.OptimizationWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OptimizationWebSocketHandler optimizationWebSocketHandler;

    public WebSocketConfig(OptimizationWebSocketHandler optimizationWebSocketHandler) {
        this.optimizationWebSocketHandler = optimizationWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(optimizationWebSocketHandler, "/ws/optimize")
                .setAllowedOrigins("http://localhost:5173", "http://127.0.0.1:5173");
    }
}
