package ru.vibeart.api.exceptions;

/**
 * Исключение, сигнализирующее о неудачной аутентификации пользователя.
 * <p>
 * Обрабатывается в {@link ru.vibeart.api.exceptions.handlers.GlobalExceptionHandler}
 * и преобразуется в ответ с кодом 401 (Unauthorized).
 * </p>
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
