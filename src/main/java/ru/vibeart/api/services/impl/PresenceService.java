package ru.vibeart.api.services.impl;

import org.springframework.stereotype.Service;
import ru.vibeart.api.models.enums.OnlineStatus;
import ru.vibeart.api.services.UserService;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Учитывает присутствие пользователей в сети по количеству открытых
 * WebSocket-сессий и обновляет их онлайн-статус.
 */
@Service
public class PresenceService {
    private final UserService userService;
    private final ConcurrentHashMap<UUID, Integer> connections = new ConcurrentHashMap<>();

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param userService сервис данных пользователя
     */
    public PresenceService(UserService userService) {
        this.userService = userService;
    }

    /**
     * <h1>Подключение сессии пользователя</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Увеличивает счётчик открытых сессий пользователя и, если это
     *     первая сессия, помечает пользователя онлайн.
     * </p>
     *
     * @param userId UUID пользователя
     */
    public void userConnected(UUID userId) {
        connections.merge(userId, 1, Integer::sum);
        userService.setOnlineStatus(userId, OnlineStatus.ONLINE);
    }

    /**
     * <h1>Отключение сессии пользователя</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Уменьшает счётчик открытых сессий пользователя и, если сессий
     *     не осталось, помечает пользователя офлайн.
     * </p>
     *
     * @param userId UUID пользователя
     */
    public void userDisconnected(UUID userId) {
        connections.computeIfPresent(userId, (k, v) -> v <= 1 ? null : v - 1);
        if (!connections.containsKey(userId)) {
            userService.setOnlineStatus(userId, OnlineStatus.OFFLINE);
        }
    }
}
