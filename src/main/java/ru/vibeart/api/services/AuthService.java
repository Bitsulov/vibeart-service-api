package ru.vibeart.api.services;

import ru.vibeart.api.dtos.auth.*;

public interface AuthService {
    void register(SignUpRequest signUpRequest);
    void verify(VerifyRequest verifyRequest);
    AuthResponse login(SignInRequest signInRequest);
    AuthResponse refresh(RefreshRequest refreshRequest);
}
