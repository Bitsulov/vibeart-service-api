package ru.vibeart.api.dtos.message;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.vibeart.api.dtos.user.UserResponse;
import ru.vibeart.api.models.enums.MessageStatus;

import java.util.UUID;

@Schema(description = "Данные сообщения для отображения")
public class MessageResponse {
    private UUID uuid;
    private String text;
    private String createdAt;
    private MessageStatus status;
    private UserResponse author;

    @Schema(description = "UUID сообщения", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Schema(description = "Текст сообщения", example = "Привет!")
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    @Schema(description = "Дата создания сообщения", example = "2026-07-13T10:15:30Z")
    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Schema(description = "Статус сообщения")
    public MessageStatus getStatus() {
        return status;
    }
    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    @Schema(description = "Данные автора сообщения")
    public UserResponse getAuthor() {
        return author;
    }
    public void setAuthor(UserResponse author) {
        this.author = author;
    }
}
