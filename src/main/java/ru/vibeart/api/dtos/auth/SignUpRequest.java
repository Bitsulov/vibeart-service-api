package ru.vibeart.api.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Регистрация клиента")
public class SignUpRequest {
    private String email;
    private String password;
    private String confirmPassword;

    public SignUpRequest() {}

    @Schema(description = "Почта", example = "example@test.com")
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Schema(description = "Пароль", example = "password")
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 64, message = "Password must contain at least 6 and maximum 64 symbols")
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @Schema(description = "Подтверждение пароля", example = "password")
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 64, message = "Password confirmation must contain at least 6 and maximum 64 symbols")
    public String getConfirmPassword() {
        return confirmPassword;
    }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
