package org.banana.security.service;

import org.banana.entity.User;
import org.banana.repository.UserRepository;
import org.banana.security.UserRole;
import org.banana.security.dto.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    private final UUID userId = UUID.randomUUID();
    private final User testUser = new User(userId, "John", "Doe", "phone", "username", "hashed_password", UserRole.ROLE_USER);
    private final UserPrincipal testPrincipal = new UserPrincipal(userId, "John", "Doe", "phone", "username", "hashed_password", UserRole.ROLE_USER);

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_whenUserExists_thenReturnsUserPrincipal() {
        Mockito.when(userRepository.findByUsername("johndoe"))
                .thenReturn(Optional.of(testUser));

        UserDetails result = userDetailsService.loadUserByUsername("johndoe");

        assertThat(result).isInstanceOf(UserPrincipal.class);
        assertThat(result).isEqualTo(testPrincipal);
    }

    @Test
    void loadUserByUsername_whenUserNotFound_thenShouldThrowUsernameNotFoundException() {
        Mockito.when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid password or login");
    }
}
