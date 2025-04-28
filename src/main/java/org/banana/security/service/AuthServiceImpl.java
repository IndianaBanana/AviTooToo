package org.banana.security.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.user.UserLoginRequestDto;
import org.banana.dto.user.UserRegisterRequestDto;
import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserMapper;
import org.banana.entity.User;
import org.banana.exception.UserAddingEmailException;
import org.banana.exception.UserAddingPhoneException;
import org.banana.repository.UserRepository;
import org.banana.security.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(JwtService jwtService, AuthenticationManager authManager, UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional // todo поменять чтобы возвращал токен (возможно надо как то доработать JwtService)
    public String register(UserRegisterRequestDto requestDto) {
        log.debug("entering register method in {}", this.getClass().getSimpleName());
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new UserAddingEmailException(requestDto.getUsername());
        }
        if (userRepository.existsByPhone(requestDto.getPhone())) {
            throw new UserAddingPhoneException(requestDto.getPhone());
        }
        String password = passwordEncoder.encode(requestDto.getPassword());
        User user = UserMapper.INSTANCE.userRegisterRequestDtoToUser(requestDto);
        user.setPassword(password);
        user.setRole(UserRole.ROLE_USER);
        userRepository.save(user);
        return jwtService.generateToken(requestDto.getUsername());
    }

    @Override
    public String verify(UserLoginRequestDto requestDto) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword()));
        if (authentication != null && authentication.isAuthenticated()) {
            return jwtService.generateToken(requestDto.getUsername());
        } else {
            return "failed to verify user";
        }
    }
}
