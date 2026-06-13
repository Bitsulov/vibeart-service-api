package ru.vibeart.api.exceptions;

/**
 * Исключение, сигнализирующее об отсутствии запрашиваемого ресурса.
 * <p>
 * Обрабатывается в {@link ru.vibeart.api.exceptions.handlers.GlobalExceptionHandler}
 * и преобразуется в ответ с кодом 404 (Not Found).
 * </p>
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
