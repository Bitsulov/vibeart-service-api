package ru.vibeart.api.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

/**
 * Настраивает авторизацию STOMP-сообщений WebSocket-соединения.
 * Отвечает за:
 * <ul>
 *     <li>Разрешение служебных фреймов без destination ({@code CONNECT}, {@code DISCONNECT}, {@code HEARTBEAT}) авторизованным пользователям.</li>
 *     <li>Разрешение подписки на личные (/user/**) destination авторизованным пользователям.</li>
 *     <li>Запрет подписки на любые другие destination: каждый новый топик добавляется отдельным правилом.</li>
 *     <li>Разрешение отправки ({@code SEND}) на destination {@code /app/**} авторизованным пользователям.</li>
 *     <li>Запрет всех остальных сообщений, включая прямую отправку в топики брокера.</li>
 * </ul>
 */
@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    /**
     * Собирает правила авторизации для входящих STOMP-сообщений.
     *
     * @param messages объект для описания правил доступа к STOMP-сообщениям по их типу и destination
     * @return готовый {@link AuthorizationManager} для канала STOMP-сообщений
     */
    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                .nullDestMatcher().authenticated()
                .simpSubscribeDestMatchers("/user/**").authenticated()
                .simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE).denyAll()
                .simpDestMatchers("/app/**").authenticated()
                .anyMessage().denyAll();
        return messages.build();
    }

    /**
     * Отключает проверку CSRF-токена во фрейме {@code CONNECT}.
     * <p>
     * Бин с именем {@code csrfChannelInterceptor} подменяет стандартный перехватчик
     * {@link EnableWebSocketSecurity}. Аутентификация выполняется по JWT
     * из заголовков фрейма, а не по cookie, поэтому CSRF-защита не требуется.
     * </p>
     *
     * @return перехватчик, пропускающий сообщения без изменений
     */
    @Bean
    public ChannelInterceptor csrfChannelInterceptor() {
        return new ChannelInterceptor() {};
    }
}
