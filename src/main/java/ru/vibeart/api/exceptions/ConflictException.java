package ru.vibeart.api.exceptions;

/**
 * Исключение, сигнализирующее о конфликте с текущим состоянием ресурса.
 * <p>
 * Обрабатывается в {@link ru.vibeart.api.exceptions.handlers.GlobalExceptionHandler}
 * и преобразуется в ответ с кодом 409 (Conflict).
 * </p>
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
