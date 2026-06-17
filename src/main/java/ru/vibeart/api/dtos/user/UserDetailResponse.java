package ru.vibeart.api.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Данные пользователя")
public class UserDetailResponse {
    private UUID uuid;
    private String name;
    private String username;
    private String avatarUrl;
    private String email;
    private String role;
    private boolean enabled;

    @Schema(description = "Уникальный идентификатор пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Schema(description = "Имя пользователя", example = "Иван")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Schema(description = "Логин пользователя", example = "ivan123")
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @Schema(description = "Ссылка на фото профиля")
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Schema(description = "Почта", example = "test@example.com")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Schema(description = "Роль пользователя", example = "USER")
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Schema(description = "Подтверждён ли email пользователя")
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
