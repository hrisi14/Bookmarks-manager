package bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.BookmarksManagerAPI;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.user.User;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UsersStorage {
    //keeps registered users
    private static final int MIN_PASSWORD_LENGTH = 5;
    private static final String PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$";

    private final String fileName;
    private final Map<String, User> users;

    public UsersStorage(String fileName) {
        this.users = new HashMap<>();
        this.fileName = fileName;
        try {
            initializeUsersDatabase(fileName);
        } catch (IllegalStateException e) {
            ExceptionsLogger.logClientException(e);
        }
    }

    public UsersStorage(Map<String, User> users, String fileName) {
        this.users = users;
        this.fileName = fileName;
        try {
            initializeUsersDatabase(fileName);
        } catch (IllegalStateException e) {
            ExceptionsLogger.logClientException(e);
        }
    }


    public String register(String username, String password) {
        if (username == null || username.isEmpty() ||
                username.isBlank()) {
            ExceptionsLogger.logClientException(new IllegalArgumentException(String.
                    format("Invalid username %s or password %s", username, password)));
            return "Username/password " +
                    "must not be null or blank!";
        }
        if (!validatePassword(password)) {
            ExceptionsLogger.logClientException(new IllegalArgumentException(String.
                    format("Invalid username %s or password %s", username, password)));
            return "Password must consist of at least five characters and " +
                    "contain at least one capital letter, " +
                    "one small letter and one digit.";
        }

        if (isARegisteredUser(username)) {
            throw new UserAlreadyExistsException(String.format("User with" +
                    "name %s already exists!", username));
        }
        User registeredUser = new User(username, password,
                new BookmarksGroupStorage(BookmarksManagerAPI.GROUP_FILE_PATH + username));
        users.put(username, registeredUser);
        return String.format("User %s has been successfully registered.", username);
    }

    public boolean isARegisteredUser(String username) {
       return users.containsKey(username);
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void updateUser(String username, User user) {
        users.replace(username, user);
    }

    public void saveUsers() {
        Path filePath = Paths.get(fileName);
        if (!Files.exists(filePath)) {
            FileCreator.createFile(fileName);
        }

        try (var objectOutputStream = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            for (User u : users.values()) {
                objectOutputStream.writeObject(u);
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save users to database!", e);
        }
    }

    public void initializeUsersDatabase(String fileName) {
        Path usersFilePath = Paths.get(fileName);
        if (!Files.exists(usersFilePath)) {
            FileCreator.createFile(fileName);
        } else {
            readUsers();
        }
    }

    public void readUsers() {
        Path filePath = Paths.get(fileName);

        try (var objectInputStream = new ObjectInputStream(Files.newInputStream(filePath))) {
            while (true) {
                try {
                    User currentUser = (User) objectInputStream.readObject();
                    users.put(currentUser.getUsername(), currentUser);
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Could not read users from database!", e);
        }
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof BookmarksGroupStorage)) {
            return false;
        }
        UsersStorage storage = (UsersStorage) obj;

        return fileName.equals(storage.getFileName()) &&
                users.entrySet().containsAll(storage.
                        getUsers().entrySet()) &&
                storage.getUsers().entrySet().containsAll(users.entrySet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }

    private boolean validatePassword(String password) {
        return password != null && !password.isBlank() &&
                password.length() >= MIN_PASSWORD_LENGTH
                && password.matches(PASSWORD_REGEX);
    }
}
