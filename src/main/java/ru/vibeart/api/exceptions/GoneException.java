package ru.vibeart.api.exceptions;

/**
 * Исключение, сигнализирующее о том, что запрашиваемый ресурс безвозвратно устарел.
 * <p>
 * Обрабатывается в {@link ru.vibeart.api.exceptions.handlers.GlobalExceptionHandler}
 * и преобразуется в ответ с кодом 410 (Gone).
 * </p>
 */
public class GoneException extends RuntimeException {
    public GoneException(String message) {
        super(message);
    }
}
