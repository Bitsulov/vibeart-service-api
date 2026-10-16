package ru.vibeart.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.vibeart.api.dtos.message.CreateMessageRequest;
import ru.vibeart.api.dtos.message.MessageResponse;
import ru.vibeart.api.dtos.message.UpdateMessageRequest;
import ru.vibeart.api.services.MessageService;

import java.util.UUID;

@Controller
@RequestMapping("/api/message")
@Tag(
        name = "Сообщения",
        description = "Создание, изменение и удаление сообщений"
)
public class MessageController {
    private final MessageService messageService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param messageService сервис данных сообщений
     */
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(
            summary = "Создание сообщения",
            description = "Создаёт сообщение собеседнику от имени текущего пользователя. " +
                    "Если личного чата с собеседником ещё нет, он создаётся автоматически.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Сообщение успешно создано"),
                    @ApiResponse(responseCode = "400", description = "Неверные данные"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "403", description = "Профиль отправителя отключён"),
                    @ApiResponse(responseCode = "404", description = "Собеседник не найден"),
                    @ApiResponse(responseCode = "409", description = "Профиль собеседника отключён"),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или сервера")
            }
    )
    @PostMapping
    public ResponseEntity<MessageResponse> createMessage(
            @Parameter(description = "Данные нового сообщения", required = true)
            @Valid @RequestBody CreateMessageRequest createMessageRequest
    ) {
        MessageResponse response = messageService.createMessage(createMessageRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Изменение сообщения по UUID",
            description = "Находит сообщение по UUID, изменяет его текст и возвращает.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Сообщение успешно изменено"),
                    @ApiResponse(responseCode = "400", description = "Неверные данные"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "403", description = "Запрос отправлен не автором сообщения"),
                    @ApiResponse(responseCode = "404", description = "Сообщение не найдено"),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или сервера")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateMessage(
            @Parameter(description = "UUID сообщения", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Новые данные сообщения", required = true)
            @Valid @RequestBody UpdateMessageRequest updateMessageRequest
    ) {
        MessageResponse response = messageService.updateMessage(id, updateMessageRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Удаление сообщения по UUID",
            description = "Находит сообщение по UUID и удаляет его.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Сообщение успешно удалено"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "403", description = "Запрос отправлен не автором сообщения"),
                    @ApiResponse(responseCode = "404", description = "Сообщение не найдено"),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или сервера")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMessage(
            @Parameter(description = "UUID сообщения", required = true)
            @PathVariable UUID id
    ) {
        messageService.deleteMessage(id);
        return new ResponseEntity<>("Message deleted successfully", HttpStatus.OK);
    }
}