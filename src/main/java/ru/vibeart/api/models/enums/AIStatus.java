package ru.vibeart.api.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Статус проверки поста на контент, сгенерированный нейросетью.
 * <p>
 * Используется для модерации публикаций.
 * При сериализации в JSON возвращается значение {@code title} благодаря аннотации {@link com.fasterxml.jackson.annotation.JsonValue}.
 * </p>
 *
 * <h2>Значения</h2>
 * <ul>
 *   <li>{@code GOOD} — контент создан человеком;</li>
 *   <li>{@code PROBLEM} — контент предположительно сгенерирован AI.</li>
 * </ul>
 */
public enum AIStatus {
    GOOD(1, "good"),
    PROBLEM(2, "problem");

    private int id;
    private String title;

    AIStatus(int id, String title) {
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
