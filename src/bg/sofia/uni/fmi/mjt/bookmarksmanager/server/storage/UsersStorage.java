package bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.user.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UsersStorage {
    //keeps registered users
    private static final int MIN_PASSWORD_LENGTH = 5;
    private static final String PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$";
    private static final String REGISTERED_USERS_FILE = "src" + File.separator +
            "bg" + File.separator + "sofia" + File.separator +
            "uni" + File.separator + "fmi" + File.separator + "mjt" +
            File.separator + "bookmarksmanager" + File.separator + "server"
            + File.separator + "storage" + File.separator + "users" + File.separator + "registeredUsers";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, User> users;

    public UsersStorage() {
        this.users = new HashMap<>();
        try {
            initializeUsersDatabase();
        } catch (IllegalStateException e) {
            ExceptionsLogger.logClientException(e);
        }
    }

    public UsersStorage(Map<String, User> users) {
        this.users = users;
        try {
            initializeUsersDatabase();
        } catch (IllegalStateException e) {
            ExceptionsLogger.logClientException(e);
        }
    }


    public String register(String username, String password) {
        if (username == null || username.isEmpty() ||
                username.isBlank() || !validatePassword(password)) {
            ExceptionsLogger.logClientException(new IllegalArgumentException(String.
                    format("Invalid username %s or password %s", username, password)));
           return "Username/password are null or blank, or password does not match the template!";
        }
        if (isARegisteredUser(username)) {
            //to do: replace with logging
            throw new UserAlreadyExistsException(String.format("User with" +
                    "name %s already exists!", username));
        }
        User registeredUser = new User(username, password,
                new BookmarksGroupStorage(username));
        users.put(username, registeredUser);
        saveUser(username, password);
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

    private boolean validatePassword(String password) {
      return password != null && !password.isBlank() &&
              password.length() >= MIN_PASSWORD_LENGTH
              && password.matches(PASSWORD_REGEX);
    }

    private void saveUser(String username, String password) {
        Path filePath = Paths.get(REGISTERED_USERS_FILE);

        if (!Files.exists(filePath)) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REGISTERED_USERS_FILE, true))) {
                writer.write(GSON.toJson(username));
                writer.write(GSON.toJson(password));
                writer.newLine();
        } catch (IOException e) {
            ExceptionsLogger.logClientException(e);
        }
    }

    private void initializeUsersDatabase() {
        Path usersFilePath = Paths.get(REGISTERED_USERS_FILE);
        if (!Files.exists(usersFilePath)) {
            FileCreator.createFile(REGISTERED_USERS_FILE);
        } else {
            readUsers();
        }
    }

    private void readUsers() {
        Path filePath = Paths.get(REGISTERED_USERS_FILE);

        try (var objectInputStream = new ObjectInputStream(Files.newInputStream(filePath))) {

            Object userObject;
            while ((userObject = objectInputStream.readObject()) != null) {
                User s = (User) userObject;
                users.put(s.getUsername(), s);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Could not read users from database!", e);
        }
    }
}
