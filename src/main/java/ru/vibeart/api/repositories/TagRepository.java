package ru.vibeart.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vibeart.api.models.entities.Tag;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Tag}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя стандартные CRUD-операции
 * (создание, чтение, обновление, удаление) и добавляет метод для поиска по названию тега.
 * </p>
 *
 * <h2>Назначение</h2>
 * <p>
 * Отвечает за доступ к данным тегов публикаций в базе данных.
 * </p>
 *
 * <h2>Основные возможности</h2>
 * <ul>
 *   <li>{@link #findByTitle(String)} — поиск тега по названию.</li>
 * </ul>
 *
 */
public interface TagRepository extends JpaRepository<Tag, Long> {
    /**
     * Ищет тег по названию.
     *
     * @param title название тега
     * @return {@link Optional}, содержащий найденный тег, если он существует
     */
    Optional<Tag> findByTitle(String title);
}