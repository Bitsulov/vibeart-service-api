package ru.vibeart.api.configs;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Конфигурация общих бинов приложения.
 * <p>
 * Содержит настройку:
 * <ul>
 *     <li>{@link ModelMapper} — для преобразования DTO ↔ Entity;</li>
 *     <li>{@link CorsConfigurationSource} — для разрешения CORS-запросов
 *         (в частности, для фронтенда на localhost:3000/3001);</li>
 * </ul>
 *
 * <h2>Назначение</h2>
 * Данный класс используется как общий Spring-конфиг, доступный во всех компонентах сервиса.
 * </p>
 *
 * <h2>Безопасность и CORS</h2>
 * <p>
 * Текущие настройки позволяют принимать запросы с указанных фронтенд-доменов:
 * {@code http://localhost:3000} и {@code http://localhost:3001}.
 * Для production рекомендуется:
 * <ul>
 *   <li>Перенести разрешённые домены в конфигурацию (application.yml);</li>
 *   <li>Ограничить методы (если не требуется DELETE/PUT);</li>
 *   <li>Отключить {@code allowCredentials=true}, если не используется аутентификация через cookies.</li>
 * </ul>
 * </p>
 */
@Configuration
public class BeanConfig {

    /**
     * Бин {@link ModelMapper} для автоматического преобразования
     * между DTO и Entity-объектами.
     * <p>
     * Пример использования:
     * <pre>
     * BookDTO dto = modelMapper.map(bookEntity, BookDTO.class);
     * </pre>
     *
     * @return экземпляр {@link ModelMapper}
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * Конфигурация CORS для разрешения запросов с фронтенда.
     * <p>
     * Разрешает домены:
     * <ul>
     *   <li>http://localhost:3000</li>
     *   <li>http://localhost:3001</li>
     * </ul>
     *
     * <p>Разрешённые методы: GET, POST, PUT, DELETE, OPTIONS.</p>
     *
     * @return экземпляр {@link CorsConfigurationSource}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Разрешение на запросы со следующих адресов:
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001"));
        // Разрешение на использование следующих HTTP-методов:
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Разрешение на использование любых заголовков в запросе (Подставляются все заголовки, а не *)
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        // Разрешение на передачу заголовков авторизации, куки-файлов и двусторонней аутентификации
        configuration.setAllowCredentials(true);

        // Применить конфиг ко всем эндпоинтом
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
