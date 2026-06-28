package at.fhtw.tourplanner.server.dto;

import java.util.Map;

/**
 * Uniform error body returned for every handled exception.
 * {@code fieldErrors} is only populated for validation failures.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, null);
    }
}
