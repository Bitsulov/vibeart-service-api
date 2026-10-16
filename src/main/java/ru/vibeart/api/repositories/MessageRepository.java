package ru.vibeart.api.repositories;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.vibeart.api.models.entities.Message;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью {@link Message}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя стандартные CRUD-операции
 * (создание, чтение, обновление, удаление) и добавляет методы для поиска сообщений.
 * </p>
 *
 * <h2>Основные возможности</h2>
 * <ul>
 *   <li>{@link #findByChat_UuidAndCreatedAtLessThanOrderByCreatedAtDesc(UUID, Instant, Limit)} —
 *       постраничный поиск сообщений чата с курсорной пагинацией;</li>
 *   <li>{@link #findByUuid(UUID)} — поиск сообщения по UUID;</li>
 *   <li>{@link #findFirstByChat_UuidOrderByCreatedAtDesc(UUID)} — поиск последнего сообщения чата.</li>
 * </ul>
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
    /**
     * Ищет сообщения чата раньше указанной даты, от новых к старым.
     *
     * @param chatId UUID чата
     * @param before курсор — верхняя граница даты создания
     * @param limit максимальное количество сообщений
     * @return срез с найденными сообщениями
     */
    Slice<Message> findByChat_UuidAndCreatedAtLessThanOrderByCreatedAtDesc(UUID chatId, Instant before, Limit limit);

    /**
     * Ищет сообщение по UUID.
     *
     * @param uuid UUID сообщения
     * @return {@link Optional}, содержащий найденное сообщение, если оно существует
     */
    Optional<Message> findByUuid(UUID uuid);

    /**
     * Ищет последнее по дате создания сообщение чата.
     *
     * @param chatId UUID чата
     * @return {@link Optional}, содержащий найденное сообщение, если оно существует
     */
    Optional<Message> findFirstByChat_UuidOrderByCreatedAtDesc(UUID chatId);
}
