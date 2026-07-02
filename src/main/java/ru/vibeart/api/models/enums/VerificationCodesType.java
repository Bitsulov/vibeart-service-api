package ru.vibeart.api.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VerificationCodesType {
    REGISTER(1, "register"),
    CHANGE_EMAIL(2, "change_email"),
    CHANGE_PASSWORD(3, "change_password");

    private int id;
    private String title;

    VerificationCodesType(int id, String title) {
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
