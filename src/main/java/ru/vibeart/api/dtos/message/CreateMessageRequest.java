package ru.vibeart.api.dtos.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Данные для создания сообщения")
public class CreateMessageRequest {
    private String text;
    private UUID companionUuid;

    @Schema(description = "Текст сообщения", example = "Привет!")
    @NotBlank(message = "Text cannot be empty")
    @Size(max = 1000, message = "Text cannot be longer 1000 symbols")
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    @Schema(description = "UUID собеседника", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull(message = "Companion UUID cannot be empty")
    public UUID getCompanionUuid() {
        return companionUuid;
    }
    public void setCompanionUuid(UUID companionUuid) {
        this.companionUuid = companionUuid;
    }
}
