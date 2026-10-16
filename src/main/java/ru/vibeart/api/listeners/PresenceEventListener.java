package ru.vibeart.api.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import ru.vibeart.api.security.UserPrincipal;
import ru.vibeart.api.services.impl.PresenceService;

import java.security.Principal;

/**
 * Слушатель событий подключения и отключения WebSocket-сессий.
 */
@Component
public class PresenceEventListener {
    private final PresenceService presenceService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param presenceService сервис учёта присутствия пользователей в сети
     */
    public PresenceEventListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    /**
     * Отмечает пользователя подключившимся при установлении WebSocket-сессии.
     *
     * @param event событие подключения сессии
     */
    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        Principal user = event.getUser();
        if (user instanceof Authentication auth && auth.getPrincipal() instanceof UserPrincipal principal) {
            presenceService.userConnected(principal.getUuid());
        }
    }

    /**
     * Отмечает пользователя отключившимся при завершении WebSocket-сессии.
     *
     * @param event событие отключения сессии
     */
    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if (user instanceof Authentication auth && auth.getPrincipal() instanceof UserPrincipal principal) {
            presenceService.userDisconnected(principal.getUuid());
        }
    }
}
