package ru.vibeart.api.services.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.vibeart.api.models.entities.User;
import ru.vibeart.api.repositories.UserRepository;
import ru.vibeart.api.security.UserPrincipal;

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
     * Загружает пользователя по email (используется как username).
     * <p>
     * Если пользователь найден, преобразует его в {@link UserPrincipal}.
     * Если нет — выбрасывает {@link UsernameNotFoundException}.
     * </p>
     *
     * @param email email пользователя
     * @return {@link UserDetails} — объект, совместимый со Spring Security
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserPrincipal.create(user);
    }
}
