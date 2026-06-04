package ru.vibeart.api.models.entities;

import jakarta.persistence.*;
import ru.vibeart.api.models.enums.RoleEnum;

import java.util.List;

/**
 * Сущность {@code Role} представляет роль пользователя в системе.
 * <p>
 * Каждая роль определяется значением перечисления {@link RoleEnum} и может быть
 * связана с несколькими пользователями.
 * </p>
 *
 * <p>
 * Данный класс наследуется от {@link BaseEntity}, который содержит уникальный идентификатор.
 * </p>
 *
 */
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {
    /** Название роли, представленное в виде перечисления {@link RoleEnum}. */
    private RoleEnum name;

    /** Список пользователей, имеющих данную роль. */
    private List<User> users;

    public Role() {}

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    public RoleEnum getName() {
        return name;
    }
    public void setName(RoleEnum name) {
        this.name = name;
    }

    // Связь определена в поле "role" в таблице "users"
    @OneToMany(mappedBy = "role")
    public List<User> getUsers() {
        return users;
    }
    public void setUsers(List<User> users) {
        this.users = users;
    }
}
