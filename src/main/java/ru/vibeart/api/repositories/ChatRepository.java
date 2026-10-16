package ru.vibeart.api.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vibeart.api.models.entities.Chat;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью {@link Chat}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя стандартные CRUD-операции
 * (создание, чтение, обновление, удаление) и добавляет методы для поиска чатов.
 * </p>
 *
 * <h2>Основные возможности</h2>
 * <ul>
 *   <li>{@link #findByMembers_User_UuidAndActiveTrue(UUID, Pageable)} — поиск активных чатов пользователя;</li>
 *   <li>{@link #findChatBetweenUsers(UUID, UUID)} — поиск личного чата между двумя собеседниками;</li>
 *   <li>{@link #findByUuidAndActiveTrue(UUID)} — поиск активного чата по UUID.</li>
 * </ul>
 */
public interface ChatRepository extends JpaRepository<Chat, Long> {
    /**
     * Ищет активные чаты, в которых пользователь состоит участником.
     *
     * @param userUuid UUID пользователя
     * @param pageable параметры пагинации
     * @return страница с найденными чатами
     */
    Page<Chat> findByMembers_User_UuidAndActiveTrue(UUID userUuid, Pageable pageable);

    /**
     * Ищет активный личный чат между двумя собеседниками.
     *
     * @param firstUserUuid UUID первого собеседника
     * @param secondUserUuid UUID второго собеседника
     * @return {@link Optional}, содержащий найденный чат, если он существует
     */
    @Query("""
            SELECT c FROM Chat c
            WHERE c.direct = true
              AND c.active = true
              AND EXISTS (SELECT 1 FROM c.members m1 WHERE m1.user.uuid = :firstUserUuid)
              AND EXISTS (SELECT 1 FROM c.members m2 WHERE m2.user.uuid = :secondUserUuid)
              AND SIZE(c.members) = 2
            """)
    Optional<Chat> findChatBetweenUsers(
            @Param("firstUserUuid") UUID firstUserUuid,
            @Param("secondUserUuid") UUID secondUserUuid
    );

    /**
     * Ищет активный чат по UUID.
     *
     * @param id UUID чата
     * @return {@link Optional}, содержащий найденный чат, если он существует
     */
    Optional<Chat> findByUuidAndActiveTrue(UUID id);
}
