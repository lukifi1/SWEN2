package at.fhtw.tourplanner.server.business;

import at.fhtw.tourplanner.server.business.exceptions.InvalidCredentialsException;
import at.fhtw.tourplanner.server.business.exceptions.UserAlreadyExistsException;
import at.fhtw.tourplanner.server.business.security.JwtService;
import at.fhtw.tourplanner.server.dal.UserRepository;
import at.fhtw.tourplanner.server.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for user self-registration and credential based login.
 * Passwords are stored as BCrypt hashes; on success a signed JWT is returned.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public String register(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            log.warn("Registration rejected: username '{}' already exists", username);
            throw new UserAlreadyExistsException(username);
        }

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();
        userRepository.save(user);

        log.info("Registered new user '{}'", username);
        return jwtService.generateToken(username);
    }

    @Transactional(readOnly = true)
    public String login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("Failed login attempt for user '{}'", username);
            throw new InvalidCredentialsException();
        }

        log.info("User '{}' logged in", username);
        return jwtService.generateToken(username);
    }
}
