package ru.vibeart.api.models.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Сущность чата.
 * <p>
 * Наследует автоинкрементный {@code id} от {@link BaseEntity}.
 * </p>
 *
 * <h2>Связи</h2>
 * <ul>
 *   <li>{@link ChatMember} — участники чата (OneToMany, cascade ALL);</li>
 *   <li>{@link Message} — сообщения чата (OneToMany, cascade ALL).</li>
 * </ul>
 */
@Entity
@Table(name = "chats")
public class Chat extends BaseEntity {
    private UUID uuid;
    private Instant createdAt;
    private String imageUrl;
    private boolean isDirect;
    private boolean isActive = true;
    private List<ChatMember> members;
    private List<Message> messages;

    public Chat() {}

    public Chat(UUID uuid, Instant createdAt, boolean isDirect) {
        this.uuid = uuid;
        this.createdAt = createdAt;
        this.isDirect = isDirect;
    }

    @Column(nullable = false, unique = true)
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Column(nullable = false)
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Column(nullable = false)
    public boolean isDirect() {
        return isDirect;
    }
    public void setDirect(boolean direct) {
        isDirect = direct;
    }

    @Column(nullable = false)
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ChatMember> getMembers() {
        return members;
    }
    public void setMembers(List<ChatMember> members) {
        this.members = members;
    }

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Message> getMessages() {
        return messages;
    }
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
