package ru.vibeart.api.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Тип кода подтверждения, привязанного к пользователю.
 * <p>
 * Используется в сущности {@link ru.vibeart.api.models.entities.VerificationCode} как часть
 * уникального ограничения {@code (user_id, code_type)}.
 * При сериализации в JSON возвращается значение {@code title} благодаря аннотации {@link com.fasterxml.jackson.annotation.JsonValue}.
 * </p>
 *
 * <h2>Значения</h2>
 * <ul>
 *   <li>{@code REGISTER} — подтверждение регистрации (верификация email);</li>
 *   <li>{@code CHANGE_EMAIL} — подтверждение смены адреса электронной почты;</li>
 *   <li>{@code CHANGE_PASSWORD} — подтверждение смены пароля.</li>
 * </ul>
 */
public enum VerificationCodesType {
    REGISTER(1, "register"),
    CHANGE_EMAIL(2, "change_email"),
    CHANGE_PASSWORD(3, "change_password");

    private int id;
    private String title;

    VerificationCodesType(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    @JsonValue
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
