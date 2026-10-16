package ru.vibeart.api.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StompEvents {
    CREATED(1, "created"),
    UPDATED(2, "updated"),
    DELETED(3, "deleted");

    private int id;
    private String title;

    StompEvents(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    @JsonValue
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
