package ru.vibeart.api.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Статус доверия пользователя или сообщества.
 * <p>
 * Используется для модерации: определяет, прошёл ли аккаунт проверку.
 * При сериализации в JSON возвращается значение {@code title} благодаря аннотации {@link com.fasterxml.jackson.annotation.JsonValue}.
 * </p>
 *
 * <h2>Значения</h2>
 * <ul>
 *   <li>{@code TRUST} — аккаунт проверен и заслуживает доверия (значение по умолчанию);</li>
 *   <li>{@code UNTRUST} — аккаунт не прошёл проверку.</li>
 * </ul>
 */
public enum TrustStatus {
    TRUST(1, "trust"),
    UNTRUST(2, "untrust");

    private int id;
    private String title;

    TrustStatus(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    // Указывает при сериализации в JSON использовать это значения
    @JsonValue
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
