package ru.vibeart.api.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Повторная отправка кода подтверждения")
public class SendCodeRequest {
    private String email;

    @Schema(description = "Почта", example = "example@test.com")
    @NotEmpty(message = "Почта не может быть пустая")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
