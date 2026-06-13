package ru.vibeart.api.exceptions;

/**
 * Исключение, сигнализирующее об отсутствии прав на доступ к ресурсу.
 * <p>
 * Обрабатывается в {@link ru.vibeart.api.exceptions.handlers.GlobalExceptionHandler}
 * и преобразуется в ответ с кодом 403 (Forbidden).
 * </p>
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
