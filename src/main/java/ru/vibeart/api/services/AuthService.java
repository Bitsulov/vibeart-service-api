package ru.vibeart.api.services;

import ru.vibeart.api.dtos.auth.*;

import java.util.Locale;

/**
 * Сервис аутентификации и регистрации пользователей.
 */
public interface AuthService {
    /**
     * Регистрация пользователя в системе
     * @param signUpRequest объект для регистрации пользователя
     * @param locale объект locale с текущим языком пользователя
     */
    void register(SignUpRequest signUpRequest, Locale locale);

    /**
     * Повторная отправка кода подтверждения почты
     * @param sendCodeRequest объект с адресом почты для повторной отправки кода
     * @param locale объект locale с текущим языком пользователя
     */
    void send(SendCodeRequest sendCodeRequest, Locale locale);

    /**
     * Подтверждения регистрации (подтверждения почты) и авторизация
     * @param verifyRequest объект для подтверждения регистрации
     * @return объект с токенами доступа и обновления
     */
    AuthResponse verify(VerifyRequest verifyRequest);

    /**
     * Авторизация пользователя по адресу электронной почты и паролю
     * @param signInRequest объект с учётными данными для входа
     * @return объект с токенами доступа и обновления
     */
    AuthResponse login(SignInRequest signInRequest);

    /**
     * Обновление access и refresh токенов по действующему refresh токену
     * @param refreshRequest объект с refresh токеном
     * @return объект с новыми токенами доступа и обновления
     */
    AuthResponse refresh(RefreshRequest refreshRequest);
}
