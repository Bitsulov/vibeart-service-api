package ru.vibeart.api.dtos.post;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.vibeart.api.dtos.community.CommunityResponse;
import ru.vibeart.api.dtos.user.UserResponse;
import ru.vibeart.api.models.enums.AIStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Полные данные публикации")
public class PostResponse {
    private UUID uuid;
    private String title;
    private String description;
    private Long likesCount;
    private Long commentsCount;
    private Long reportsCount;
    private AIStatus aiStatus;
    private String imageUrl;
    private Instant createdAt;
    private boolean isLiked;
    private boolean isReported;
    private UserResponse author;
    private CommunityResponse community;
    private List<String> tags;

    @Schema(description = "UUID публикации", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Schema(description = "Название публикации", example = "title")
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @Schema(description = "Описание публикации", example = "Описание публикации")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Schema(description = "Дата создания публикации", example = "2026-07-13T10:15:30Z")
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Schema(description = "Состояние лайка текущим пользователем на пост", example = "2026-07-13T10:15:30Z")
    public boolean isLiked() {
        return isLiked;
    }
    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    @Schema(description = "Признак жалобы текущего пользователя на публикацию", example = "false")
    public boolean isReported() {
        return isReported;
    }
    public void setReported(boolean reported) {
        isReported = reported;
    }

    @Schema(description = "Адрес изображения публикации", example = "https://storage.vibeart.ru/posts/3fa85f64.jpg")
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Schema(description = "Статус проверки публикации на ИИ-контент", example = "good")
    public AIStatus getAiStatus() {
        return aiStatus;
    }
    public void setAiStatus(AIStatus aiStatus) {
        this.aiStatus = aiStatus;
    }

    @Schema(description = "Количество жалоб на публикации", example = "0")
    public Long getReportsCount() {
        return reportsCount;
    }
    public void setReportsCount(Long reportsCount) {
        this.reportsCount = reportsCount;
    }

    @Schema(description = "Количество комментариев на публикации", example = "0")
    public Long getCommentsCount() {
        return commentsCount;
    }
    public void setCommentsCount(Long commentsCount) {
        this.commentsCount = commentsCount;
    }

    @Schema(description = "Количество лайков на публикации", example = "0")
    public Long getLikesCount() {
        return likesCount;
    }
    public void setLikesCount(Long likesCount) {
        this.likesCount = likesCount;
    }

    @Schema(description = "Данные автора пользователя  или null")
    public UserResponse getAuthor() {
        return author;
    }
    public void setAuthor(UserResponse author) {
        this.author = author;
    }

    @Schema(description = "Данные автора сообщества или null")
    public CommunityResponse getCommunity() {
        return community;
    }
    public void setCommunity(CommunityResponse community) {
        this.community = community;
    }

    @Schema(description = "Список названий тегов публикации", example = "[\"landscape\", \"portrait\"]")
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
