package ru.vibeart.api.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Повторная отправка шестизначного кода подтверждения изменения пароля")
public class SendCodePasswordRequest {
    private String email;

    @Schema(description = "Почта", example = "example@test.com")
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Invalid email")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
