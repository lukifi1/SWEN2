package at.fhtw.tourplanner.server.business.exceptions;

public class ImportExportException extends BusinessException {

    public ImportExportException(String message) {
        super(message);
    }

    public ImportExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
