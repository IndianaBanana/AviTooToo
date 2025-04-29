package org.banana.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.entity.User;
import org.banana.exception.UserNotFoundException;
import org.banana.repository.UserRepository;
import org.banana.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by Banana on 25.04.2025
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(UserUpdateRequestDto userUpdateRequestDto) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = principal.getUser().getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        user.setFirstName(userUpdateRequestDto.getFirstName());
        user.setLastName(userUpdateRequestDto.getLastName());
        return null;
    }
}
