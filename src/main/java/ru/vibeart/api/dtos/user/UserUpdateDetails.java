package ru.vibeart.api.dtos.user;

import jakarta.validation.constraints.Size;

public class UserUpdateDetails {
    private String name;
    private String username;
    private String description;
    private boolean isDeleteAvatar;

    @Size(min = 3, max = 20, message = "Name cannot be shorter than 3 and longer than 20 symbols")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Size(min = 2, max = 10, message = "Username cannot be shorter than 2 and longer than 10 symbols")
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @Size(max = 200, message = "Description cannot be longer than 200 symbols")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDeleteAvatar() {
        return isDeleteAvatar;
    }
    public void setDeleteAvatar(boolean deleteAvatar) {
        isDeleteAvatar = deleteAvatar;
    }

    @Override
    public String toString() {
        String avatarString;
        if(isDeleteAvatar) {
            avatarString = "avatar was deleted";
        } else {
            avatarString = "avatar was not deleted";
        }
        return "name: " + name + "; " + "username: " + username + "; " + "description: " + description + "; " + avatarString;
    }
}
