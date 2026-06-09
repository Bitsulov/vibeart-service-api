package ru.vibeart.api.services;

import ru.vibeart.api.dtos.user.UserDetailResponse;

public interface UserService {
    UserDetailResponse getPrincipalUser();
}
