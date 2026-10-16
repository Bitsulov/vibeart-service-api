package ru.vibeart.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vibeart.api.dtos.chat.ChatResponse;
import ru.vibeart.api.dtos.chat.CreateChatRequest;
import ru.vibeart.api.dtos.message.MessageResponse;
import ru.vibeart.api.dtos.user.UserResponse;
import ru.vibeart.api.services.ChatService;
import ru.vibeart.api.services.MessageService;

import java.time.Instant;
import java.util.UUID;

@Controller
@RequestMapping("/api/chat")
@Tag(
        name = "Чаты",
        description = "Получение, создание чатов и выход из них"
)
public class ChatController {
    private final ChatService chatService;
    private final MessageService messageService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param chatService сервис данных чатов
     * @param messageService сервис данных сообщений
     */
    public ChatController(ChatService chatService, MessageService messageService) {
        this.chatService = chatService;
        this.messageService = messageService;
    }

    @Operation(
            summary = "Получение списка чатов текущего пользователя с пагинацией",
            description = "Возвращает постраничный список чатов, в которых состоит текущий пользователь.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список чатов успешно получен"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или сервера")
            }
    )
    @GetMapping
    public ResponseEntity<Page<ChatResponse>> getChats(Pageable pageable) {
        Page<ChatResponse> response = chatService.getChats(pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Получение сообщений чата с курсорной пагинацией",
            description = "Возвращает сообщения чата постранично, от новых к старым.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список сообщений успешно получен"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "403", description = "Пользователь не состоит в этом чате"),
                    @ApiResponse(responseCode = "404", description = "Чат не найден"),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или сервера")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<Slice<MessageResponse>> getMessages(
            @Parameter(description = "UUID чата", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Дата, раньше которой нужно вернуть сообщения")
            @RequestParam(required = false) Instant before,
            @Parameter(description = "Максимальное количество сообщений за один запрос")
            @RequestParam(defaultValue = "20") int limit
    ) {
        Slice<MessageResponse> response = messageService.getMessages(id, before, limit);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Получение данных собеседника личного чата",
            description = "Возвращает данные собеседника чата, если чат личный.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные собеседника успешно получены"),
                    @ApiResponse(responseCode = "400", description = "Чат не является личным"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "403", description = "Пользователь не состоит в этом чате"),
                    @ApiResponse(responseCode = "404", description = "Чат не найден или в нём не осталось собеседника"),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или сервера")
            }
    )
    @GetMapping("/{id}/companion")
    public ResponseEntity<UserResponse> getChatCompanion(
            @Parameter(description = "UUID чата", required = true)
            @PathVariable UUID id
    ) {
        UserResponse response = chatService.getChatCompanion(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Получение личного чата с собеседником",
            description = "Возвращает личный чат текущего пользователя с указанным собеседником, если он уже существует.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Чат успешно получен"),
                    @ApiResponse(responseCode = "400", description = "Запрошен чат с самим собой"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "404", description = "Собеседник не найден или чат с ним ещё не создан"),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или сервера")
            }
    )
    @GetMapping("/companion")
    public ResponseEntity<ChatResponse> getChatByCompanion(
            @Parameter(description = "UUID собеседника", required = true)
            @RequestParam UUID companionUuid
    ) {
        ChatResponse response = chatService.getChatByCompanion(companionUuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Создание чата",
            description = "Создаёт чат с произвольным числом участников от имени текущего пользователя. " +
                    "Текущий пользователь автоматически добавляется как владелец чата.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Чат успешно создан"),
                    @ApiResponse(responseCode = "400", description = "Неверные данные"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "403", description = "Профиль создателя отключён"),
                    @ApiResponse(responseCode = "404", description = "Один или несколько участников не найдены"),
                    @ApiResponse(responseCode = "409", description = "Профиль одного или нескольких участников отключён"),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или сервера")
            }
    )
    @PostMapping
    public ResponseEntity<ChatResponse> createChat(
            @Parameter(description = "UUID участников чата", required = true)
            @Valid @RequestBody CreateChatRequest createChatRequest
    ) {
        ChatResponse response = chatService.createChat(createChatRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Выход из чата по UUID",
            description = "Удаляет текущего пользователя из участников чата.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно вышел из чата"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "403", description = "Пользователь не состоит в этом чате"),
                    @ApiResponse(responseCode = "404", description = "Чат не найден"),
                    @ApiResponse(responseCode = "500", description = "Ошибка базы данных или сервера")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<String> leaveChat(
            @Parameter(description = "UUID чата", required = true)
            @PathVariable UUID id
    ) {
        chatService.leaveChat(id);
        return new ResponseEntity<>("Left chat successfully", HttpStatus.OK);
    }
}
