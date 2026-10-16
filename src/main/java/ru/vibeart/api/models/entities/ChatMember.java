package ru.vibeart.api.models.entities;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Сущность участника чата — связывает {@link Chat} и {@link User}.
 * <p>
 * Наследует автоинкрементный {@code id} от {@link BaseEntity}.
 * Уникальное ограничение на пару {@code (chat_id, user_id)} исключает
 * дублирование участников одного чата.
 * </p>
 *
 * <h2>Связи</h2>
 * <ul>
 *   <li>{@link Chat} — чат, в котором состоит участник (ManyToOne);</li>
 *   <li>{@link User} — пользователь-участник (ManyToOne).</li>
 * </ul>
 */
@Entity
@Table(
        name = "chat_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "user_id"})
)
public class ChatMember extends BaseEntity {
    private Instant joinedAt;
    private Chat chat;
    private User user;
    private boolean isActive;
    private boolean isOwner;

    public ChatMember() {}

    public ChatMember(Chat chat, User user, boolean isActive, boolean isOwner, Instant joinedAt) {
        this.chat = chat;
        this.user = user;
        this.isActive = isActive;
        this.isOwner = isOwner;
        this.joinedAt = joinedAt;
    }

    @Column(nullable = false)
    public Instant getJoinedAt() {
        return joinedAt;
    }
    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    public Chat getChat() {
        return chat;
    }
    public void setChat(Chat chat) {
        this.chat = chat;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    @Column(nullable = false)
    public boolean getActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }

    @Column(nullable = false)
    public boolean getOwner() {
        return isOwner;
    }
    public void setOwner(boolean owner) {
        isOwner = owner;
    }
}
