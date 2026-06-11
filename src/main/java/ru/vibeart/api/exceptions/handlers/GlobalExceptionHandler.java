package ru.vibeart.api.exceptions.handlers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.vibeart.api.exceptions.ConflictException;
import ru.vibeart.api.exceptions.ForbiddenException;
import ru.vibeart.api.exceptions.ResourceNotFoundException;
import ru.vibeart.api.exceptions.UnauthorizedException;
import ru.vibeart.api.exceptions.models.AppError;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {
    // Ресурс не найден (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<AppError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    // Конфликт данных, ресурс уже существует (409)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<AppError> handleConflict(ConflictException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    // Неверные данные от клиента (400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AppError> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    // Нет прав для получения доступа (403)
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<AppError> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    }

    // Пользователь не авторизован (401)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<AppError> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    // Ошибка базы данных с обезличенным сообщением (500)
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<AppError> handleDatabase(DataAccessException ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred", request.getRequestURI());
    }

    // Неверное значение параметра (400)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<AppError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue();
        return buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    // Ошибка валидации (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        return buildError(HttpStatus.BAD_REQUEST, errorMessage, request.getRequestURI());
    }

    // Остальные ошибки (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppError> handleAllUnhandled(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred: " + ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<AppError> buildError(HttpStatus status, String message, String path) {
        AppError error = new AppError(
                status.value(),
                message,
                path,
                Instant.now()
        );
        return new ResponseEntity<>(error, status);
    }
}
