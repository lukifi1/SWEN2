package at.fhtw.tourplanner.server.common;

import at.fhtw.tourplanner.server.business.exceptions.*;
import at.fhtw.tourplanner.server.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Translates business-layer exceptions into HTTP responses. This is the only
 * place that maps our own exception types onto status codes, keeping that
 * concern out of the controllers and services.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({TourNotFoundException.class, TourLogNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(BusinessException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExists(UserAlreadyExistsException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
    }

    @ExceptionHandler(ResourceAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(ResourceAccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());
    }

    @ExceptionHandler(RouteCalculationException.class)
    public ResponseEntity<ErrorResponse> handleRouteCalculation(RouteCalculationException ex) {
        log.error("Route calculation failed: {}", ex.getMessage());
        return build(HttpStatus.BAD_GATEWAY, "Route calculation failed", ex.getMessage());
    }

    @ExceptionHandler(ImportExportException.class)
    public ResponseEntity<ErrorResponse> handleImportExport(ImportExportException ex) {
        log.warn("Import/export error: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Import/export failed", ex.getMessage());
    }

    @ExceptionHandler({ImageStorageException.class, PersistenceFailedException.class})
    public ResponseEntity<ErrorResponse> handleInternalBusiness(BusinessException ex) {
        log.error("Internal business error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        log.warn("Validation failed: {}", fieldErrors);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                "Please check your input values.",
                fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Something went wrong.");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), error, message));
    }
}
