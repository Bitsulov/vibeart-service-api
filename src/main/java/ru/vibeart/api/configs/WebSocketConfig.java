package ru.vibeart.api.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import ru.vibeart.api.security.JwtChannelInterceptor;

/**
 * Конфигурация WebSocket-соединений по протоколу STOMP.
 * <p>
 * Порядок {@code HIGHEST_PRECEDENCE + 99} нужен, чтобы {@link JwtChannelInterceptor}
 * выполнялся раньше перехватчиков авторизации Spring Security.
 * </p>
 */
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtChannelInterceptor jwtChannelInterceptor;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param jwtChannelInterceptor перехватчик, аутентифицирующий сессию по JWT из фрейма {@code CONNECT}
     */
    public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    /**
     * Регистрирует STOMP-эндпоинт {@code /ws} с поддержкой SockJS-фолбэка.
     *
     * @param registry реестр STOMP-эндпоинтов
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:3001")
                .withSockJS();
    }

    /**
     * Настраивает встроенный брокер сообщений: подписки на {@code /topic/},
     * исходящие сообщения клиента — с префиксом {@code /app}.
     *
     * @param registry реестр брокера сообщений
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic/");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Добавляет {@link JwtChannelInterceptor} в канал входящих сообщений.
     *
     * @param registration настройки канала входящих сообщений
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}