package bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions;

public class UserNotLoggedInException extends RuntimeException {
    public UserNotLoggedInException(String message) {
        super(message);
    }
}
