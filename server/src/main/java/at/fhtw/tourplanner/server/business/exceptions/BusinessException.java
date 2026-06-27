package at.fhtw.tourplanner.server.business.exceptions;

/**
 * Base type for all exceptions raised by the business layer.
 * Keeps the business layer free of framework / persistence specific exceptions
 * so that callers only ever deal with our own exception types.
 */
public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String message) {
        super(message);
    }

    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
