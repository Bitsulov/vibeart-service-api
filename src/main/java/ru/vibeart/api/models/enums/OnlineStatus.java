package ru.vibeart.api.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Перечисление (enum), представляющее онлайн-статус пользователя.
 * <p>
 * Используется для отображения состояния пользователя в системе,
 * например, в списках чатов, профилях и административных панелях.
 * </p>
 *
 * <h2>Назначение</h2>
 * <p>
 * Отражает, находится ли пользователь в сети в данный момент времени.
 * Может использоваться как в бизнес-логике, так и для сериализации в JSON.
 * </p>
 *
 * <h2>Список статусов</h2>
 * <table border="1" cellspacing="0" cellpadding="4">
 *   <tr><th>Статус</th><th>ID</th><th>Описание</th></tr>
 *   <tr><td>ONLINE</td><td>1</td><td>Пользователь в сети</td></tr>
 *   <tr><td>OFFLINE</td><td>2</td><td>Пользователь не в сети</td></tr>
 * </table>
 *
 * <h2>Особенности сериализации</h2>
 * <p>
 * Благодаря аннотации {@link JsonValue} метод {@link #getStatus()} используется
 * при преобразовании enum в JSON.
 * Например:
 * </p>
 * <pre>
 * {
 *   "status": "Онлайн"
 * }
 * </pre>
 */
public enum OnlineStatus {

    /** Пользователь находится в сети. */
    ONLINE(1, "online"),

    /** Пользователь не в сети. */
    OFFLINE(2, "offline");

    /** Уникальный числовой идентификатор статуса. */
    private int number;

    /** Человекочитаемое описание статуса. */
    private String status;

    OnlineStatus(int number, String status) {
        this.number = number;
        this.status = status;
    }

    /**
     * Возвращает числовой идентификатор статуса.
     *
     * @return номер статуса
     */
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Возвращает человекочитаемое описание статуса.
     * <p>Аннотировано {@link JsonValue}, чтобы в JSON отображалось значение, а не имя enum.</p>
     *
     * @return локализованное название статуса
     */
    @JsonValue
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
