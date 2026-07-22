package ru.vibeart.api.models.entities;

import jakarta.persistence.*;
import ru.vibeart.api.models.enums.VerificationCodesType;

import java.time.Instant;

/**
 * Сущность кода подтверждения (регистрация, смена email, смена пароля и т.д.).
 * <p>
 * Хранит сам код, срок его действия и целевое значение ({@code targetValue} — например,
 * новый email при смене адреса), тип задаётся {@link VerificationCodesType}.
 * Уникальное ограничение на пару {@code (user_id, code_type)} исключает одновременное
 * существование двух активных кодов одного типа у одного пользователя.
 * Наследует автоинкрементный {@code id} от {@link BaseEntity}.
 * </p>
 */
@Entity
@Table(
        name = "verification_codes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "code_type"})
)
public class VerificationCode extends BaseEntity {
    private String code;
    private Instant codeExpiresAt;
    private String targetValue;
    private User user;
    private VerificationCodesType type;

    public VerificationCode() {}

    public VerificationCode(
            String code,
            Instant codeExpiresAt,
            String targetValue,
            User user,
            VerificationCodesType type
    ) {
        this.code = code;
        this.codeExpiresAt = codeExpiresAt;
        this.targetValue = targetValue;
        this.user = user;
        this.type = type;
    }

    @Column(nullable = false, length = 6)
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    @Column(nullable = false)
    public Instant getCodeExpiresAt() {
        return codeExpiresAt;
    }
    public void setCodeExpiresAt(Instant codeExpiresAt) {
        this.codeExpiresAt = codeExpiresAt;
    }

    @Column(nullable = false)
    public String getTargetValue() {
        return targetValue;
    }
    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "code_type")
    public VerificationCodesType getType() {
        return type;
    }
    public void setType(VerificationCodesType type) {
        this.type = type;
    }
}
