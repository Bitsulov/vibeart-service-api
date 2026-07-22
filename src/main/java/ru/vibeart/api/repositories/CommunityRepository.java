package ru.vibeart.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vibeart.api.models.entities.Community;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью {@link Community}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя стандартные CRUD-операции
 * (создание, чтение, обновление, удаление) и добавляет метод для поиска по UUID.
 * </p>
 *
 * <h2>Назначение</h2>
 * <p>
 * Отвечает за доступ к данным сообществ в базе данных.
 * </p>
 *
 * <h2>Основные возможности</h2>
 * <ul>
 *   <li>{@link #findByUuid(UUID)} — поиск сообщества по UUID.</li>
 * </ul>
 *
 */
public interface CommunityRepository extends JpaRepository<Community, Long> {
    /**
     * Ищет сообщество по уникальному идентификатору (UUID).
     *
     * @param uuid уникальный идентификатор
     * @return {@link Optional}, содержащий найденное сообщество, если оно существует
     */
    Optional<Community> findByUuid(UUID uuid);
}