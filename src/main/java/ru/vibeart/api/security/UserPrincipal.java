package ru.vibeart.api.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.vibeart.api.models.entities.User;

import java.util.Collection;
import java.util.Collections;

/**
 * Класс-обёртка над сущностью {@link User} для интеграции со Spring Security.
 * <p>
 * Реализует интерфейс {@link UserDetails}, позволяя использовать
 * объект пользователя в контексте аутентификации Spring Security.
 * </p>
 *
 * <h2>Назначение</h2>
 * <ul>
 *   <li>Предоставляет доступ к основным данным пользователя (email, пароль, роль);</li>
 *   <li>Преобразует роли в формат {@link GrantedAuthority} для SecurityContext;</li>
 *   <li>Используется при создании {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}.</li>
 * </ul>
 *
 * <h2>Роль в безопасности</h2>
 * <p>
 * Объект {@code UserPrincipal} хранится в {@link org.springframework.security.core.context.SecurityContextHolder}
 * и доступен в любой точке приложения:
 * </p>
 * <pre>
 * Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 * UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
 * </pre>
 */
public class UserPrincipal implements UserDetails {

    /** Сущность пользователя из базы данных. */
    private final User user;

    /**
     * Конструктор, принимающий объект {@link User}.
     *
     * @param user пользователь, связанный с текущим принципалом
     */
    public UserPrincipal(User user) {
        this.user = user;
    }

    /**
     * Фабричный метод для создания экземпляра {@link UserPrincipal}.
     *
     * @param user сущность пользователя
     * @return объект {@link UserPrincipal}
     */
    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }

    /**
     * Возвращает коллекцию прав доступа (authorities) пользователя.
     * <p>
     * Здесь роль пользователя преобразуется в объект {@link SimpleGrantedAuthority}.
     * </p>
     *
     * @return коллекция с единственной ролью пользователя
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getName().name()));
    }

    /**
     * Возвращает пароль пользователя.
     *
     * @return хэш пароля
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Возвращает имя пользователя (в данном случае email).
     *
     * @return email пользователя
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Проверяет, не истёк ли срок действия учётной записи.
     *
     * @return всегда {@code true}, если логика не реализована
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Проверяет, не заблокирована ли учётная запись.
     *
     * @return всегда {@code true}, если логика не реализована
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Проверяет, не истёк ли срок действия учётных данных.
     *
     * @return всегда {@code true}, если логика не реализована
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Проверяет, активна ли учётная запись пользователя.
     *
     * @return значение поля {@code enabled} у {@link User}
     */
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
