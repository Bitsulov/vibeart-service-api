package ru.vibeart.api.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vibeart.api.models.entities.Like;
import ru.vibeart.api.models.entities.Post;
import ru.vibeart.api.models.entities.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Like}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя стандартные CRUD-операции
 * (создание, чтение, обновление, удаление) и добавляет методы для поиска лайка пользователя
 * на посте и массового поиска активных лайков среди списка постов.
 * </p>
 *
 * <h2>Назначение</h2>
 * <p>
 * Отвечает за доступ к данным лайков публикаций в базе данных.
 * </p>
 *
 * <h2>Основные возможности</h2>
 * <ul>
 *   <li>{@link #findByUserAndPost(User, Post)} — поиск лайка по паре пользователь/пост;</li>
 *   <li>{@link #findActiveLikedPostIds(User, Collection)} — поиск постов из списка, активно лайкнутых пользователем.</li>
 * </ul>
 *
 */
public interface LikeRepository extends JpaRepository<Like, Long> {
    /**
     * Ищет лайк по паре «пользователь — пост», независимо от значения флага {@code isActive}.
     *
     * @param user пользователь
     * @param post публикация
     * @return {@link Optional}, содержащий найденный лайк, если он существует
     */
    Optional<Like> findByUserAndPost(User user, Post post);

    /**
     * Ищет среди переданных публикаций те, что активно лайкнуты указанным пользователем.
     *
     * @param user пользователь
     * @param posts коллекция публикаций для проверки
     * @return список внутренних идентификаторов публикаций, лайкнутых пользователем
     */
    @Query("select l.post.id from Like l where l.user = :user and l.post in :posts and l.active = true")
    List<Long> findActiveLikedPostIds(@Param("user") User user, @Param("posts") Collection<Post> posts);
}
