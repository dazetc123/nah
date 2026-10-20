package com.example.betong.config;

import com.example.betong.Websocket.GpsHandshakeInterceptor;
import com.example.betong.Websocket.GpsWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Mục 2.2.2 báo cáo - Gửi vị trí GPS theo thời gian thực.
 * Đăng ký endpoint WebSocket thuần (không STOMP) để app tài xế gửi tọa độ
 * theo chu kỳ 5-10 giây; xác thực JWT ngay ở bước handshake (xem
 * GpsHandshakeInterceptor).
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GpsWebSocketHandler gpsWebSocketHandler;
    private final GpsHandshakeInterceptor gpsHandshakeInterceptor;

    public WebSocketConfig(GpsWebSocketHandler gpsWebSocketHandler, GpsHandshakeInterceptor gpsHandshakeInterceptor) {
        this.gpsWebSocketHandler = gpsWebSocketHandler;
        this.gpsHandshakeInterceptor = gpsHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gpsWebSocketHandler, "/ws/vi-tri-gps")
                .addInterceptors(gpsHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
