package at.fhtw.tourplanner.server.dto;

public record AuthResponse(
        String token,
        String username
) {
}
