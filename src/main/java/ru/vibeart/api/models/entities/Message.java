package ru.vibeart.api.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ru.vibeart.api.models.enums.MessageStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Сущность сообщения в чате.
 * <p>
 * Содержит текст сообщения, дату создания и статус доставки.
 * Наследует автоинкрементный {@code id} от {@link BaseEntity}.
 * </p>
 *
 * <h2>Связи</h2>
 * <ul>
 *   <li>{@link Chat} — чат, которому принадлежит сообщение (ManyToOne);</li>
 *   <li>{@link User} — автор сообщения (ManyToOne).</li>
 * </ul>
 */
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {
    private UUID uuid;
    private String text;
    private User author;
    private Chat chat;
    private Instant createdAt;
    private MessageStatus status = MessageStatus.RECEIVED;

    public Message() {}

    @Column(nullable = false, unique = true)
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Column(nullable = false)
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    public Chat getChat() {
        return chat;
    }
    public void setChat(Chat chat) {
        this.chat = chat;
    }

    @Column(nullable = false)
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Column(nullable = false)
    public MessageStatus getStatus() {
        return status;
    }
    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}
