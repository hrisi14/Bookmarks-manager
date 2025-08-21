package bg.sofia.uni.fmi.mjt.bookmarksmanager;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.InvalidCredentialsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.NoSuchBookmarkException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.NoSuchGroupException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.NoSuchUserException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.UserNotLoggedInException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.finder.BookmarksFinder;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.UsersStorage;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.user.User;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Here I use the Facade design pattern to separate storage from command handling

public class BookmarksManager implements BookmarksManagerAPI {

    private static final String NOT_LOGGED_WARNING = "User must " +
            "have logged in before using the app's commands!";

    private static final String INVALID_COMMAND_PARAMS = "Invalid command's " +
            "parameters- group name or bookmark!";

    private static final String INEXISTENT_GROUP_BOOKMARK = "User does" +
            " not have such a group/bookmark!";

    private final Map<SocketChannel, User> loggedInUsers; //manages users' login sessions
    private final UsersStorage usersStorage;
    private final BookmarksFinder finder;


    public BookmarksManager() {
        this.loggedInUsers = new HashMap<>();
        this.usersStorage = new UsersStorage(REGISTERED_USERS_FILE);
        this.finder = new BookmarksFinder();
    }

    public BookmarksManager(Map<SocketChannel, User> loggedInUsers,
                            UsersStorage usersStorage, BookmarksFinder finder) {
        this.loggedInUsers = loggedInUsers;
        this.usersStorage = usersStorage;
        this.finder = finder;
    }

    //wrapper functions of the base ones- this is some kind of a declaration (interface)
    //of the basic apps functions

    @Override
    public String register(SocketChannel clientChannel, String username, String password) {
        if (username == null || username.isEmpty() ||
                username.isBlank()) {
            ExceptionsLogger.logClientException(new IllegalArgumentException(String.
                    format("Invalid username %s or password %s", username, password)));
            return "Username/password " +
                    "must not be null or blank!";
        }
        String result;
        try {
            result = usersStorage.register(username, password);
        } catch (UserAlreadyExistsException e) {
            ExceptionsLogger.logClientException(e);
            return String.format("User with username %s already exists!", username);
        }
        return result;
    }

    @Override
    public String login(SocketChannel clientChannel, String username, String password) {
        if (username == null || username.isEmpty() ||
                username.isBlank() || password == null || password.isEmpty() ||
                password.isBlank())  {
            ExceptionsLogger.logClientException( new
                    IllegalArgumentException("Username/password" +
                    " can not be null or blank!"));

            return "Username/password " +
                    "must not be null or blank!";
        }
        if (!usersStorage.isARegisteredUser(username)) {
            throw new NoSuchUserException("There is no" +
                    " registered user with username " + username);
        }

        if (loggedInUsers.containsKey(clientChannel)) {
            return String.format("User with username %s has already logged in!", username);
        }

        if (!usersStorage.getUsers().get(username).validatePassword(password)) {
            throw new InvalidCredentialsException("Incorrect password" +
                    " entered when logging!");
        }
         User loggedInUser = usersStorage.getUsers().get(username);
         loggedInUsers.put(clientChannel, loggedInUser);
         return String.format("User with name %s has successfully logged in.", username);
    }

    @Override
    public String createNewBookmarksGroup(SocketChannel clientChannel, String groupName) {
        if (clientChannel == null) {
            return "Communication problem occurred. " +
                    "Try to connect again.";
        }
        if (!hasUserLoggedIn(clientChannel)) {
            return NOT_LOGGED_WARNING;
        }
        if (groupName == null || groupName.isEmpty() ||
                groupName.isBlank()) {
           ExceptionsLogger.logClientException(new IllegalArgumentException("New " +
                   "group name can not be null!"));
            return INVALID_COMMAND_PARAMS + "group name: " + groupName;
        }
        try {
            User loggedInUser = loggedInUsers.get(clientChannel);
            loggedInUser.getStorage().createNewGroup(groupName);
            usersStorage.updateUser(loggedInUser.getUsername(), loggedInUser);
            return String.format("Successful creation of bookmarks " +
                    "group %s for user %s", groupName, loggedInUser.getUsername());
        } catch (GroupAlreadyExistsException e) {
            ExceptionsLogger.logClientException(e);
            return "Such a group already exists. Please, try with another name.";
        }
    }

    @Override
    public String addNewBookmarkToGroup(SocketChannel clientChannel, String groupName,
                                        String url, boolean isShortened) {
        if (!hasUserLoggedIn(clientChannel)) {
            return NOT_LOGGED_WARNING;
        }
        User loggedInUser = loggedInUsers.get(clientChannel);
        try {
            loggedInUser.getStorage().addNewBookmarkToGroup(Bookmark.of(url, groupName,
                    isShortened), groupName);
        } catch (NoSuchGroupException e) {
            ExceptionsLogger.logClientException(e);
            return INEXISTENT_GROUP_BOOKMARK;
        } catch (IllegalStateException e) {
            ExceptionsLogger.logClientException(e);
            return "Could not shorten link! Original url returned!";
        } catch (IllegalArgumentException e) {
            ExceptionsLogger.logClientException(e);
            return INVALID_COMMAND_PARAMS;
        }
        usersStorage.updateUser(loggedInUser.getUsername(), loggedInUser);
        invalidateFinder(loggedInUser.getUsername());
        return String.format("Successful add of bookmark %s " +
                "to group %s of user %s", url, groupName,
                loggedInUser.getUsername());
    }

