package ru.vibeart.api.services;

import ru.vibeart.api.dtos.user.UserDetailResponse;

/**
 * Сервис для работы с данными пользователя.
 */
public interface UserService {
    /**
     * Возвращает данные текущего аутентифицированного пользователя.
     * @return объект с данными пользователя
     */
    UserDetailResponse getPrincipalUser();
}
