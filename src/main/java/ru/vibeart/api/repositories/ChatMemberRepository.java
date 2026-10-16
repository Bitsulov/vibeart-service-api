package ru.vibeart.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vibeart.api.models.entities.ChatMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью {@link ChatMember}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя стандартные CRUD-операции
 * (создание, чтение, обновление, удаление) и добавляет методы для поиска
 * участников чата.
 * </p>
 *
 * <h2>Основные возможности</h2>
 * <ul>
 *   <li>{@link #findByChat_UuidAndUser_Uuid(UUID, UUID)} — поиск участника по чату и пользователю;</li>
 *   <li>{@link #findByChat_UuidAndUser_UuidNot(UUID, UUID)} — поиск собеседника личного чата;</li>
 *   <li>{@link #countByChat_Uuid(UUID)} — подсчёт количества участников чата;</li>
 *   <li>{@link #findByChat_Uuid(UUID)} — поиск всех участников чата;</li>
 *   <li>{@link #findFirstByChat_UuidOrderByJoinedAtAsc(UUID)} — поиск участника, вступившего раньше остальных.</li>
 * </ul>
 */
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    /**
     * Ищет участника чата по чату и пользователю.
     *
     * @param chatUuid UUID чата
     * @param userUuid UUID пользователя
     * @return {@link Optional}, содержащий найденного участника, если он существует
     */
    Optional<ChatMember> findByChat_UuidAndUser_Uuid(UUID chatUuid, UUID userUuid);

    /**
     * Ищет собеседника личного чата — участника, отличного от указанного пользователя.
     *
     * @param chatUuid UUID чата
     * @param userUuid UUID текущего пользователя
     * @return {@link Optional}, содержащий найденного собеседника, если он существует
     */
    Optional<ChatMember> findByChat_UuidAndUser_UuidNot(UUID chatUuid, UUID userUuid);

    /**
     * Считает количество участников чата.
     *
     * @param chatUuid UUID чата
     * @return количество участников
     */
    long countByChat_Uuid(UUID chatUuid);

    /**
     * Ищет всех участников чата.
     *
     * @param chatUuid UUID чата
     * @return список участников чата
     */
    List<ChatMember> findByChat_Uuid(UUID chatUuid);

    /**
     * Ищет участника чата, вступившего раньше остальных.
     *
     * @param chatUuid UUID чата
     * @return {@link Optional}, содержащий найденного участника, если он существует
     */
    Optional<ChatMember> findFirstByChat_UuidOrderByJoinedAtAsc(UUID chatUuid);
}
