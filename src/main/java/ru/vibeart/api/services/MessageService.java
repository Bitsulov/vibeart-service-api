package ru.vibeart.api.services;

import org.springframework.data.domain.Slice;
import ru.vibeart.api.dtos.message.CreateMessageRequest;
import ru.vibeart.api.dtos.message.MessageResponse;
import ru.vibeart.api.dtos.message.UpdateMessageRequest;

import java.time.Instant;
import java.util.UUID;

/**
 * Сервис для работы с данными сообщений.
 */
public interface MessageService {
    /**
     * Возвращает сообщения чата с курсорной пагинацией, от новых к старым.
     *
     * @param id UUID чата
     * @param before дата, раньше которой нужно вернуть сообщения
     * @param limit максимальное количество сообщений за один запрос
     * @return срез с данными сообщений
     */
    Slice<MessageResponse> getMessages(UUID id, Instant before, int limit);

    /**
     * Создаёт сообщение собеседнику от имени текущего пользователя. Если личного
     * чата с собеседником ещё нет, он создаётся автоматически.
     *
     * @param createMessageRequest объект с данными нового сообщения
     * @return объект с данными созданного сообщения
     */
    MessageResponse createMessage(CreateMessageRequest createMessageRequest);

    /**
     * Находит сообщение по UUID, изменяет его текст и возвращает.
     *
     * @param id UUID сообщения
     * @param updateMessageRequest объект с новыми данными сообщения
     * @return объект с данными изменённого сообщения
     */
    MessageResponse updateMessage(UUID id, UpdateMessageRequest updateMessageRequest);

    /**
     * Находит сообщение по UUID и удаляет его.
     *
     * @param id UUID сообщения
     */
    void deleteMessage(UUID id);
}
