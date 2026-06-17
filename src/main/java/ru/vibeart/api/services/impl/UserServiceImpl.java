package ru.vibeart.api.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.vibeart.api.dtos.user.UserDetailResponse;
import ru.vibeart.api.exceptions.ResourceNotFoundException;
import ru.vibeart.api.models.entities.User;
import ru.vibeart.api.repositories.UserRepository;
import ru.vibeart.api.services.UserService;
import ru.vibeart.api.utils.AuthUtil;

/**
 * Реализация {@link UserService}.
 * <p>
 * Использует {@link AuthUtil} для определения текущего пользователя
 * и {@link ModelMapper} для преобразования сущности {@link User} в DTO.
 * </p>
 */
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuthUtil authUtil;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param userRepository репозиторий пользователей
     * @param modelMapper конвертер для преобразования DTO и сущностей
     * @param authUtil утилита для получения данных текущего аутентифицированного пользователя
     */
    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, AuthUtil authUtil) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
    }

    /**
     * Возвращает данные текущего аутентифицированного пользователя.
     * @return объект с данными пользователя
     * @throws ResourceNotFoundException если пользователь не найден
     */
    @Override
    public UserDetailResponse getPrincipalUser() {
        String ownerEmail = authUtil.getPrincipalEmail();
        User user = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + ownerEmail));

        UserDetailResponse response = modelMapper.map(user, UserDetailResponse.class);
        response.setRole(user.getRole().getName().name());
        return response;
    }
}
