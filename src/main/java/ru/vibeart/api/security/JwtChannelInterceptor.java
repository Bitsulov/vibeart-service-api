package ru.vibeart.api.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.vibeart.api.services.impl.CustomUserDetailsService;

/**
 * Перехватчик входящих STOMP-сообщений, аутентифицирующий WebSocket-сессию по JWT.
 * <p>
 * Браузер не может передать заголовок {@code Authorization} при WebSocket-handshake,
 * поэтому токен передаётся в заголовках STOMP-фрейма {@code CONNECT}.
 * Отвечает за:
 * <ul>
 *     <li>Извлечение JWT-токена из заголовка {@code Authorization} фрейма {@code CONNECT};</li>
 *     <li>Проверку его валидности через {@link JwtTokenProvider};</li>
 *     <li>Загрузку данных пользователя через {@link CustomUserDetailsService};</li>
 *     <li>Привязку аутентификации к WebSocket-сессии, которая затем используется
 *     для всех последующих фреймов этой сессии.</li>
 * </ul>
 * Если токен отсутствует или невалиден, сессия остаётся неаутентифицированной,
 * и {@link ru.vibeart.api.configs.WebSocketSecurityConfig} отклоняет её сообщения.
 *
 * <h2>Пример фрейма</h2>
 * <pre>
 * CONNECT
 * Authorization:Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 * </pre>
 */
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param tokenProvider провайдер JWT-токенов (валидация и извлечение username)
     * @param customUserDetailsService сервис для загрузки пользователя по username
     */
    public JwtChannelInterceptor(JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Аутентифицирует сессию при получении фрейма {@code CONNECT}.
     * Остальные фреймы пропускаются без изменений.
     *
     * @param message входящее STOMP-сообщение
     * @param channel канал входящих сообщений
     * @return исходное сообщение
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String token = getJwtFromHeaders(accessor);
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)
                && "access".equals(tokenProvider.getTokenType(token))) {
            String username = tokenProvider.getUsernameFromJWT(token);
            var userDetails = customUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            accessor.setUser(authentication);
        }

        return message;
    }

    /**
     * Извлекает JWT-токен из заголовка {@code Authorization} STOMP-фрейма.
     *
     * @param accessor заголовки STOMP-фрейма
     * @return строка токена без префикса "Bearer ", либо {@code null}, если токен отсутствует
     */
    private String getJwtFromHeaders(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
