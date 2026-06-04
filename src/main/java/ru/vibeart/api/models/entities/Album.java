package ru.vibeart.api.models.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Сущность альбома — именованной коллекции постов.
 * <p>
 * Содержит название, описание, обложку ({@code imageUrl}), дату создания
 * и денормализованный счётчик постов {@code worksCount}.
 * Автором может быть пользователь ({@link User}) или сообщество ({@link Community}) — взаимоисключающие поля.
 * Наследует автоинкрементный {@code id} от {@link BaseEntity}; клиентам передаётся {@code uuid}.
 * </p>
 *
 * <h2>Связи</h2>
 * <ul>
 *   <li>{@link User} — автор-пользователь (ManyToOne, опционально);</li>
 *   <li>{@link Community} — автор-сообщество (ManyToOne, опционально);</li>
 *   <li>{@link Post} — посты альбома (ManyToMany, владелец связи; join-таблица {@code album_posts}).</li>
 * </ul>
 */
@Entity
@Table(name = "albums")
public class Album extends BaseEntity {
    private UUID uuid;
    private String title;
    private String description;
    private Integer worksCount = 0;
    private User authorUser;
    private Community authorCommunity;
    private String imageUrl;
    private Instant createdAt;
    private List<Post> posts;

    public Album() {}

    @Column(nullable = false, unique = true)
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Column(nullable = false, length = 15)
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @Column(length = 200)
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Column(nullable = false)
    public Integer getWorksCount() {
        return worksCount;
    }
    public void setWorksCount(Integer worksCount) {
        this.worksCount = worksCount;
    }

    @ManyToOne
    @JoinColumn(name = "author_user_id")
    public User getAuthorUser() {
        return authorUser;
    }
    public void setAuthorUser(User authorUser) {
        this.authorUser = authorUser;
    }

    @ManyToOne
    @JoinColumn(name = "author_community_id")
    public Community getAuthorCommunity() {
        return authorCommunity;
    }
    public void setAuthorCommunity(Community authorCommunity) {
        this.authorCommunity = authorCommunity;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Column(nullable = false)
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @ManyToMany
    // joinColumns - текущая таблица, inverseJoinColumns - другая таблица
    @JoinTable(
        name = "album_posts",
        joinColumns = @JoinColumn(name = "album_id"),
        inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    public List<Post> getPosts() {
        return posts;
    }
    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}
