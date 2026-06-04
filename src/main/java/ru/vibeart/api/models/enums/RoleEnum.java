package ru.vibeart.api.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Роли пользователей в системе.
 * <p>
 * Используется в сущности {@link ru.vibeart.api.models.entities.Role}.
 * При сериализации в JSON возвращается значение {@code title} благодаря аннотации {@link com.fasterxml.jackson.annotation.JsonValue}.
 * </p>
 *
 * <h2>Значения</h2>
 * <ul>
 *   <li>{@code USER} — обычный пользователь;</li>
 *   <li>{@code ADMIN} — администратор с расширенными правами.</li>
 * </ul>
 */
public enum RoleEnum {
    USER(1, "user"),
    ADMIN(2, "admin");

    private int id;
    private String title;

    RoleEnum(int id, String title) {
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
