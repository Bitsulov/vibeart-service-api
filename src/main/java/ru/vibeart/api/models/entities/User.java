package ru.vibeart.api.models.entities;

import jakarta.persistence.*;
import ru.vibeart.api.models.enums.OnlineStatus;
import ru.vibeart.api.models.enums.TrustStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Сущность пользователя приложения.
 * <p>
 * Хранит учётные данные, профиль и связи с контентом пользователя.
 * Наследует автоинкрементный {@code id} от {@link BaseEntity}.
 * Клиентам передаётся {@code uuid}, внутренний {@code id} не раскрывается.
 * </p>
 *
 * <h2>Связи</h2>
 * <ul>
 *   <li>{@link Role} — роль пользователя (ManyToOne);</li>
 *   <li>{@link Post}, {@link Album} — контент пользователя (OneToMany, cascade ALL);</li>
 *   <li>{@link Subscription} — подписки через join-сущность (follower / following);</li>
 *   <li>{@link Community} — сообщества, которыми владеет пользователь;</li>
 *   <li>{@link Community} — сообщества, в которых пользователь является администратором
 *       (ManyToMany, обратная сторона; владеет {@link Community}, join-таблица {@code community_admins}).</li>
 * </ul>
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private UUID uuid;
    private String name;
    private String email;
    private String username;
    private String password;
    private String description;
    private String avatarUrl;
    private Integer worksCount = 0;
    private Integer subscribersCount = 0;
    private Integer subscribesCount = 0;
    private Instant createdAt;
    private TrustStatus trustStatus = TrustStatus.TRUST;
    private OnlineStatus onlineStatus = OnlineStatus.OFFLINE;
    private List<Album> albums;
    private List<Post> posts;
    private List<Subscription> followers;
    private List<Subscription> followings;
    private List<Community> communities;
    private List<VerificationCode> codes;
    private List<Community> administeredCommunities;
    private Role role;
    private boolean enabled;

    public User() {}

    @Column(nullable = false, unique = true)
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Column(length = 20)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Column(nullable = false, unique = true)
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Column(unique = true, length = 10)
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @Column(length = 64)
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @Column(length = 200)
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Column(nullable = false)
    public Integer getWorksCount() {
        return worksCount;
    }
    public void setWorksCount(Integer worksCount) {
        this.worksCount = worksCount;
    }

    @Column(nullable = false)
    public Integer getSubscribersCount() {
        return subscribersCount;
    }
    public void setSubscribersCount(Integer subscribersCount) {
        this.subscribersCount = subscribersCount;
    }

    @Column(nullable = false)
    public Integer getSubscribesCount() {
        return subscribesCount;
    }
    public void setSubscribesCount(Integer subscribesCount) {
        this.subscribesCount = subscribesCount;
    }

    @Column(nullable = false)
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TrustStatus getTrustStatus() {
        return trustStatus;
    }
    public void setTrustStatus(TrustStatus trustStatus) {
        this.trustStatus = trustStatus;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }
    public void setOnlineStatus(OnlineStatus onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    @OneToMany(mappedBy = "authorUser", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Album> getAlbums() {
        return albums;
    }
    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    @OneToMany(mappedBy = "authorUser", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Post> getPosts() {
        return posts;
    }
    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    // Cascade - все операции над пользователем применяются и к его подпискам.
    // OrphanRemoval - при удалении подписки у пользователя удалится и сама подписка
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Subscription> getFollowers() {
        return followers;
    }
    public void setFollowers(List<Subscription> followers) {
        this.followers = followers;
    }

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Subscription> getFollowings() {
        return followings;
    }
    public void setFollowings(List<Subscription> followings) {
        this.followings = followings;
    }

    @OneToMany(mappedBy = "owner", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    public List<Community> getCommunities() {
        return communities;
    }
    public void setCommunities(List<Community> communities) {
        this.communities = communities;
    }

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    public List<VerificationCode> getCodes() {
        return codes;
    }
    public void setCodes(List<VerificationCode> codes) {
        this.codes = codes;
    }

    @ManyToMany(mappedBy = "admins")
    public List<Community> getAdministeredCommunities() {
        return administeredCommunities;
    }
    public void setAdministeredCommunities(List<Community> administeredCommunities) {
        this.administeredCommunities = administeredCommunities;
    }

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
