package org.banana.security.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.user.UserMapper;
import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.entity.User;
import org.banana.exception.UserNotFoundException;
import org.banana.repository.UserRepository;
import org.banana.security.UserPrincipal;
import org.banana.security.UserRole;
import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserPasswordUpdateRequestDto;
import org.banana.security.dto.UserPhoneUpdateRequestDto;
import org.banana.security.dto.UserRegisterRequestDto;
import org.banana.security.dto.UserUsernameUpdateRequestDto;
import org.banana.security.exception.UserPhoneAlreadyExistsException;
import org.banana.security.exception.UserUpdateOldEqualsNewDataException;
import org.banana.security.exception.UserUsernameAlreadyExistsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.banana.security.exception.UserUpdateOldEqualsNewDataException.UserUpdateExceptionMessage.SAME_FIRST_NAME_AND_LAST_NAME;
import static org.banana.security.exception.UserUpdateOldEqualsNewDataException.UserUpdateExceptionMessage.SAME_PHONE;
import static org.banana.security.exception.UserUpdateOldEqualsNewDataException.UserUpdateExceptionMessage.SAME_USERNAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public String register(UserRegisterRequestDto requestDto) {
        log.debug("entering register method in {}", this.getClass().getSimpleName());
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new UserUsernameAlreadyExistsException(requestDto.getUsername());
        }
        if (userRepository.existsByPhone(requestDto.getPhone())) {
            throw new UserPhoneAlreadyExistsException(requestDto.getPhone());
        }
        String password = passwordEncoder.encode(requestDto.getPassword());
        User user = userMapper.userRegisterRequestDtoToUser(requestDto);
        user.setPassword(password);
        user.setRole(UserRole.ROLE_USER);
//        user.setUserId(UUID.randomUUID());
        user = userRepository.save(user);
        return jwtService.generateToken(user);
    }

    @Override
    public String verify(UserLoginRequestDto requestDto) {
        UserPrincipal principal = (UserPrincipal) checkUserCredentialsAndReturnAuthentication(requestDto).getPrincipal();
        return jwtService.generateToken(principal.getUser());
    }

    private Authentication checkUserCredentialsAndReturnAuthentication(UserLoginRequestDto requestDto) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword()));
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("Bad credentials");
        }
        return authentication;
    }

    @Override
    public UserResponseDto getCurrentUser() {
//        userRepository.findByUsername("banana@banana.com");
//        userRepository.findById(UUID.fromString("f11bf8e8-22e1-4021-9d78-947aae9786ff"));
//        return null;
        return userMapper.userToUserResponseDto(userRepository.findById(getUserPrincipal().getUser().getId())
                .orElseThrow(() -> new UserNotFoundException(getUserPrincipal().getUser().getId())));
//        return userMapper.userToUserResponseDto(getUserPrincipal().getUser());
    }

    @Override
    public UserResponseDto findById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.userToUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(UserUpdateRequestDto userUpdateRequestDto) {
        UserPrincipal principal = getUserPrincipal();
        User user = principal.getUser();
        if (user.getFirstName().equals(userUpdateRequestDto.getFirstName())
            && user.getLastName().equals(userUpdateRequestDto.getLastName()))
            throw new UserUpdateOldEqualsNewDataException(SAME_FIRST_NAME_AND_LAST_NAME);
        user.setFirstName(userUpdateRequestDto.getFirstName());
        user.setLastName(userUpdateRequestDto.getLastName());
        user = userRepository.save(user);
        return userMapper.userToUserResponseDto(user);
    }

    @Override
    @Transactional
    public String updatePassword(UserPasswordUpdateRequestDto requestDto) {
        User user = getUserPrincipal().getUser();
        checkUserCredentialsAndReturnAuthentication(new UserLoginRequestDto(user.getUsername(), requestDto.getOldPassword()));
        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        user = userRepository.save(user);
        return jwtService.generateToken(user);
    }

    @Override
    @Transactional
    public String updateUsername(UserUsernameUpdateRequestDto requestDto) {
        User user = getUserPrincipal().getUser();
        if (user.getUsername().equals(requestDto.getNewUsername()))
            throw new UserUpdateOldEqualsNewDataException(SAME_USERNAME);

        checkUserCredentialsAndReturnAuthentication(new UserLoginRequestDto(user.getUsername(), requestDto.getPassword()));

        if (userRepository.existsByUsername(requestDto.getNewUsername()))
            throw new UserUsernameAlreadyExistsException(requestDto.getNewUsername());

        user.setUsername(requestDto.getNewUsername());
        user = userRepository.save(user);
        return jwtService.generateToken(user);
    }

    @Override
    @Transactional
    public String updatePhone(UserPhoneUpdateRequestDto requestDto) {
        User user = getUserPrincipal().getUser();
        if (user.getPhone().equals(requestDto.getNewPhone()))
            throw new UserUpdateOldEqualsNewDataException(SAME_PHONE);

        checkUserCredentialsAndReturnAuthentication(new UserLoginRequestDto(user.getUsername(), requestDto.getPassword()));

        if (userRepository.existsByPhone(requestDto.getNewPhone())) {
            throw new UserPhoneAlreadyExistsException(requestDto.getNewPhone());
        }
        user.setPhone(requestDto.getNewPhone());
        user = userRepository.save(user);
        return jwtService.generateToken(user);
    }

    @Override
    public void deleteUser(UserLoginRequestDto requestDto) {
        User currentUser = getUserPrincipal().getUser();

        if (!currentUser.getUsername().equals(requestDto.getUsername()))
            throw new BadCredentialsException("Bad credentials");

        checkUserCredentialsAndReturnAuthentication(requestDto);
        userRepository.delete(currentUser);
    }

    private UserPrincipal getUserPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
