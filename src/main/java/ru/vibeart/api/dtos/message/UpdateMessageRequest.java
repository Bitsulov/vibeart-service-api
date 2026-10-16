package ru.vibeart.api.dtos.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для изменения сообщения")
public class UpdateMessageRequest {
    private String text;

    @Schema(description = "Новый текст сообщения", example = "Привет!")
    @NotBlank(message = "Text cannot be empty")
    @Size(max = 1000, message = "Text cannot be longer 1000 symbols")
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}
