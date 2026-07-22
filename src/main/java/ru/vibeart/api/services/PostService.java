package ru.vibeart.api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import ru.vibeart.api.dtos.post.PostCreateDetails;
import ru.vibeart.api.dtos.post.PostResponse;
import ru.vibeart.api.dtos.post.PostUpdateDetails;

import java.util.UUID;

/**
 * Сервис для работы с публикациями.
 */
public interface PostService {
    /**
     * Возвращает список публикаций, при необходимости отфильтрованных по альбому.
     *
     * @param albumId UUID альбома для фильтрации публикаций, или {@code null} для получения всех публикаций
     * @param pageable параметры пагинации
     * @return список объектов с данными публикаций
     */
    Page<PostResponse> getPosts(UUID albumId, Pageable pageable);

    /**
     * Возвращает публикацию по её UUID.
     *
     * @param uuid UUID публикации
     * @return объект с данными публикации
     */
    PostResponse getPostByUuid(UUID uuid);

    /**
     * Создаёт публикацию от имени автора.
     *
     * @param postCreateDetails объект с данными новой публикации
     * @param file изображение публикации
     * @return объект с данными созданной публикации
     */
    PostResponse createPost(PostCreateDetails postCreateDetails, MultipartFile file);

    /**
     * Изменяет данные публикации от имени автора.
     *
     * @param id UUID публикации
     * @param postUpdateDetails объект с новыми данными публикации
     * @param file новое изображение публикации
     * @return объект с данными публикации
     */
    PostResponse updatePost(UUID id, PostUpdateDetails postUpdateDetails, MultipartFile file);

    /**
     * Ставит или снимает лайк пользователя на публикации.
     *
     * @param postId UUID публикации
     */
    void toggleLike(UUID postId);

    /**
     * Увеличивает счётчик жалоб публикации на единицу.
     *
     * @param postId UUID публикации
     */
    void report(UUID postId);

    /**
     * Удаляет публикацию по UUID.
     *
     * @param id UUID публикации
     */
    void deletePostByUuid(UUID id);
}
