package ru.vibeart.api.models.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

/**
 * Сущность тега для категоризации контента.
 * <p>
 * Содержит уникальное название {@code title} и дату создания.
 * Наследует автоинкрементный {@code id} от {@link BaseEntity}.
 * </p>
 *
 * <h2>Связи</h2>
 * <ul>
 *   <li>{@link Post} — посты с данным тегом (ManyToMany, обратная сторона; владеет {@link Post}, join-таблица {@code post_tags});</li>
 *   <li>{@link Community} — сообщества с данным тегом (ManyToMany, обратная сторона; владеет {@link Community}, join-таблица {@code community_tags}).</li>
 * </ul>
 */
@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {
    private String title;
    private Instant createdAt;
    private List<Post> posts;
    private List<Community> communities;

    public Tag() {}

    @Column(nullable = false, unique = true)
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @Column(nullable = false)
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @ManyToMany(mappedBy = "tags")
    public List<Post> getPosts() {
        return posts;
    }
    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    @ManyToMany(mappedBy = "tags")
    public List<Community> getCommunities() {
        return communities;
    }
    public void setCommunities(List<Community> communities) {
        this.communities = communities;
    }
}
