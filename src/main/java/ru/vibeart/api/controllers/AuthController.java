package ru.vibeart.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vibeart.api.dtos.auth.*;
import ru.vibeart.api.dtos.user.UserDetailResponse;
import ru.vibeart.api.services.AuthService;
import ru.vibeart.api.services.UserService;

/**
 * Контроллер аутентификации и регистрации пользователей.
 * <p>
 * Предоставляет эндпоинты для регистрации, повторной отправки кода подтверждения,
 * верификации email, входа и обновления токенов.
 * </p>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "Управление регистрацией, входом, верификацией и обновлением токенов")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param authService сервис аутентификации и регистрации
     * @param userService сервис данных пользователя
     */
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт нового пользователя и отправляет код подтверждения на email.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Регистрация успешно инициирована"),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные"),
                    @ApiResponse(responseCode = "409", description = "Пользователь с таким адресом электронной почты уже зарегистрирован и подтверждён")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Parameter(description = "Данные для регистрации", required = true)
            @Valid @RequestBody SignUpRequest request
    ) {
        authService.register(request);
        return new ResponseEntity<>("Registration initiated. Check your email for verification code.", HttpStatus.CREATED);
    }

    @Operation(
            summary = "Отправка кода подтверждения регистрации повторно",
            description = "Отправляет код подтверждения на email.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Код верификации успешно отправлен повторно"),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные"),
                    @ApiResponse(responseCode = "400", description = "Запрос отправлен слишком рано"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "409", description = "Пользователь уже верифицирован")
            }
    )
    @PostMapping("/send")
    public ResponseEntity<String> send(
            @Parameter(description = "Адрес электронной почты для отправки кода", required = true)
            @Valid @RequestBody SendCodeRequest request
    ) {
        authService.send(request);
        return new ResponseEntity<>("Code sent. Check your email for verification code.", HttpStatus.OK);
    }

    @Operation(
            summary = "Верификация пользователя",
            description = "Подтверждает email пользователя по полученному коду и авторизует его.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно верифицирован"),
                    @ApiResponse(responseCode = "400", description = "Неверный код верификации"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "409", description = "Пользователь уже верифицирован"),
                    @ApiResponse(responseCode = "410", description = "Код верификации истёк")
            }
    )
    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(
            @Parameter(description = "Данные для верификации", required = true)
            @Valid @RequestBody VerifyRequest request
    ) {
        AuthResponse response = authService.verify(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Авторизация пользователя",
            description = "Проверяет учетные данные и возвращает JWT access и refresh токены.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Авторизация прошла успешно"),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные"),
                    @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Данные для входа", required = true)
            @Valid @RequestBody SignInRequest request
    ) {
        AuthResponse response = authService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Обновление токенов",
            description = "Генерирует новый access токен на основе refresh токена.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Токены успешно обновлены"),
                    @ApiResponse(responseCode = "400", description = "Неверный или недействительный refresh токен")
            }
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Parameter(description = "Данные с refresh токеном", required = true)
            @Valid @RequestBody RefreshRequest request
    ) {
        AuthResponse response = authService.refresh(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Получить информацию о текущем пользователе",
            description = "Возвращает данные пользователя, основываясь на текущем контексте безопасности.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешное получение данных"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping("/user")
    public ResponseEntity<UserDetailResponse> getPrincipalUser() {
        UserDetailResponse response = userService.getPrincipalUser();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
