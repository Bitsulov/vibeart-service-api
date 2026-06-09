package ru.vibeart.api.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.vibeart.api.exceptions.ResourceNotFoundException;

/**
 * Утилитарный компонент для получения данных об аутентифицированном пользователе.
 * <p>
 * Используется для извлечения email (имени пользователя) из текущего контекста безопасности Spring.
 * Помогает избежать дублирования логики при работе с {@link SecurityContextHolder}.
 * </p>
 *
 * <h2>Назначение</h2>
 * <ul>
 *   <li>Позволяет получить email текущего пользователя;</li>
 *   <li>Бросает исключение, если пользователь не аутентифицирован;</li>
 *   <li>Используется в сервисах и контроллерах для проверки прав и фильтрации данных.</li>
 * </ul>
 */
@Component
public class AuthUtil {

    /**
     * Возвращает email текущего аутентифицированного пользователя.
     *
     * @return email пользователя из контекста безопасности
     * @throws ResourceNotFoundException если пользователь не аутентифицирован или анонимный
     */
    public String getPrincipalEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResourceNotFoundException("User isn't authenticated");
        }
        return auth.getName();
    }
}
