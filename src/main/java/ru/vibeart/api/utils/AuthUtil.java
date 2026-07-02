package ru.vibeart.api.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.vibeart.api.exceptions.ResourceNotFoundException;
import ru.vibeart.api.exceptions.UnauthorizedException;
import ru.vibeart.api.security.UserPrincipal;

import java.util.UUID;

/**
 * Утилитарный компонент для получения данных об аутентифицированном пользователе.
 * <p>
 * Используется для извлечения UUID из текущего контекста безопасности Spring.
 * Помогает избежать дублирования логики при работе с {@link SecurityContextHolder}.
 * </p>
 *
 * <h2>Назначение</h2>
 * <ul>
 *   <li>Позволяет получить UUID текущего пользователя;</li>
 *   <li>Бросает исключение, если пользователь не аутентифицирован;</li>
 *   <li>Используется в сервисах и контроллерах для проверки прав и фильтрации данных.</li>
 * </ul>
 */
@Component
public class AuthUtil {

    /**
     * Возвращает UUID текущего аутентифицированного пользователя.
     *
     * @return UUID пользователя из контекста безопасности
     * @throws UnauthorizedException если пользователь не аутентифицирован или анонимный
     */
    public UUID getPrincipalUuid() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("User isn't authenticated");
        }

        if (auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUuid();
        }

        throw new UnauthorizedException("Invalid assignment of principal");
    }

    /**
     * Возвращает email текущего аутентифицированного пользователя.
     *
     * @return email пользователя из контекста безопасности
     * @throws UnauthorizedException если пользователь не аутентифицирован или анонимный
     */
    public String getPrincipalEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("User isn't authenticated");
        }
        return auth.getName();
    }
}
