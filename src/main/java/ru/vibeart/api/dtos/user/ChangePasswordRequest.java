package ru.vibeart.api.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "Изменение пароля")
public class ChangePasswordRequest {
    private String password;
    private String newPassword;
    private String confirmPassword;

    public ChangePasswordRequest() {}

    @Schema(description = "Старый пароль", example = "password")
    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must contain at least 6 symbols")
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @Schema(description = "Новый пароль", example = "password")
    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must contain at least 6 symbols")
    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Schema(description = "Подтверждение пароля", example = "password")
    @NotEmpty(message = "Password must contain at least 6 symbols")
    public String getConfirmPassword() {
        return confirmPassword;
    }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
