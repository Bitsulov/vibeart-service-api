package ru.vibeart.api.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Обновление пары токенов")
public class RefreshRequest {
    private String refreshToken;

    @Schema(description = "Обновление токена")
    @NotEmpty(message = "Токен не может быть пустым")
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
