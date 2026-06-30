package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.InvalidCredentialsException;
import at.fhtw.tourplanner.server.business.exceptions.UserAlreadyExistsException;
import at.fhtw.tourplanner.server.business.security.JwtService;
import at.fhtw.tourplanner.server.dal.UserRepository;
import at.fhtw.tourplanner.server.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Protects the registration and login rules in {@link AuthService}. Critically:
 * passwords must always be stored as a hash (never plain text), duplicate usernames
 * must be rejected with {@link UserAlreadyExistsException}, and login must fail with
 * {@link InvalidCredentialsException} both for unknown users and wrong passwords -
 * without leaking which of the two was wrong.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerEncodesPasswordSavesUserAndReturnsToken() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(jwtService.generateToken("alice")).thenReturn("jwt-token");

        String token = authService.register("alice", "secret123");

        assertThat(token).isEqualTo("jwt-token");
        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getUsername()).isEqualTo("alice");
        assertThat(savedUser.getValue().getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register("alice", "secret123"))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        User user = User.builder().username("alice").passwordHash("hashed").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateToken("alice")).thenReturn("jwt-token");

        assertThat(authService.login("alice", "secret123")).isEqualTo("jwt-token");
    }

    @Test
    void loginRejectsUnknownUser() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("ghost", "secret123"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = User.builder().username("alice").passwordHash("hashed").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("alice", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }
}