    @Override
    public String removeBookmarkFromGroup(SocketChannel clientChannel, String groupName, String bookmarkTitle) {
        if (!hasUserLoggedIn(clientChannel)) {
            return NOT_LOGGED_WARNING;
        }
        User loggedInUser = loggedInUsers.get(clientChannel);
        try {
            loggedInUser.getStorage().removeBookmarkFromGroup(bookmarkTitle, groupName);
        } catch (NoSuchGroupException | NoSuchBookmarkException e)  {
            ExceptionsLogger.logClientException(e);
            return INEXISTENT_GROUP_BOOKMARK;
        } catch (IllegalArgumentException e) {
            ExceptionsLogger.logClientException(e);
            return INVALID_COMMAND_PARAMS;
        }
            usersStorage.updateUser(loggedInUser.getUsername(), loggedInUser);
            invalidateFinder(loggedInUser.getUsername());

        return String.format("Successful remove of bookmark %s " +
                "from group %s of user %s", bookmarkTitle, groupName,
                loggedInUser.getUsername());
    }

    @Override
    public String cleanUp(SocketChannel clientChannel) {
        if (!hasUserLoggedIn(clientChannel)) {
            return NOT_LOGGED_WARNING;
        }
        User loggedInUser = loggedInUsers.get(clientChannel);
        loggedInUser.getStorage().cleanUp();
        invalidateFinder(loggedInUser.getUsername());
        return String.format("Successful removal of user's %s " +
                "invalid bookmarks (if there were such)",
                loggedInUser.getUsername());
    }

    @Override
    public List<Bookmark> listAll(SocketChannel clientChannel) {
        if (!hasUserLoggedIn(clientChannel)) {
            throw new UserNotLoggedInException("User with socket channel "
                    + clientChannel.toString() + "has not logged in!");
        }
       return finder.searchBookmarksByUser(loggedInUsers.
               get(clientChannel).getUsername(), usersStorage);

    }

    @Override
    public List<Bookmark> listByGroup(SocketChannel clientChannel, String groupName) {
        if (!hasUserLoggedIn(clientChannel)) {
            throw new UserNotLoggedInException("User with socket channel "
                    + clientChannel.toString() + "has not logged in!");
        }
        return finder.searchBookmarksByGroup(groupName, loggedInUsers.
                get(clientChannel).getUsername(), usersStorage);
    }

    @Override
    public List<Bookmark> searchByTags(SocketChannel clientChannel, Set<String> keywords) {
        if (!hasUserLoggedIn(clientChannel)) {
            throw new UserNotLoggedInException("User with socket channel "
                    + clientChannel.toString() + "has not logged in!");
        }

        return finder.searchBookmarksByTags(loggedInUsers.
                get(clientChannel).getUsername(), keywords, usersStorage);

    }

    @Override
    public List<Bookmark> searchByTitle(SocketChannel clientChannel, String title) {
        if (!hasUserLoggedIn(clientChannel)) {
           throw new UserNotLoggedInException("User with socket channel "
                   + clientChannel.toString() + "has not logged in!");
        }

        return finder.searchBookmarksByTitle(loggedInUsers.
                get(clientChannel).getUsername(), title, usersStorage);
    }

    @Override
    public List<Bookmark> importFromChrome(SocketChannel clientChannel) {
        if (!hasUserLoggedIn(clientChannel)) {
            throw new UserNotLoggedInException("User with socket channel "
                    + clientChannel.toString() + "has not logged in!");
        }
       return loggedInUsers.get(clientChannel).
               getStorage().importBookmarksFromChrome();
    }

    @Override
    public void disconnectUser(SocketChannel clientChannel) {
        if (loggedInUsers.containsKey(clientChannel)) {
            User disconnectedUser = loggedInUsers.get(clientChannel);
            disconnectedUser.getStorage().updateGroupsFile();
            loggedInUsers.remove(clientChannel);
        }
        usersStorage.saveUsers();
    }

    @Override
    public Map<SocketChannel, User> getLoggedInUsers() {
        return loggedInUsers;
    }

    private boolean hasUserLoggedIn(SocketChannel clientChannel) {
        try {
            if (!loggedInUsers.containsKey(clientChannel)) {
                ExceptionsLogger.logClientException(new UserNotLoggedInException(String.format(
                        "User with socket channel address %s has not logged in!",
                        clientChannel.getLocalAddress())));
                return false;
            }
        } catch (IOException e) {
            ExceptionsLogger.logClientException(e);
            return false;
        }
        return true;
    }

    private void invalidateFinder(String username) {
        finder.invalidateUserCache(username);
    }

}
