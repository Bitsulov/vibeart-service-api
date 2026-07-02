package ru.vibeart.api.dtos.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "Верификация почты")
public class VerifyRequest {
    private String email;
    private String verificationCode;

    @Schema(description = "Почта", example = "test@example.com")
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Invalid email")
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Schema(description = "Код подтверждения", example = "123456")
    @NotEmpty(message = "Code cannot be empty")
    @Size(min = 6, max = 6, message = "Code must contain at least 6 symbols")
    public String getVerificationCode() {
        return verificationCode;
    }
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
