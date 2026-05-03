package at.fhtw.tourplanner.server.common;

import at.fhtw.tourplanner.server.business.exceptions.TourNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TourNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleTourNotFound(TourNotFoundException exception) {
        log.warn(exception.getMessage());

        return Map.of(
                "error", "Tour not found",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationError(MethodArgumentNotValidException exception) {
        log.warn("Validation error: {}", exception.getMessage());

        return Map.of(
                "error", "Validation failed",
                "message", "Please check your input values."
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGeneralError(Exception exception) {
        log.error("Unexpected error occurred", exception);

        return Map.of(
                "error", "Internal server error",
                "message", "Something went wrong."
        );
    }
}
