package at.fhtw.tourplanner.server.business.exceptions;

public class ImageStorageException extends BusinessException {

    public ImageStorageException(String message) {
        super(message);
    }

    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
