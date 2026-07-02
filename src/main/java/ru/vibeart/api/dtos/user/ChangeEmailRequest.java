package ru.vibeart.api.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на изменение электронной почты пользователя")
public class ChangeEmailRequest {
    private String email;

    @Schema(
            title = "Адрес электронной почты",
            description = "Адрес электронной почты, на который заменяется существующая почта",
            example = "new@test.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
