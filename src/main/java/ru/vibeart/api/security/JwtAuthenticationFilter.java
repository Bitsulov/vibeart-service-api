package ru.vibeart.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.vibeart.api.services.impl.CustomUserDetailsService;

import java.io.IOException;

/**
 * Фильтр аутентификации, обрабатывающий JWT-токены в каждом HTTP-запросе.
 * <p>
 * Наследуется от {@link OncePerRequestFilter}, что гарантирует выполнение фильтра ровно один раз на запрос.
 * Отвечает за:
 * <ul>
 *     <li>Извлечение JWT-токена из заголовка {@code Authorization};</li>
 *     <li>Проверку его валидности через {@link JwtTokenProvider};</li>
 *     <li>Загрузку данных пользователя через {@link CustomUserDetailsService};</li>
 *     <li>Добавление информации о пользователе в {@link SecurityContextHolder}.</li>
 * </ul>
 *
 * <h2>Механизм работы</h2>
 * <ol>
 *   <li>Извлекается заголовок {@code Authorization: Bearer <token>};</li>
 *   <li>Проверяется, что токен не пустой и корректный;</li>
 *   <li>Если токен валиден — извлекается username;</li>
 *   <li>Загружается {@code UserDetails} пользователя;</li>
 *   <li>Создаётся объект {@link UsernamePasswordAuthenticationToken} и помещается в {@link SecurityContextHolder};</li>
 *   <li>Далее запрос продолжает цепочку фильтров.</li>
 * </ol>
 *
 * <h2>Пример заголовка запроса</h2>
 * <pre>
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 * </pre>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Конструктор фильтра JWT-аутентификации.
     *
     * @param tokenProvider провайдер JWT-токенов (валидация и извлечение username)
     * @param customUserDetailsService сервис для загрузки пользователя по username
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Основная логика фильтра:
     * <ul>
     *   <li>Получает JWT-токен из запроса;</li>
     *   <li>Проверяет его валидность;</li>
     *   <li>Если токен корректный — аутентифицирует пользователя в контексте Spring Security.</li>
     * </ul>
     *
     * @param request HTTP-запрос
     * @param response HTTP-ответ
     * @param filterChain цепочка фильтров
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = getJwtFromRequest(request);

        if(StringUtils.hasText(token) && tokenProvider.validateToken(token)
                && "access".equals(tokenProvider.getTokenType(token))) {
            String username = tokenProvider.getUsernameFromJWT(token);
            var userDetails = customUserDetailsService.loadUserByUsername(username);

            // Создаём объект аутентификации и сохраняем его в SecurityContext
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            // Добавление к аутентификации детали HTTP-запроса
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // Добавление аутентификации в контекст безопасности Spring
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Продолжение цепочки фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Извлекает JWT-токен из заголовка {@code Authorization}.
     *
     * @param request текущий HTTP-запрос
     * @return строка токена без префикса "Bearer ", либо {@code null}, если токен отсутствует
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
