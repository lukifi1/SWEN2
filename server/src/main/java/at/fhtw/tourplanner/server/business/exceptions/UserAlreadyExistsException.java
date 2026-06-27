package at.fhtw.tourplanner.server.business.exceptions;

public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException(String username) {
        super("Username '" + username + "' is already taken.");
    }
}
