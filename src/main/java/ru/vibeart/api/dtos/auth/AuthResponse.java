package ru.vibeart.api.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Получение данных пользователя при авторизации")
public class AuthResponse {
    private UUID uuid;
    private String accessToken;
    private String refreshToken;
    private long accessTokenExpiresIn;
    private long refreshTokenExpiresIn;

    public AuthResponse(UUID uuid, String accessToken, String refreshToken, long accessTokenExpiresIn, long refreshTokenExpiresIn) {
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    @Schema(description = "Уникальный идентификатор пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Schema(description = "Access токен")
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Schema(description = "Refresh токен")
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Schema(description = "Время жизни Access токена в секундах", example = "3600")
    public long getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }
    public void setAccessTokenExpiresIn(long accessTokenExpiresIn) {
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    @Schema(description = "Время жизни Refresh токена в секундах", example = "604800")
    public long getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }
    public void setRefreshTokenExpiresIn(long refreshTokenExpiresIn) {
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }
}
