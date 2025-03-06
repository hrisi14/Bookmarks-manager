package bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions;

public class BookmarkAlreadyExistsException extends RuntimeException {
    public BookmarkAlreadyExistsException(String message) {
        super(message);
    }
}
