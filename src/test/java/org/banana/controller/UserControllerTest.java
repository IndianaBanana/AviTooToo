package org.banana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.banana.config.SecurityConfig;
import org.banana.dto.user.UserResponseDto;
import org.banana.dto.user.UserUpdateRequestDto;
import org.banana.exception.UserNotFoundException;
import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserPasswordUpdateRequestDto;
import org.banana.security.dto.UserPhoneUpdateRequestDto;
import org.banana.security.dto.UserUsernameUpdateRequestDto;
import org.banana.security.exception.UserPhoneAlreadyExistsException;
import org.banana.security.exception.UserUpdateOldEqualsNewDataException;
import org.banana.security.exception.UserUsernameAlreadyExistsException;
import org.banana.security.service.JwtService;
import org.banana.security.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserControllerImpl.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // --- findById ---
    @Test
    @WithMockUser
    void findById_whenExists_thenOk() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseDto dto = new UserResponseDto(id, "First", "Last", "123", "user");
        when(userService.findById(id)).thenReturn(dto);

        mvc.perform(get("/api/v1/user/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    @Test
    @WithMockUser
    void findById_whenNotFound_thenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.findById(id)).thenThrow(new UserNotFoundException(id));

        mvc.perform(get("/api/v1/user/{id}", id))
                .andExpect(status().isNotFound());
    }

    // --- getCurrentUser ---
    @Test
    @WithMockUser
    void getCurrentUser_thenOk() throws Exception {
        UserResponseDto dto = new UserResponseDto(UUID.randomUUID(), "F", "L", "123", "u");
        when(userService.getCurrentUser()).thenReturn(dto);

        mvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    // --- updateUser ---
    @Test
    @WithMockUser
    void updateUser_whenValid_thenOk() throws Exception {
        UserUpdateRequestDto req = new UserUpdateRequestDto("N1", "N2");
        UserResponseDto dto = new UserResponseDto(UUID.randomUUID(), "N1", "N2", "123", "u");
        when(userService.updateUser(req)).thenReturn(dto);

        mvc.perform(patch("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }

    @Test
    @WithMockUser
    void updateUser_whenSameName_thenConflict() throws Exception {
        UserUpdateRequestDto req = new UserUpdateRequestDto("F", "L");
        doThrow(new UserUpdateOldEqualsNewDataException(UserUpdateOldEqualsNewDataException.UserUpdateExceptionMessage.SAME_FIRST_NAME_AND_LAST_NAME))
                .when(userService).updateUser(req);

        mvc.perform(patch("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithAnonymousUser
    void updateUser_whenUnauthorized_thenUnauthorized() throws Exception {
        UserUpdateRequestDto req = new UserUpdateRequestDto("N1", "N2");

        mvc.perform(patch("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }


    // --- updatePassword ---

    @Test
    @WithAnonymousUser
    void updatePassword_whenUnauthorized_thenUnauthorized() throws Exception {
        UserPasswordUpdateRequestDto req = new UserPasswordUpdateRequestDto("old", "new", "new");

        mvc.perform(patch("/api/v1/user/security/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void updatePassword_whenValid_thenOk() throws Exception {
        UserPasswordUpdateRequestDto req = new UserPasswordUpdateRequestDto("old", "new123", "new123");
        when(userService.updatePassword(req)).thenReturn("token");

        mvc.perform(patch("/api/v1/user/security/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("token")));
    }

    @Test
    @WithMockUser
    void updatePassword_whenMismatch_thenBadRequest() throws Exception {
        UserPasswordUpdateRequestDto req = new UserPasswordUpdateRequestDto("old", "123123123", "4444аааа444");

        mvc.perform(patch("/api/v1/user/security/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"errors\":{\"matchingNewPassword\":\"New password and matching password must be equal\"}")));
    }


    @Test
    @WithMockUser
    void updatePassword_whenOldEqualsNew_thenBadRequest() throws Exception {
        String oldPassword = "oldpass1234";
        UserPasswordUpdateRequestDto req = new UserPasswordUpdateRequestDto(oldPassword, oldPassword, oldPassword);

        mvc.perform(patch("/api/v1/user/security/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("newPassword")));
    }

    @Test
    @WithMockUser
    void updatePassword_whenNewPasswordLengthInvalid_thenBadRequest() throws Exception {
        UserPasswordUpdateRequestDto req = new UserPasswordUpdateRequestDto("old", "a", "a");

        mvc.perform(patch("/api/v1/user/security/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("size must be between")));
    }

    // --- updateUsername ---
    @Test
    @WithMockUser
    void updateUsername_whenValid_thenOk() throws Exception {
        UserUsernameUpdateRequestDto req = new UserUsernameUpdateRequestDto("u@u.com", "pass");
        when(userService.updateUsername(req)).thenReturn("token2");

        mvc.perform(patch("/api/v1/user/security/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("token2")));
    }

    @Test
    @WithMockUser
    void updateUsername_whenExists_thenConflict() throws Exception {
        UserUsernameUpdateRequestDto req = new UserUsernameUpdateRequestDto("u@u.com", "pass");
        doThrow(new UserUsernameAlreadyExistsException("u")).when(userService).updateUsername(req);

        mvc.perform(patch("/api/v1/user/security/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void updateUsername_whenInvalid_thenBadRequest() throws Exception {
        UserUsernameUpdateRequestDto req = new UserUsernameUpdateRequestDto("u", "pass");

        mvc.perform(patch("/api/v1/user/security/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("must be a well-formed email address")));
    }


    // --- updatePhone ---
    @Test
    @WithMockUser
    void updatePhone_whenValid_thenOk() throws Exception {
        UserPhoneUpdateRequestDto req = new UserPhoneUpdateRequestDto("+1234567890", "pass");
        when(userService.updatePhone(req)).thenReturn("token3");

        mvc.perform(patch("/api/v1/user/security/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("token3")));
    }

    @Test
    @WithMockUser
    void updatePhone_whenInvalidFormat_thenBadRequest() throws Exception {
        UserPhoneUpdateRequestDto req = new UserPhoneUpdateRequestDto("abc", "pass");

        mvc.perform(patch("/api/v1/user/security/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updatePhone_whenPhoneExists_thenConflict() throws Exception {
        UserPhoneUpdateRequestDto req = new UserPhoneUpdateRequestDto("+1234567890", "pass");
        doThrow(new UserPhoneAlreadyExistsException("+1234567890")).when(userService).updatePhone(req);

        mvc.perform(patch("/api/v1/user/security/phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    // --- deleteUser ---
    @Test
    @WithMockUser
    void deleteUser_whenValid_thenNoContent() throws Exception {
        UserLoginRequestDto req = new UserLoginRequestDto("user", "pass");

        mvc.perform(delete("/api/v1/user/security")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteUser_whenWrongCredentials_thenConflict() throws Exception {
        UserLoginRequestDto req = new UserLoginRequestDto("u", "p");
        doThrow(new AccessDeniedException("Only owner can delete account")).when(userService).deleteUser(req);

        mvc.perform(delete("/api/v1/user/security")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void deleteUser_whenAnonymous_thenUnauthorized() throws Exception {
        UserLoginRequestDto req = new UserLoginRequestDto("u", "p");

        mvc.perform(delete("/api/v1/user/security")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
