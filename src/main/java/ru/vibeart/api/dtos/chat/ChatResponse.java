package ru.vibeart.api.dtos.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.vibeart.api.dtos.message.MessageResponse;
import ru.vibeart.api.dtos.user.UserResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Данные чата для отображения")
public class ChatResponse {
    private UUID uuid;
    private String imageUrl;
    private Instant createdAt;
    private List<UserResponse> members;
    private MessageResponse lastMessage;

    @Schema(description = "UUID чата", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Schema(description = "Ссылка на изображение чата", example = "http://host:9000/bucket/path/file.jpg")
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Schema(description = "Дата создания чата", example = "2026-07-13T10:15:30Z")
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Schema(description = "Участники чата")
    public List<UserResponse> getMembers() {
        return members;
    }
    public void setMembers(List<UserResponse> members) {
        this.members = members;
    }

    @Schema(description = "Последнее сообщение чата")
    public MessageResponse getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(MessageResponse lastMessage) {
        this.lastMessage = lastMessage;
    }
}
