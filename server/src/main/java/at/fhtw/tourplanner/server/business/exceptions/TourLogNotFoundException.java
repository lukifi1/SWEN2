package at.fhtw.tourplanner.server.business.exceptions;

public class TourLogNotFoundException extends BusinessException {

    public TourLogNotFoundException(Long id) {
        super("Tour log with id " + id + " was not found.");
    }
}
