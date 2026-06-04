package ru.vibeart.api.models.entities;

import jakarta.persistence.*;

/**
 * Базовый суперкласс для всех JPA-сущностей приложения.
 * <p>
 * Не является самостоятельной таблицей в БД — аннотация {@link MappedSuperclass}
 * означает, что поля этого класса включаются в таблицы дочерних сущностей.
 * </p>
 *
 * <h2>Назначение</h2>
 * <p>
 * Предоставляет единый первичный ключ {@code id} для всех сущностей.
 * Значение генерируется базой данных (стратегия {@link GenerationType#IDENTITY}).
 * Внутренний {@code id} не раскрывается клиентам — для этого сущности используют UUID.
 * </p>
 */
@MappedSuperclass
public class BaseEntity {
    /** Суррогатный первичный ключ, формируемый базой данных. */
    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
