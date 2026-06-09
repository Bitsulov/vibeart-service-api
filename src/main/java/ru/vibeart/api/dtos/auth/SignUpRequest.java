package ru.vibeart.api.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "Регистрация клиента")
public class SignUpRequest {
    private String email;
    private String password;
    private String confirmPassword;

    public SignUpRequest() {}

    @Schema(description = "Почта", example = "example@test.com")
    @NotEmpty(message = "Почта не может быть пустая")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Schema(description = "Пароль", example = "password")
    @NotEmpty(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать не меньше 6 символов")
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @Schema(description = "Подтверждение пароля", example = "password")
    @NotEmpty
    public String getConfirmPassword() {
        return confirmPassword;
    }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
