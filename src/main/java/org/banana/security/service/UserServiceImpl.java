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
import org.banana.security.UserRole;
import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserPasswordUpdateRequestDto;
import org.banana.security.dto.UserPhoneUpdateRequestDto;
import org.banana.security.dto.UserPrincipal;
import org.banana.security.dto.UserRegisterRequestDto;
import org.banana.security.dto.UserUsernameUpdateRequestDto;
import org.banana.exception.UserPhoneAlreadyExistsException;
import org.banana.exception.UserUpdateOldEqualsNewDataException;
import org.banana.exception.UserUsernameAlreadyExistsException;
import org.banana.util.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.banana.exception.UserUpdateOldEqualsNewDataException.UserUpdateExceptionMessage.SAME_FIRST_NAME_AND_LAST_NAME;
import static org.banana.exception.UserUpdateOldEqualsNewDataException.UserUpdateExceptionMessage.SAME_PHONE;
import static org.banana.exception.UserUpdateOldEqualsNewDataException.UserUpdateExceptionMessage.SAME_USERNAME;

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
        log.info("register({}) in {}", requestDto, getClass().getSimpleName());

        if (userRepository.existsByUsername(requestDto.getUsername()))
            throw new UserUsernameAlreadyExistsException(requestDto.getUsername());

        if (userRepository.existsByPhone(requestDto.getPhone()))
            throw new UserPhoneAlreadyExistsException(requestDto.getPhone());

        String password = passwordEncoder.encode(requestDto.getPassword());
        User user = userMapper.userRegisterRequestDtoToUser(requestDto);
        user.setPassword(password);
        user.setRole(UserRole.ROLE_USER);
        user = userRepository.save(user);
        log.debug("registered user {}", user);
        return jwtService.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getPhone());
    }

    @Override
    public String verify(UserLoginRequestDto requestDto) {
        log.info("verify({}) in {}", requestDto, getClass().getSimpleName());
        UserPrincipal principal = (UserPrincipal) checkUserCredentialsAndReturnAuthentication(requestDto).getPrincipal();
        return jwtService.generateToken(principal.getId(), principal.getUsername(), principal.getRole(), principal.getPhone());
    }

    @Override
    public UserResponseDto getCurrentUser() {
        log.info("getCurrentUser() in {}", getClass().getSimpleName());
        UUID id = SecurityUtils.getCurrentUserPrincipal().getId();

        return userMapper.userToUserResponseDto(userRepository.findFetchedById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Override
    public UserResponseDto findById(UUID id) {
        log.info("findById({}) in {}", id, getClass().getSimpleName());

        User user = userRepository.findFetchedById(id).orElseThrow(() -> new UserNotFoundException(id));

        log.debug("user found: {}", user);
        return userMapper.userToUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(UserUpdateRequestDto userUpdateRequestDto) {
        log.info("updateUser({}) in {}", userUpdateRequestDto, getClass().getSimpleName());
        UserPrincipal userPrincipal = SecurityUtils.getCurrentUserPrincipal();
        String newFirstName = userUpdateRequestDto.getFirstName();
        String newLastName = userUpdateRequestDto.getLastName();

        if (userPrincipal.getFirstName().equals(newFirstName) && userPrincipal.getLastName().equals(newLastName))
            throw new UserUpdateOldEqualsNewDataException(SAME_FIRST_NAME_AND_LAST_NAME);

        User user = userRepository.findFetchedById(userPrincipal.getId())
                .orElseThrow(() -> new UserNotFoundException(userPrincipal.getId()));

        user.setFirstName(newFirstName);
        user.setLastName(newLastName);
        user = userRepository.save(user);
        log.debug("user updated: {}", user);
        return userMapper.userToUserResponseDto(user);
    }

    @Override
    @Transactional
    public String updatePassword(UserPasswordUpdateRequestDto requestDto) {
        log.info("updatePassword() in {}", getClass().getSimpleName());
        UserPrincipal user = SecurityUtils.getCurrentUserPrincipal();

        checkUserCredentialsAndReturnAuthentication(new UserLoginRequestDto(user.getUsername(), requestDto.getOldPassword()));

        String encodedPass = passwordEncoder.encode(requestDto.getNewPassword());
        userRepository.updatePassword(user.getId(), encodedPass);

        log.debug("password updated for user {}", user);
        return jwtService.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getPhone());
    }

    @Override
    @Transactional
    public String updateUsername(UserUsernameUpdateRequestDto requestDto) {
        log.info("updateUsername({}) in {}", requestDto, getClass().getSimpleName());
        UserPrincipal user = SecurityUtils.getCurrentUserPrincipal();

        if (user.getUsername().equals(requestDto.getNewUsername()))
            throw new UserUpdateOldEqualsNewDataException(SAME_USERNAME);

        checkUserCredentialsAndReturnAuthentication(new UserLoginRequestDto(user.getUsername(), requestDto.getPassword()));

        if (userRepository.existsByUsername(requestDto.getNewUsername()))
            throw new UserUsernameAlreadyExistsException(requestDto.getNewUsername());

        userRepository.updateUsername(user.getId(), requestDto.getNewUsername());

        log.debug("username updated");
        return jwtService.generateToken(user.getId(), requestDto.getNewUsername(), user.getRole(), user.getPhone());
    }

    @Override
    @Transactional
    public String updatePhone(UserPhoneUpdateRequestDto requestDto) {
        log.info("updatePhone({}) in {}", requestDto, getClass().getSimpleName());
        UserPrincipal user = SecurityUtils.getCurrentUserPrincipal();

        if (user.getPhone().equals(requestDto.getNewPhone()))
            throw new UserUpdateOldEqualsNewDataException(SAME_PHONE);

        checkUserCredentialsAndReturnAuthentication(new UserLoginRequestDto(user.getUsername(), requestDto.getPassword()));

        if (userRepository.existsByPhone(requestDto.getNewPhone()))
            throw new UserPhoneAlreadyExistsException(requestDto.getNewPhone());

        userRepository.updatePhone(user.getId(), requestDto.getNewPhone());

        log.debug("phone updated");
        return jwtService.generateToken(user.getId(), user.getUsername(), user.getRole(), requestDto.getNewPhone());
    }

    @Override
    @Transactional
    public void deleteUser(UserLoginRequestDto requestDto) {
        log.info("deleteUser({}) in {}", requestDto, getClass().getSimpleName());
        UserPrincipal currentUser = SecurityUtils.getCurrentUserPrincipal();

        if (!currentUser.getUsername().equals(requestDto.getUsername()))
            throw new AccessDeniedException("Only owner can delete account");

        checkUserCredentialsAndReturnAuthentication(requestDto);

        userRepository.deleteById(currentUser.getId());
    }

    private Authentication checkUserCredentialsAndReturnAuthentication(UserLoginRequestDto requestDto) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword()));

        if (authentication == null || !authentication.isAuthenticated())
            throw new BadCredentialsException("Bad credentials");
        log.debug("authenticated user {}", authentication.getPrincipal());
        return authentication;
    }
}
