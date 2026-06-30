package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.ResourceAccessDeniedException;
import at.fhtw.tourplanner.server.dal.UserRepository;
import at.fhtw.tourplanner.server.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Protects the invariant in {@link CurrentUserService}: a JWT that passed
 * signature validation but whose username no longer exists in the database
 * must be refused with {@link ResourceAccessDeniedException} rather than
 * returning null or propagating a low-level NoSuchElementException.
 */
@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @Test
    void requireReturnsUserWhenFound() {
        User user = User.builder().username("alice").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        User result = currentUserService.require("alice");

        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void requireThrowsWhenUserNoLongerExists() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUserService.require("ghost"))
                .isInstanceOf(ResourceAccessDeniedException.class)
                .hasMessageContaining("ghost");
    }
}
