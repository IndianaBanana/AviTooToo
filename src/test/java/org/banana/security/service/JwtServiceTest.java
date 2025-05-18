package org.banana.security.service;

import io.jsonwebtoken.ExpiredJwtException;
import org.banana.security.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString("testtesttesttesttesttesttesttest".getBytes());
    private final UUID userId = UUID.randomUUID();
    private final String username = "johndoe";
    private final String phone = "+123456789";
    private final UserRole role = UserRole.ROLE_USER;
    private JwtService jwtService;
    private String token;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 86_400_000);
        token = jwtService.generateToken(userId, username, role, phone);
    }

    @Test
    void generateToken_whenInvoked_thenShouldIncludeAllClaims() {
        assertThat(token).isNotNull();

        assertThat(jwtService.extractUserId(token)).isEqualTo(userId.toString());
        assertThat(jwtService.extractUsername(token)).isEqualTo(username);
        assertThat(jwtService.extractRole(token)).isEqualTo(role.name());
        assertThat(jwtService.extractPhone(token)).isEqualTo(phone);
        assertThat(jwtService.extractJwtId(token)).isNotBlank();
    }

    @Test
    void validateToken_whenValidTokenAndMatchingUsername_thenShouldReturnTrue() {
        var userDetails = new User(username, "password", new java.util.ArrayList<>());

        boolean isValid = jwtService.validateToken(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_whenUsernameDoesNotMatch_thenShouldReturnFalse() {
        var otherUser = new User("someoneElse", "pass", new java.util.ArrayList<>());

        boolean isValid = jwtService.validateToken(token, otherUser);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_whenTokenIsFresh_thenShouldReturnFalse() {
        assertThat(jwtService.validateToken(token, new User(username, "", new java.util.ArrayList<>()))).isTrue();
    }

    @Test
    void validateToken_whenTokenIsExpired_thenShouldThrowExpiredJwtException() {
        jwtService = new JwtService(SECRET, -1000);
        token = jwtService.generateToken(userId, username, role, phone);
        assertThatThrownBy(() -> jwtService.validateToken(token, new User(username, "", new java.util.ArrayList<>())))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractClaims_whenInvoked_thenShouldReturnCorrectValues() {
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId.toString());
        assertThat(jwtService.extractUsername(token)).isEqualTo(username);
        assertThat(jwtService.extractPhone(token)).isEqualTo(phone);
        assertThat(jwtService.extractRole(token)).isEqualTo(role.name());
        assertThat(jwtService.extractJwtId(token)).isNotNull();
    }
}
