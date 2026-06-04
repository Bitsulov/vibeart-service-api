package ru.vibeart.api.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Статус проверки поста на чувствительный контент.
 * <p>
 * Используется для модерации публикаций.
 * При сериализации в JSON возвращается значение {@code title} благодаря аннотации {@link com.fasterxml.jackson.annotation.JsonValue}.
 * </p>
 *
 * <h2>Значения</h2>
 * <ul>
 *   <li>{@code CHECKED} — пост проверен и признан безопасным;</li>
 *   <li>{@code UNCHECKED} — пост ещё не прошёл проверку (значение по умолчанию).</li>
 * </ul>
 */
public enum NSFWStatus {
    CHECKED(1, "checked"),
    UNCHECKED(2, "unchecked");

    private int id;
    private String title;

    NSFWStatus(int id, String title) {
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
