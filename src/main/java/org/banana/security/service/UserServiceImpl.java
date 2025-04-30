package org.banana.security.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.dto.user.UserMapper;
import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.entity.User;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        User user = UserMapper.INSTANCE.userRegisterRequestDtoToUser(requestDto);
        user.setPassword(password);
        user.setRole(UserRole.ROLE_USER);
        userRepository.save(user);
        return jwtService.generateToken(user);
    }

    @Override
    public String verify(UserLoginRequestDto requestDto) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword()));
        if (authentication != null && authentication.isAuthenticated()) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            return jwtService.generateToken(principal.getUser());
        } else {
            throw new BadCredentialsException("Bad credentials");
        }
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
        userRepository.save(user);
        return UserMapper.INSTANCE.userToUserResponseDto(user);
    }

    @Override
    @Transactional
    public String updatePassword(UserPasswordUpdateRequestDto requestDto) {
        User user = getUserPrincipal().getUser();
        verify(new UserLoginRequestDto(user.getUsername(), requestDto.getOldPassword()));
        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(user);
        return jwtService.generateToken(user);
    }

    @Override
    @Transactional
    public String updateUsername(UserUsernameUpdateRequestDto requestDto) {
        User user = getUserPrincipal().getUser();
        verify(new UserLoginRequestDto(user.getUsername(), requestDto.getPassword()));

        if (user.getUsername().equals(requestDto.getNewUsername()))
            throw new UserUpdateOldEqualsNewDataException(SAME_USERNAME);

        if (userRepository.existsByUsername(requestDto.getNewUsername()))
            throw new UserUsernameAlreadyExistsException(requestDto.getNewUsername());

        user.setUsername(requestDto.getNewUsername());
        userRepository.save(user);
        return jwtService.generateToken(user);
    }

    @Override
    @Transactional
    public String updatePhone(UserPhoneUpdateRequestDto requestDto) {
        User user = getUserPrincipal().getUser();
        verify(new UserLoginRequestDto(user.getUsername(), requestDto.getPassword()));
        if (user.getPhone().equals(requestDto.getNewPhone()))
            throw new UserUpdateOldEqualsNewDataException(SAME_PHONE);

        if (userRepository.existsByPhone(requestDto.getNewPhone())) {
            throw new UserPhoneAlreadyExistsException(requestDto.getNewPhone());
        }
        user.setPhone(requestDto.getNewPhone());
        userRepository.save(user);
        return jwtService.generateToken(user);
    }

    @Override
    public void deleteUser(UserLoginRequestDto requestDto) {
        User user = getUserPrincipal().getUser();
        verify(requestDto);
        userRepository.delete(user);
    }

    private UserPrincipal getUserPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
