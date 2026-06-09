package ru.vibeart.api.exceptions.models;

import java.time.Instant;

/**
 * Универсальная модель ошибки приложения.
 * <p>
 * Используется для формирования унифицированных ответов при возникновении
 * исключений в REST-контроллерах.
 * </p>
 *
 * <h2>Назначение</h2>
 * Класс применяется при обработке исключений в глобальном exception handler’е.
 * <h2>Поля</h2>
 * <ul>
 *   <li><b>statusCode</b> — HTTP-код ошибки (например, 400, 404, 500);</li>
 *   <li><b>message</b> — описание ошибки или сообщение из исключения;</li>
 *   <li><b>path</b> — URL-запрос, при котором произошла ошибка;</li>
 *   <li><b>timestamp</b> — время возникновения ошибки.</li>
 * </ul>
 *
 * <h2>Пример JSON-ответа</h2>
 * <pre>
 * {
 *   "statusCode": 404,
 *   "message": "User not found",
 *   "path": "/api/users/999",
 *   "timestamp": "2025-11-02T21:30:00Z"
 * }
 * </pre>
 */
public class AppError {

    /** HTTP-код ошибки (например, 400, 404, 500). */
    private int statusCode;

    /** Сообщение об ошибке. */
    private String message;

    /** URI запроса, на котором произошла ошибка. */
    private String path;

    /** Время возникновения ошибки. */
    private Instant timestamp;

    /** Конструктор без аргументов (требуется для сериализации). */
    public AppError() {}

    /**
     * Полный конструктор.
     *
     * @param statusCode HTTP-код ошибки
     * @param message текст ошибки
     * @param path путь запроса
     * @param timestamp  время возникновения
     */
    public AppError(int statusCode, String message, String path, Instant timestamp) {
        this.statusCode = statusCode;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
