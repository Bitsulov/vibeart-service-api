package ru.vibeart.api.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.vibeart.api.dtos.user.UserDetailResponse;
import ru.vibeart.api.exceptions.ResourceNotFoundException;
import ru.vibeart.api.models.entities.User;
import ru.vibeart.api.repositories.UserRepository;
import ru.vibeart.api.services.UserService;
import ru.vibeart.api.utils.AuthUtil;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuthUtil authUtil;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, AuthUtil authUtil) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
    }

    @Override
    public UserDetailResponse getPrincipalUser() {
        String ownerEmail = authUtil.getPrincipalEmail();
        User user = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + ownerEmail));
        return modelMapper.map(user, UserDetailResponse.class);
    }
}
