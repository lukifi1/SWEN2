package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.ResourceAccessDeniedException;
import at.fhtw.tourplanner.server.dal.UserRepository;
import at.fhtw.tourplanner.server.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Resolves the {@link User} entity for the username carried by the validated
 * JWT. Centralizes the "authenticated user must exist" rule.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User require(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceAccessDeniedException(
                        "Authenticated user '" + username + "' no longer exists."));
    }
}
