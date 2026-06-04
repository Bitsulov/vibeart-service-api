package ru.vibeart.api.models.entities;

import jakarta.persistence.*;

/**
 * Сущность подписки одного пользователя на другого.
 * <p>
 * Хранит пару «подписчик ({@code follower}) → отслеживаемый ({@code following})» и флаг активности {@code isActive}.
 * Уникальное ограничение на пару {@code (follower_id, following_id)} исключает дублирование подписок.
 * Наследует автоинкрементный {@code id} от {@link BaseEntity}.
 * </p>
 */
@Entity
@Table(
    name = "subscription",
    uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"})
)
public class Subscription extends BaseEntity {
    private User follower;
    private User following;
    private boolean isActive;

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
    public User getFollowing() {
        return following;
    }
    public void setFollowing(User following) {
        this.following = following;
    }

    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
}
