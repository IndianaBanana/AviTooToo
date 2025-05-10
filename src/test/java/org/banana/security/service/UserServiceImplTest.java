package org.banana.security.service;

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
import org.banana.security.exception.UserPhoneAlreadyExistsException;
import org.banana.security.exception.UserUpdateOldEqualsNewDataException;
import org.banana.security.exception.UserUsernameAlreadyExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
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

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setup() {
        user = new User(USER_ID, "John", "Doe", "123", "com@com.com", "password", UserRole.ROLE_USER);
        userResponseDto = new UserResponseDto(USER_ID, "John", "Doe", "123", "com@com.com", null, null);
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getFirstName(), user.getLastName(), user.getPhone(), user.getUsername(), user.getPassword(), user.getRole());
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
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
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);

        UserRegisterRequestDto requestDto = new UserRegisterRequestDto(
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getUsername(),
                user.getPassword(),
                user.getPassword()
        );

        when(userMapper.userRegisterRequestDtoToUser(any())).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(user);
        when(jwtService.generateToken(USER_ID, requestDto.getUsername(), UserRole.ROLE_USER, requestDto.getPhone())).thenReturn("token");

        String result = userService.register(requestDto);

        assertThat(result).isEqualTo("token");
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedPass");
        assertThat(userCaptor.getValue().getRole()).isNotNull().isEqualTo(UserRole.ROLE_USER);
        verify(userRepository).save(userCaptor.getValue());
    }

    @Test
    void verify_shouldAuthenticateAndReturnToken() {
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getFirstName(), user.getLastName(), user.getPhone(), user.getUsername(), user.getPassword(), user.getRole());
        Authentication auth = mock(Authentication.class);

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principal);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        UserLoginRequestDto dto = new UserLoginRequestDto(user.getUsername(), user.getPassword());

        userService.verify(dto);
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(principal.getId(), principal.getUsername(), principal.getRole(), principal.getPhone());
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
    void getCurrentUser_ReturnCurrentUser() {
        when(userRepository.findFetchedById(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.userToUserResponseDto(any(User.class))).thenReturn(userResponseDto);
        assertThat(userService.getCurrentUser()).isEqualTo(userResponseDto);
        verify(userRepository).findFetchedById(USER_ID);
    }


    @Test
    void findById_WhenUserWithGivenIdExists_ReturnUserResponseDto() {
        when(userRepository.findFetchedById(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.userToUserResponseDto(any(User.class))).thenReturn(userResponseDto);
        assertThat(userService.findById(USER_ID)).isEqualTo(userResponseDto);
        verify(userRepository).findFetchedById(USER_ID);
    }

    @Test
    void findById_WhenUserWithGivenIdDoesNotExist_ThrowUserNotFoundException() {
        when(userRepository.findFetchedById(USER_ID)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findById(USER_ID));
    }

    @Test
    void updateUser_whenLastNameAndFirstNameAreEqualToOldValuesSimultaneously_thenThrowUserUpdateOldEqualsNewDataExceptionWith_SAME_LAST_NAME_AND_FIRST_NAME_Attribute() {
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto(user.getFirstName(), user.getLastName());

        Exception exception = assertThrows(UserUpdateOldEqualsNewDataException.class, () -> userService.updateUser(requestDto));
        assertThat(exception.getMessage()).contains(SAME_FIRST_NAME_AND_LAST_NAME.getDescription());
    }

    @Test
    void updateUser_whenLastNameAndFirstNameAreNotEqualToOldValuesSimultaneously_thenUpdateUser() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto("newFN", "newLN");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findFetchedById(USER_ID)).thenReturn(Optional.of(user));
        userService.updateUser(requestDto);

        verify(userRepository).save(captor.capture());
        verify(userMapper).userToUserResponseDto(captor.getValue());
        assertThat(captor.getValue().getFirstName()).isEqualTo(requestDto.getFirstName());
        assertThat(captor.getValue().getLastName()).isEqualTo(requestDto.getLastName());
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
        UserPasswordUpdateRequestDto dto = new UserPasswordUpdateRequestDto("old-pass", "new-pass", "new-pass");
        when(jwtService.generateToken(USER_ID, user.getUsername(), user.getRole(), user.getPhone())).thenReturn("token");
        when(passwordEncoder.encode(dto.getNewPassword())).thenReturn("new-pass-encoded");
        assertThat(userService.updatePassword(dto)).isEqualTo("token");
        verify(userRepository).updatePassword(USER_ID, "new-pass-encoded");
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
        UserUsernameUpdateRequestDto dto = new UserUsernameUpdateRequestDto(user.getUsername(), "pass");

        Exception exception = assertThrows(UserUpdateOldEqualsNewDataException.class, () -> userService.updateUsername(dto));
        assertThat(exception.getMessage()).contains(SAME_USERNAME.getDescription());
    }

    @Test
    void updateUsername_whenGoodCredentialsAndUsernameDoesNotExistAndNewUsernameNotEqualsOldUsername_thenSaveUserReturnToken() {
        whenGoodCredentialsGiven();
        UserUsernameUpdateRequestDto dto = new UserUsernameUpdateRequestDto("new-username", "pass");
        when(userRepository.existsByUsername("new-username")).thenReturn(false);
        when(jwtService.generateToken(USER_ID, "new-username", user.getRole(), user.getPhone())).thenReturn("token");

        String token = userService.updateUsername(dto);

        assertThat(token).isEqualTo("token");

        verify(userRepository).updateUsername(USER_ID, "new-username");
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
        UserPhoneUpdateRequestDto dto = new UserPhoneUpdateRequestDto(user.getPhone(), "pass");

        Exception exception = assertThrows(UserUpdateOldEqualsNewDataException.class, () -> userService.updatePhone(dto));
        assertThat(exception.getMessage()).contains(SAME_PHONE.getDescription());
    }

    @Test
    void updatePhone_whenGoodCredentialsAndPhoneDoesNotExistAndNewPhoneNotEqualsOldPhone_thenSaveUserReturnToken() {
        whenGoodCredentialsGiven();
        UserPhoneUpdateRequestDto dto = new UserPhoneUpdateRequestDto("new-phone", "pass");
        when(userRepository.existsByPhone("new-phone")).thenReturn(false);
        when(jwtService.generateToken(USER_ID, user.getUsername(), user.getRole(), dto.getNewPhone())).thenReturn("token");

        assertThat(userService.updatePhone(dto)).isEqualTo("token");
        verify(userRepository).updatePhone(USER_ID, dto.getNewPhone());
    }



    @Test
    void deleteUser_whenBadCredentials_thenThrowBadCredentialsException() {
        whenBadCredentialsGiven();
        UserLoginRequestDto dto = new UserLoginRequestDto(user.getUsername(), user.getPassword() + "wrong-pass");

        assertThrows(BadCredentialsException.class, () -> userService.deleteUser(dto));
    }

    @Test
    void deleteUser_whenUsernameNotEqualsUserPrincipalUsername_thenThrowBadCredentialsException() {
        UserLoginRequestDto dto = new UserLoginRequestDto(user.getUsername() + "wrong-username", user.getPassword());

        assertThrows(AccessDeniedException.class, () -> userService.deleteUser(dto));
    }

    @Test
    void deleteUser_whenGoodCredentials_thenDeleteUser() {
        whenGoodCredentialsGiven();
        UserLoginRequestDto dto = new UserLoginRequestDto(user.getUsername(), user.getPassword());
        userService.deleteUser(dto);
        verify(userRepository).deleteById(USER_ID);
    }

    private void whenGoodCredentialsGiven() {
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getFirstName(), user.getLastName(), user.getPhone(), user.getUsername(), user.getPassword(), user.getRole());
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);

        when(auth.getPrincipal()).thenReturn(principal);
        when(context.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
    }
    private void whenBadCredentialsGiven() {
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getFirstName(), user.getLastName(), user.getPhone(), user.getUsername(), user.getPassword(), user.getRole());
        SecurityContextHolder.setContext(context);

        when(auth.getPrincipal()).thenReturn(principal);
        when(context.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(false);
        when(authManager.authenticate(any())).thenReturn(auth);
    }
}

