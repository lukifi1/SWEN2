package at.fhtw.tourplanner.server.business.exceptions;

/**
 * Raised when a user tries to access or modify a resource (tour / tour log)
 * that belongs to another user.
 */
public class ResourceAccessDeniedException extends BusinessException {

    public ResourceAccessDeniedException(String message) {
        super(message);
    }
}
