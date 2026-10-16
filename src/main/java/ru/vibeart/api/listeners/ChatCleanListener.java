package ru.vibeart.api.listeners;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.vibeart.api.events.DeleteEmptyChatEvent;
import ru.vibeart.api.services.ChatService;

/**
 * Слушатель события {@link DeleteEmptyChatEvent}.
 */
@Component
public class ChatCleanListener {
    private final ChatService chatService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param chatService сервис данных чатов
     */
    public ChatCleanListener(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Деактивирует чат, оставшийся без участников.
     *
     * @param event событие выхода последнего участника из чата
     */
    @TransactionalEventListener
    public void onEmptyChat(DeleteEmptyChatEvent event) {
        chatService.deactivateIfEmpty(event.chatId());
    }
}
