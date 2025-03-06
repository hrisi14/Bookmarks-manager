package bg.sofia.uni.fmi.mjt.bookmarksmanager.user;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.BookmarksManagerAPI;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.BookmarksGroupStorage;

public class User{

    private  String username;
    private  String password;
    private  final BookmarksGroupStorage storage;

    public User(String username, String password, BookmarksGroupStorage storage) {
        this.username = username;
        setPassword(password);
        this.storage = storage;
    }


    public void setPassword(String newPassword) {
        if (newPassword == null || newPassword.isBlank()
                || newPassword.isEmpty()) {
            throw new IllegalArgumentException("User password " +
                    "can not be null or empty!");
        }
        this.password = password;
    }

    public void setUsername(String username) {
        if (username == null || username.isBlank()
                || username.isEmpty()) {
            throw new IllegalArgumentException("Username " +
                    "can not be null or empty!");
        }
        this.username = username;
    }

    public boolean validatePassword(String passwordToCheck) {
        if (passwordToCheck == null || passwordToCheck.isBlank()
                || passwordToCheck.isEmpty()) {
            throw new IllegalArgumentException("User password " +
                    "can not be null or empty!");
        }
        return passwordToCheck.equals(password);
    }

    public String getUsername() {
        return username;
    }

    public BookmarksGroupStorage getStorage() {
        return storage;
    }
}
