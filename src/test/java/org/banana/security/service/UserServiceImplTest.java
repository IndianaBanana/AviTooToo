package org.banana.security.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.banana.security.exception.UserUpdateOldEqualsNewDataException.UserUpdateExceptionMessage.SAME_FIRST_NAME_AND_LAST_NAME;
import static org.banana.security.exception.UserUpdateOldEqualsNewDataException.UserUpdateExceptionMessage.SAME_PHONE;
import static org.banana.security.exception.UserUpdateOldEqualsNewDataException.UserUpdateExceptionMessage.SAME_USERNAME;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final UUID USER_ID = UUID.randomUUID();
    UserResponseDto userResponseDto;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;
    private User user;

    @BeforeEach
    void setup() {
        user = new User(USER_ID, "John", "Doe", "123", "com@com.com", "password", UserRole.ROLE_USER);
        userResponseDto = UserMapper.INSTANCE.userToUserDto(user);
    }

    @Test
    void register_whenUsernameAlreadyExists_thenThrowUserUsernameAlreadyExistsException() {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
        requestDto.setUsername("john");
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(UserUsernameAlreadyExistsException.class, () -> userService.register(requestDto));
    }

    @Test
    void register_whenPhoneAlreadyExists_thenThrowUserPhoneAlreadyExistsException() {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
        requestDto.setUsername("john");
        requestDto.setPhone("123");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(true);

        assertThrows(UserPhoneAlreadyExistsException.class, () -> userService.register(requestDto));
    }

    @Test
    void register_whenPhoneAndUsernameDoNotExist_thenReturnToken() {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
        requestDto.setUsername("john");
        requestDto.setPhone("123");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");

        String token = userService.register(requestDto);
        assertThat(token).isNotBlank().isEqualTo("token");
    }

    @Test
    void verify_shouldAuthenticateAndReturnToken() {
        UserPrincipal principal = new UserPrincipal(user);
        Authentication auth = mock(Authentication.class);

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principal);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken(user)).thenReturn("token");

        UserLoginRequestDto dto = new UserLoginRequestDto(user.getUsername(), user.getPassword());

        String token = userService.verify(dto);

        assertThat(token).isNotBlank().isEqualTo("token");
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(user);
    }

    @Test
    void verify_shouldThrowBadCredentialsException_whenNotAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(authManager.authenticate(any())).thenReturn(auth);

        UserLoginRequestDto dto = new UserLoginRequestDto("john", "wrong-pass");

        assertThrows(BadCredentialsException.class, () -> userService.verify(dto));
    }

    @Test
    void getCurrentUser_shouldReturnCurrentUser() {
        setupSecurityContext();
        assertThat(userService.getCurrentUser()).isEqualTo(userResponseDto);
    }

    @Test
    void updateUser_whenLastNameAndFirstNameAreEqualToOldValuesSimultaneously_thenThrowUserUpdateOldEqualsNewDataExceptionWith_SAME_LAST_NAME_AND_FIRST_NAME_Attribute() {
        setupSecurityContext();

        UserUpdateRequestDto requestDto = new UserUpdateRequestDto(user.getFirstName(), user.getLastName());

        Exception exception = assertThrows(UserUpdateOldEqualsNewDataException.class, () -> userService.updateUser(requestDto));
        assertThat(exception.getMessage()).contains(SAME_FIRST_NAME_AND_LAST_NAME.getDescription());
    }

    @Test
    void updateUser_whenLastNameAndFirstNameAreNotEqualToOldValuesSimultaneously_thenUpdateUser() {
        setupSecurityContext();
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto(user.getFirstName() + "string", user.getLastName());

        UserResponseDto userResponseDto = userService.updateUser(requestDto);

        assertThat(userResponseDto.getFirstName()).isEqualTo(requestDto.getFirstName());
        assertThat(userResponseDto.getLastName()).isEqualTo(requestDto.getLastName());
    }

    @Test
    void updatePassword_whenBadCredentials_thenThrowBadCredentialsException() {
        whenBadCredentialsGiven();
        UserPasswordUpdateRequestDto dto = new UserPasswordUpdateRequestDto("wrong-old-pass", "new-pass", "new-pass");

        assertThrows(BadCredentialsException.class, () -> userService.updatePassword(dto));
    }


    @Test
    void updatePassword_whenGoodCredentials_thenSaveUserReturnToken() {
        whenGoodCredentialsGiven();
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserPasswordUpdateRequestDto dto = new UserPasswordUpdateRequestDto("old-pass", "new-pass", "new-pass");

        assertThat(userService.updatePassword(dto)).isEqualTo("token");
        verify(userRepository).save(any(User.class));
    }


    @Test
    void updateUsername_whenBadCredentials_thenThrowBadCredentialsException() {
        whenBadCredentialsGiven();
        UserUsernameUpdateRequestDto dto = new UserUsernameUpdateRequestDto("new-username", "wrong-pass");

        assertThrows(BadCredentialsException.class, () -> userService.updateUsername(dto));
    }

    @Test
    void updateUsername_whenGoodCredentialsAndUsernameAlreadyExistsAndNewUsernameNotEqualsOldUsername_thenThrowUserUsernameAlreadyExistsException() {
        whenGoodCredentialsGiven();
        UserUsernameUpdateRequestDto dto = new UserUsernameUpdateRequestDto(user.getUsername() + "new-username", "pass");
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(UserUsernameAlreadyExistsException.class, () -> userService.updateUsername(dto));
    }

    @Test
    void updateUsername_whenGoodCredentialsAndUsernameDoesNotExistAndNewUsernameEqualsOldUsername_thenThrowUserUpdateOldEqualsNewDataExceptionWith_SAME_USERNAME_Attribute() {
        setupSecurityContext();
        UserUsernameUpdateRequestDto dto = new UserUsernameUpdateRequestDto(user.getUsername(), "pass");

        Exception exception = assertThrows(UserUpdateOldEqualsNewDataException.class, () -> userService.updateUsername(dto));
        assertThat(exception.getMessage()).contains(SAME_USERNAME.getDescription());
    }

    @Test
    void updateUsername_whenGoodCredentialsAndUsernameDoesNotExistAndNewUsernameNotEqualsOldUsername_thenSaveUserReturnToken() {
        whenGoodCredentialsGiven();
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserUsernameUpdateRequestDto dto = new UserUsernameUpdateRequestDto(user.getUsername() + "new-username", "pass");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        assertThat(userService.updateUsername(dto)).isEqualTo("token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updatePhone_whenBadCredentials_thenThrowBadCredentialsException() {
        whenBadCredentialsGiven();
        UserPhoneUpdateRequestDto dto = new UserPhoneUpdateRequestDto("new-phone", "wrong-pass");

        assertThrows(BadCredentialsException.class, () -> userService.updatePhone(dto));
    }

    @Test
    void updatePhone_whenGoodCredentialsAndPhoneAlreadyExistsAndNewPhoneNotEqualsOldPhone_thenThrowUserPhoneAlreadyExistsException() {
        whenGoodCredentialsGiven();
        UserPhoneUpdateRequestDto dto = new UserPhoneUpdateRequestDto(user.getPhone() + "new-phone", "pass");
        when(userRepository.existsByPhone(anyString())).thenReturn(true);

        assertThrows(UserPhoneAlreadyExistsException.class, () -> userService.updatePhone(dto));
    }

    @Test
    void updatePhone_whenGoodCredentialsAndPhoneDoesNotExistAndNewPhoneEqualsOldPhone_thenThrowUserUpdateOldEqualsNewDataExceptionWith_SAME_PHONE_Attribute() {
        setupSecurityContext();
        UserPhoneUpdateRequestDto dto = new UserPhoneUpdateRequestDto(user.getPhone(), "pass");

        Exception exception = assertThrows(UserUpdateOldEqualsNewDataException.class, () -> userService.updatePhone(dto));
        assertThat(exception.getMessage()).contains(SAME_PHONE.getDescription());
    }

    @Test
    void updatePhone_whenGoodCredentialsAndPhoneDoesNotExistAndNewPhoneNotEqualsOldPhone_thenSaveUserReturnToken() {
        whenGoodCredentialsGiven();
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserPhoneUpdateRequestDto dto = new UserPhoneUpdateRequestDto(user.getPhone() + "new-phone", "pass");
        when(userRepository.existsByPhone(anyString())).thenReturn(false);

        assertThat(userService.updatePhone(dto)).isEqualTo("token");
        verify(userRepository).save(any(User.class));
    }


    @Test
    void deleteUser_whenBadCredentials_thenThrowBadCredentialsException() {
        whenBadCredentialsGiven();
        UserLoginRequestDto dto = new UserLoginRequestDto(user.getUsername(), user.getPassword() + "wrong-pass");

        assertThrows(BadCredentialsException.class, () -> userService.deleteUser(dto));
    }

    @Test
    void deleteUser_whenUsernameNotEqualsUserPrincipalUsername_thenThrowBadCredentialsException() {
        setupSecurityContext();
        UserLoginRequestDto dto = new UserLoginRequestDto(user.getUsername() + "wrong-username", user.getPassword());

        assertThrows(BadCredentialsException.class, () -> userService.deleteUser(dto));
    }

    @Test
    void deleteUser_whenGoodCredentials_thenDeleteUser() {
        whenGoodCredentialsGiven();
        UserLoginRequestDto dto = new UserLoginRequestDto(user.getUsername(), user.getPassword());
        userService.deleteUser(dto);
        verify(userRepository).delete(user);
    }

    private void whenBadCredentialsGiven() {
        UserPrincipal principal = new UserPrincipal(user);
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);

        when(auth.getPrincipal()).thenReturn(principal);
        when(context.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(false);
        when(authManager.authenticate(any())).thenReturn(auth);
    }

    private void whenGoodCredentialsGiven() {
        UserPrincipal principal = new UserPrincipal(user);
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);

        when(auth.getPrincipal()).thenReturn(principal);
        when(context.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");
    }

    private void setupSecurityContext() {
        UserPrincipal principal = new UserPrincipal(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }
}