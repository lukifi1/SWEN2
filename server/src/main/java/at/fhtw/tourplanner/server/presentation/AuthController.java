package at.fhtw.tourplanner.server.presentation;

import at.fhtw.tourplanner.server.business.AuthService;
import at.fhtw.tourplanner.server.dto.AuthResponse;
import at.fhtw.tourplanner.server.dto.LoginRequest;
import at.fhtw.tourplanner.server.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request.username(), request.password());
        return new AuthResponse(token, request.username());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.username(), request.password());
        return new AuthResponse(token, request.username());
    }
}
