package ru.vibeart.api.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "Подтверждение смены пароля")
public class ConfirmChangePasswordRequest {
    private String email;
    private String verificationCode;

    @Schema(
            title = "Адрес электронной почты",
            description = "Адрес электронной почты, на который заменяется существующая почта",
            example = "test@example.com"
    )
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Invalid email")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Schema(
            title = "Код подтверждения",
            description = "Код подтверждения, отправленный на новый адрес электронной почты для его подтверждения",
            example = "123456",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "Code cannot be empty")
    @Size(min = 6, max = 6, message = "The code is must contains 6 symbols")
    public String getVerificationCode() {
        return verificationCode;
    }
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
