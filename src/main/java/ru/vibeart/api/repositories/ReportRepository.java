package ru.vibeart.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vibeart.api.models.entities.Post;
import ru.vibeart.api.models.entities.Report;
import ru.vibeart.api.models.entities.User;

import java.util.Collection;
import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Report}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя стандартные CRUD-операции
 * (создание, чтение, обновление, удаление) и добавляет методы для проверки существования жалобы
 * и массового поиска пожалованных постов среди списка постов.
 * </p>
 *
 * <h2>Назначение</h2>
 * <p>
 * Отвечает за доступ к данным жалоб на публикации в базе данных.
 * </p>
 *
 * <h2>Основные возможности</h2>
 * <ul>
 *   <li>{@link #existsByUserAndPost(User, Post)} — проверка наличия жалобы пользователя на пост;</li>
 *   <li>{@link #findReportedPostIds(User, Collection)} — поиск постов из списка, на которые пожаловался пользователь.</li>
 * </ul>
 *
 */
public interface ReportRepository extends JpaRepository<Report, Long> {
    /**
     * Проверяет, существует ли жалоба указанного пользователя на публикацию.
     *
     * @param user пользователь
     * @param post публикация
     * @return {@code true}, если жалоба существует, иначе {@code false}
     */
    boolean existsByUserAndPost(User user, Post post);

    /**
     * Ищет среди переданных публикаций те, на которые пожаловался указанный пользователь.
     *
     * @param user пользователь
     * @param posts коллекция публикаций для проверки
     * @return список внутренних идентификаторов публикаций, на которые подана жалоба
     */
    @Query("select r.post.id from Report r where r.user = :user and r.post in :posts")
    List<Long> findReportedPostIds(@Param("user") User user, @Param("posts") Collection<Post> posts);
}