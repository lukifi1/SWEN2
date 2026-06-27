package at.fhtw.tourplanner.server.business.exceptions;

public class TourNotFoundException extends BusinessException {

    public TourNotFoundException(Long id) {
        super("Tour with id " + id + " was not found.");
    }
}
