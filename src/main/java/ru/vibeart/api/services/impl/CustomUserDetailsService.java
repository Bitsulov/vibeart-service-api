package ru.vibeart.api.services.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.vibeart.api.models.entities.User;
import ru.vibeart.api.repositories.UserRepository;
import ru.vibeart.api.security.UserPrincipal;

import java.util.UUID;

/**
 * Реализация {@link UserDetailsService}, используемая Spring Security
 * для загрузки данных пользователя по email (username).
 * <p>
 * Отвечает за получение данных пользователя из базы данных и
 * преобразование их в объект {@link UserPrincipal}, который Spring
 * использует для создания {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}.
 * </p>
 *
 * <h2>Назначение</h2>
 * <ul>
 *   <li>Загружает пользователя по email (используется как username);</li>
 *   <li>Преобразует {@link User} в {@link UserPrincipal} — совместимую структуру для SecurityContext;</li>
 *   <li>Выбрасывает {@link UsernameNotFoundException}, если пользователь не найден.</li>
 * </ul>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Конструктор с внедрением репозитория пользователей.
     *
     * @param userRepository репозиторий для работы с таблицей пользователей
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Загружает пользователя по UUID или email.
     * <p>
     * Если пользователь найден, преобразует его в {@link UserPrincipal}.
     * Если нет — выбрасывает {@link UsernameNotFoundException}.
     * </p>
     *
     * @param uuid уникальный идентификатор пользователя
     * @return {@link UserDetails} — объект, совместимый со Spring Security
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
        User user;

        if(isUuid(uuid)) {
            user = userRepository.findByUuid(UUID.fromString(uuid))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with uuid: " + uuid));
        } else {
            user = userRepository.findByEmail(uuid)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + uuid));
        }

        return UserPrincipal.create(user);
    }

    /**
     * Проверяет, является ли переданная строка UUID.
     *
     * @param str проверяемая строка
     * @return {@code boolean}
     */
    private boolean isUuid(String str) {
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
