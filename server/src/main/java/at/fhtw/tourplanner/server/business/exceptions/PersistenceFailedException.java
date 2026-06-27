package at.fhtw.tourplanner.server.business.exceptions;

/**
 * Wraps a low-level data-access failure (e.g. a Spring {@code DataAccessException})
 * so the persistence framework's exception types never leak past the business layer.
 */
public class PersistenceFailedException extends BusinessException {

    public PersistenceFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
