package ru.vibeart.api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.vibeart.api.dtos.chat.ChatResponse;
import ru.vibeart.api.dtos.chat.CreateChatRequest;
import ru.vibeart.api.dtos.user.UserResponse;

import java.util.UUID;

/**
 * Сервис для работы с данными чатов.
 */
public interface ChatService {
    /**
     * Возвращает постраничный список чатов текущего пользователя.
     *
     * @param pageable параметры пагинации
     * @return страница с данными чатов
     */
    Page<ChatResponse> getChats(Pageable pageable);

    /**
     * Возвращает данные собеседника личного чата.
     *
     * @param id UUID чата
     * @return объект с данными собеседника
     */
    UserResponse getChatCompanion(UUID id);

    /**
     * Возвращает личный чат текущего пользователя с указанным собеседником,
     * если он уже существует.
     *
     * @param companionUuid UUID собеседника
     * @return объект с данными чата
     */
    ChatResponse getChatByCompanion(UUID companionUuid);

    /**
     * Создаёт чат с произвольным числом участников от имени текущего пользователя.
     * Текущий пользователь автоматически добавляется как владелец чата.
     *
     * @param createChatRequest объект с UUID участников чата
     * @return объект с данными созданного чата
     */
    ChatResponse createChat(CreateChatRequest createChatRequest);

    /**
     * Удаляет текущего пользователя из участников чата. Если после этого
     * в чате не остаётся участников, чат удаляется целиком.
     *
     * @param id UUID чата
     */
    void leaveChat(UUID id);

    /**
     * Деактивирует чат, если в нём не осталось участников.
     *
     * @param id UUID чата
     */
    void deactivateIfEmpty(UUID id);
}
