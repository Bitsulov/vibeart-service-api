package ru.vibeart.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Провайдер JWT-токенов (JSON Web Token) для аутентификации пользователей.
 * <p>
 * Отвечает за:
 * <ul>
 *   <li>генерацию access- и refresh-токенов;</li>
 *   <li>подпись и валидацию токенов;</li>
 *   <li>извлечение информации (claims) — пользователя, роль и тип токена.</li>
 * </ul>
 * </p>
 *
 * <h2>Назначение</h2>
 * <p>
 * Используется в механизме безопасности {@link JwtAuthenticationFilter} и {@link ru.vibeart.api.configs.SecurityConfig}
 * для работы с JWT при входе и авторизации пользователя.
 * </p>
 *
 * <h2>Параметры конфигурации задаются в {@code application.yml}</h2>
 *
 * <h2>Типы токенов</h2>
 * <ul>
 *   <li><b>access</b> — используется для доступа к API (живет недолго);</li>
 *   <li><b>refresh</b> — используется для обновления access-токена без повторного входа.</li>
 * </ul>
 */
@Component
public class JwtTokenProvider {

    /** Криптографический ключ для подписи токенов. */
    private Key key;

    /** Секретная строка для формирования ключа. */
    @Value("${app.jwt.secret}")
    private String secret;

    /** Время жизни access-токена (в миллисекундах). */
    @Value("${app.jwt.access-token-validity}")
    private long accessTokenValidityInMillis;

    /** Время жизни refresh-токена (в миллисекундах). */
    @Value("${app.jwt.refresh-token-validity}")
    private long refreshTokenValidityInMillis;

    /**
     * Инициализация симметричного ключа после внедрения значений {@code @Value}.
     * <p>Метод вызывается автоматически при старте Spring-контекста.</p>
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Генерирует access-токен для указанного пользователя и роли.
     *
     * @param username имя пользователя
     * @param role роль пользователя
     * @return строка с JWT access-токеном
     */
    public String generateAccessToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidityInMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "access")
                .claim("role", role)
                .signWith(key)
                .compact();
    }

    /**
     * Генерирует refresh-токен для указанного пользователя и роли.
     *
     * @param username имя пользователя
     * @param role     роль пользователя
     * @return строка с JWT refresh-токеном
     */
    public String generateRefreshToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidityInMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "refresh")
                .claim("role", role)
                .signWith(key)
                .compact();
    }

    /**
     * Проверяет корректность и подлинность JWT-токена.
     *
     * @param token JWT-токен
     * @return {@code true}, если токен валиден, иначе {@code false}
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            System.out.println("Error JWT: " + ex.getMessage());
        }
        return false;
    }

    /**
     * Извлекает имя пользователя (subject) из токена.
     *
     * @param token JWT-токен
     * @return имя пользователя
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Возвращает тип токена (access или refresh).
     *
     * @param token JWT-токен
     * @return строка "access" или "refresh"
     */
    public String getTokenType(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();
        return (String) claims.get("type");
    }

    /**
     * Извлекает роль пользователя из токена.
     *
     * @param token JWT-токен
     * @return роль пользователя (например, "ADMIN", "USER")
     */
    public String getUserRoleFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return (String) claims.get("role");
    }
}
