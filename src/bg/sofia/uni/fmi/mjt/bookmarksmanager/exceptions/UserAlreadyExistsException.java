package bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
