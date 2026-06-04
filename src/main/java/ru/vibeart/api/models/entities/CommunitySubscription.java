package ru.vibeart.api.models.entities;

import jakarta.persistence.*;

/**
 * Сущность подписки пользователя на сообщество.
 * <p>
 * Хранит пару «подписчик ({@link User}) → сообщество ({@link Community})» и флаг активности {@code isActive}.
 * Уникальное ограничение на пару {@code (follower_id, following_id)} исключает дублирование подписок.
 * Наследует автоинкрементный {@code id} от {@link BaseEntity}.
 * </p>
 */
@Entity
@Table(
    name = "community_subscription",
    uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"})
)
public class CommunitySubscription extends BaseEntity {
    private User follower;
    private Community following;
    private boolean isActive;

    public CommunitySubscription() {}

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    public User getFollower() {
        return follower;
    }
    public void setFollower(User follower) {
        this.follower = follower;
    }

    @ManyToOne
    @JoinColumn(name = "following_id", nullable = false)
    public Community getFollowing() {
        return following;
    }
    public void setFollowing(Community following) {
        this.following = following;
    }

    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
}
