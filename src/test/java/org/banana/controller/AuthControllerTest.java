package org.banana.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.banana.config.SecurityConfig;
import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.dto.UserRegisterRequestDto;
import org.banana.security.exception.UserPhoneAlreadyExistsException;
import org.banana.security.exception.UserUsernameAlreadyExistsException;
import org.banana.security.service.JwtService;
import org.banana.security.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.banana.dto.ValidationConstants.PHONE_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Banana on 12.05.2025
 */
@WebMvcTest(controllers = AuthControllerImpl.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

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

    @Test
    public void login_whenValidRequest_thenStatus200AndReturnToken() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("john.doe@example.com", "password123");
        String jwt = "jwt_token";
        when(userService.verify(requestDto)).thenReturn(jwt);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(result -> assertEquals(jwt, result.getResponse().getContentAsString()));
    }

    @Test
    public void login_whenEmptyUsername_thenStatus400() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("", "password123");

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_whenEmptyPassword_thenStatus400() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("john.doe@example.com", "");

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_whenInvalidEmail_thenShouldThrowBadCredentialsException() throws Exception {
        UserLoginRequestDto requestDto = new UserLoginRequestDto("example@example.com", "password123");

        when(userService.verify(requestDto))
                .thenThrow(new BadCredentialsException("message"));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void register_whenValidRequest_thenStatus200AndReturnToken() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("John", "Doe", "1234567890", "john.doe@example.com", "password123", "password123");
        String jwt = "jwt_token";
        when(userService.register(requestDto)).thenReturn(jwt);

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(result -> assertEquals(jwt, result.getResponse().getContentAsString()));
    }

    @Test
    public void register_whenUsernameAlreadyExists_thenStatus400() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("John", "Doe", "1234567890", "john.doe@example.com", "password123", "password123");
        when(userService.register(requestDto)).thenThrow(new UserUsernameAlreadyExistsException("john.doe@example.com"));

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void register_whenPhoneAlreadyExists_thenStatus400() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("John", "Doe", "1234567890", "john.doe@example.com", "password123", "password123");
        when(userService.register(requestDto)).thenThrow(new UserPhoneAlreadyExistsException(requestDto.getPhone()));

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void register_whenInvalidUsername_thenStatus400() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("John", "Doe", "1234567890", "invalid-email", "password123", "password123");

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("username")));
    }

    @Test
    public void register_whenPasswordLengthInvalid_thenStatus400() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("John", "Doe", "1234567890", "john.doe@example.com", "1", "1");
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("password")));
    }

    @Test
    public void register_whenInvalidPhone_thenStatus400() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("John", "Doe", "invalid-phone", "john.doe@example.com", "password123", "password123");

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains(PHONE_ERROR_MESSAGE)));
    }

    @Test
    public void register_whenPasswordAndMatchingPasswordNotEquals_thenStatus400() throws Exception {
        UserRegisterRequestDto requestDto = new UserRegisterRequestDto("John", "Doe", "1234567890", "john.doe@example.com", "password123", "password456");

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("Password and matching password must be equal")));
    }
}
