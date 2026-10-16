package ru.vibeart.api.events;

import java.util.UUID;

/**
 * Событие выхода последнего участника из чата.
 *
 * @param chatId UUID чата, оставшегося без участников
 */
public record DeleteEmptyChatEvent(UUID chatId) {
}
