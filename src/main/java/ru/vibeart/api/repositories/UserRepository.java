package ru.vibeart.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vibeart.api.models.entities.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя стандартные CRUD-операции
 * (создание, чтение, обновление, удаление) и добавляет методы для поиска по уникальным полям.
 * </p>
 *
 * <h2>Назначение</h2>
 * <p>
 * Отвечает за доступ к данным пользователей в базе данных.
 * Используется сервисами аутентификации, регистрации и управления пользователями.
 * </p>
 *
 * <h2>Основные возможности</h2>
 * <ul>
 *   <li>{@link #findByEmail(String)} — поиск пользователя по email;</li>
 *   <li>{@link #findByUsername(String)} — поиск пользователя по имени пользователя;</li>
 *   <li>{@link #findByUUID(String)} — поиск пользователя по UUID;</li>
 *   <li>{@link #existsByEmail(String)} — проверка наличия пользователя с указанным email.</li>
 * </ul>
 *
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Ищет пользователя по адресу электронной почты.
     *
     * @param email адрес электронной почты
     * @return {@link Optional}, содержащий найденного пользователя, если он существует
     */
    Optional<User> findByEmail(String email);

    /**
     * Ищет пользователя по имени пользователя (username).
     *
     * @param username имя пользователя
     * @return {@link Optional}, содержащий найденного пользователя, если он существует
     */
    Optional<User> findByUsername(String username);

    /**
     * Ищет пользователя по уникальному идентификатору (UUID).
     *
     * @param uuid уникальный идентификатор
     * @return {@link Optional}, содержащий найденного пользователя, если он существует
     */
    Optional<User> findByUuid(UUID uuid);

    /**
     * Проверяет, существует ли пользователь с указанным адресом электронной почты.
     *
     * @param email адрес электронной почты
     * @return {@code true}, если пользователь существует, иначе {@code false}
     */
    boolean existsByEmail(String email);
}
