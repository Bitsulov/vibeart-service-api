package ru.vibeart.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vibeart.api.models.entities.Role;
import ru.vibeart.api.models.enums.RoleEnum;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Role}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя базовые CRUD-операции
 * и добавляет метод для поиска роли по её имени ({@link RoleEnum}).
 * </p>
 *
 * <h2>Назначение</h2>
 * <p>
 * Используется при аутентификации и регистрации пользователей для назначения им соответствующих ролей
 * (например, USER, ADMIN).
 * </p>
 *
 * <h2>Основные методы</h2>
 * <ul>
 *   <li>{@link #findByName(RoleEnum)} — поиск роли по её системному имени.</li>
 * </ul>
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Ищет роль по её названию.
     *
     * @param name имя роли (например, {@code RoleEnum.USER})
     * @return {@link Optional}, содержащий найденную роль, если она существует
     */
    Optional<Role> findByName(RoleEnum name);
}
