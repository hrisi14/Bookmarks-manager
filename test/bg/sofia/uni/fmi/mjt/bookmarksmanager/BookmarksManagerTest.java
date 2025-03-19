package bg.sofia.uni.fmi.mjt.bookmarksmanager;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.BookmarksGroup;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.InvalidCredentialsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.NoSuchUserException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.UserNotLoggedInException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.finder.BookmarksFinder;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.BookmarksGroupStorage;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.UsersStorage;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;



public class BookmarksManagerTest {
    private static final String TEST_FILE_NAME = "test" + File.separator +
            "bg" + File.separator + "sofia" + File.separator +
            "uni" + File.separator + "fmi" + File.separator + "mjt" +
            File.separator + "bookmarksmanager" + File.separator + "server"
            + File.separator + "storage" + File.separator + "testGroupsFile.txt";

    private static final String TEST_FILE_USERS = "test" + File.separator +
            "bg" + File.separator + "sofia" + File.separator +
            "uni" + File.separator + "fmi" + File.separator + "mjt" +
            File.separator + "bookmarksmanager" + File.separator + "server"
            + File.separator + "storage" + File.separator + "testUsersFile.dat";

    private static final String NOT_LOGGED_WARNING = "User must " +
            "have logged in before using the app's commands!";

    private static final String INVALID_COMMAND_PARAMS = "Invalid command's " +
            "parameters- group name or bookmark!";

    private static final String INEXISTENT_GROUP_BOOKMARK = "User does" +
            " not have such a group/bookmark!";

    private Map<SocketChannel, User> loggedInUsers;

    private UsersStorage usersStorage;
    private BookmarksFinder finder = mock(BookmarksFinder.class);
    private BookmarksGroupStorage bookmarksStorage1, bookmarksStorage2;

    private BookmarksManager manager;

    private SocketChannel sc1, sc2, sc3;


    @Before
     public void setUp() {
        try {
            sc1 = SocketChannel.open();
            sc2 = SocketChannel.open();
            sc3 = SocketChannel.open();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Bookmark bookmark = new Bookmark("Github", "https://github.com/",
                Set.of("github", "branch", "commit"), "DevOps");
        Map<String, Bookmark> bookmarks = new HashMap<>();
        bookmarks.put("Github", bookmark);

        BookmarksGroup group1 = new BookmarksGroup("Group1", bookmarks);
        Map<String, BookmarksGroup> groups = new HashMap<>();
        groups.put("Group1", group1);
        bookmarksStorage1 = new BookmarksGroupStorage(groups, TEST_FILE_NAME);

        bookmarksStorage2 = new BookmarksGroupStorage(groups,TEST_FILE_NAME);

        User user1 = new User ("User1",
                "passwordUser1", bookmarksStorage1);
        User user2 = new User ("User2",
                "passwordUser2", bookmarksStorage2);

        Map<String, User> users = new HashMap<>();
        users.put("User1", user1);
        users.put("User2", user2);

        usersStorage = new UsersStorage(users, TEST_FILE_USERS);
        loggedInUsers = new HashMap<>();
        loggedInUsers.put(sc1, user1);
        manager = new BookmarksManager(loggedInUsers, usersStorage, finder);
    }

    @After
    public void cleanUp() throws IOException {
        Path path = Path.of(TEST_FILE_NAME);
        Files.deleteIfExists(path);
        ExceptionsLogger.cleanUpLogs();
    }

    @Test
    public void testRegisterSuccessful() {

        String result =  manager.register(sc3, "User3", "passwordUser3");
        String expectedResult = "User User3 has been successfully registered.";
        assertEquals(result, expectedResult);
    }

    @Test
    public void testRegisterUserExists() {
        String result = manager.register(sc1, "User1", "passwordUser1");
        String expectedResult = "User with username User1 already exists!";
        assertEquals(result, expectedResult);
    }

    @Test
    public void testLoginSuccessful() {
        String result = manager.login(sc2, "User2", "passwordUser2");
        String expectedResult = "User with name User2 has successfully logged in.";
        assertEquals(result, expectedResult);
    }

    @Test
    public void testUserHasAlreadyLoggedIn() {
        String result = manager.login(sc1, "User1", "userU1219");
        String expectedResult = "User with username User1 has already logged in!";
        assertEquals(result, expectedResult);
    }

    @Test
    public void testLoginAssertThrowsInvalidCredentialsException() {
        Assertions.assertThrows(InvalidCredentialsException.class, () ->
                        manager.login(sc2, "User2", "incorrectPassword3"),
                "InvalidCredentialsException expected when " +
                        "logging with an incorrect password.");
    }

    @Test
    public void testLoginAssertThrowsIllegalArgumentException() {
        assertEquals(manager.login(sc1, null, "passwordUser1"),
                "Username/password " +
                        "must not be null or blank!");
    }

    @Test
    public void testLoginAssertThrowsNoSuchUserException() {
        Assertions.assertThrows(NoSuchUserException.class, () ->
                        manager.login(sc2, "NotRegisteredUser", "Password5"),
                "NoSuchUserException expected when unregistered user tries to log in.");
    }

    @Test
    public void testCreateNewBookmarksGroupSuccessfully() {
        String expectedResult = String.format("Successful creation of bookmarks " +
                "group %s for user %s", "Group2", "User1");
        String result = manager.createNewBookmarksGroup(sc1, "Group2");
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testCreateNewBookmarksGroupUserNotLoggedIn() {
        assertEquals(manager.createNewBookmarksGroup(sc2, "NewGroup"), NOT_LOGGED_WARNING);
    }

    @Test
    public void testCreateNewBookmarksGroupInvalidParameters() {
        assertEquals(manager.createNewBookmarksGroup(sc1, null),
                INVALID_COMMAND_PARAMS + "group name: " + null);
    }

    @Test
    public void testAddNewBookmarkToGroupSuccessfully() {
        manager.createNewBookmarksGroup(sc1, "Group1");
        String url = "https://github.com/fmi/java-course/tree/master";
        String result = manager.addNewBookmarkToGroup(sc1,
                "Group1", url,false);
        String expected = String.format("Successful add of bookmark %s " +
                "to group %s of user %s", url, "Group1", "User1");
        assertEquals(result, expected);
    }

    @Test
    public void testAddNewBookmarkToGroupUserNotLoggedIn() {
        String url = "https://github.com/fmi/java-course/tree/master";
        assertEquals( manager.addNewBookmarkToGroup(sc2,
                        "Group2",url, false),
                NOT_LOGGED_WARNING);
    }

    @Test
    public void testAddNewBookmarkToNoSuchGroup() {
        String url = "https://github.com/fmi/java-course/tree/master";
        assertEquals( manager.addNewBookmarkToGroup(sc1,
                        "Group2",url, false),
                INEXISTENT_GROUP_BOOKMARK);
    }

    @Test
    public void testAddNewBookmarkToGroupInvalidUrl() {
        assertEquals( manager.addNewBookmarkToGroup(sc1,
                        "Group2",null, false),
                INVALID_COMMAND_PARAMS);
    }

    @Test
    public void testRemoveBookmarkFromGroupSuccessfully() {
        String expectedOutput = String.format("Successful remove of bookmark %s " +
                "from group %s of user %s", "Github", "Group1", "User1");
        assertEquals(expectedOutput,  manager.removeBookmarkFromGroup(sc1,
                "Group1", "Github"));
    }

    @Test
    public void  testRemoveBookmarkFromGroupUserNotLoggedIn() {
        assertEquals(NOT_LOGGED_WARNING,  manager.removeBookmarkFromGroup(sc3,
                "Group1", "Github"));
    }

    @Test
    public void  testRemoveBookmarkFromGroupNoSuchGroup() {
        assertEquals(INEXISTENT_GROUP_BOOKMARK,  manager.removeBookmarkFromGroup(sc1,
                "InexistentGroup", "Github"));
    }

    @Test
    public void  testRemoveBookmarkFromGroupNoSuchBookmark() {
        assertEquals(INEXISTENT_GROUP_BOOKMARK,  manager.removeBookmarkFromGroup(sc1,
                "Group1", "InexistentBookmark"));
    }

    @Test
    public void  testRemoveBookmarkFromGroupInvalidParams() {
        assertEquals(INVALID_COMMAND_PARAMS,  manager.removeBookmarkFromGroup(sc1,
                null, null));
    }

    @Test
    public void  testCleanUpUserNotLoggedIn() {
        assertEquals(NOT_LOGGED_WARNING,  manager.cleanUp(sc2));
    }

    @Test
    public void cleanUpSuccessful() {
        String expectedOutput = "Successful removal of user's User1 " +
                        "invalid bookmarks (if there were such)";
        assertEquals(expectedOutput,  manager.cleanUp(sc1));
    }

    @Test
    public void testListAllAssertThrows() {
        Assertions.assertThrows(UserNotLoggedInException.class, () -> {
            manager.listAll(sc2);
            }, "UserNotLoggedInException expected when " +
                "listing bookmarks of not logged in user!");

    }

    @Test
    public void testListByGroupAssertThrows() {
        Assertions.assertThrows(UserNotLoggedInException.class, () -> {
            manager.listByGroup(sc2, "Group2");
        }, "UserNotLoggedInException expected when " +
                "listing bookmarks of not logged in user!");

    }

    @Test
    public void testSearchByTitleAssertThrows() {
        Assertions.assertThrows(UserNotLoggedInException.class, () -> {
            manager.searchByTitle(sc2, "MJT-course");
        }, "UserNotLoggedInException expected when " +
                "searching for bookmarks of not logged in user!");
    }

    @Test
    public void testSearchByTagAssertThrows() {
        Assertions.assertThrows(UserNotLoggedInException.class, () -> {
            manager.searchByTags(sc2, Set.of("fmi"));
        }, "UserNotLoggedInException expected when " +
                "searching for bookmarks of not logged in user!");
    }

    @Test
    public void testImportFromChromeAssertThrows() {
        Assertions.assertThrows(UserNotLoggedInException.class, () -> {
            manager.importFromChrome(sc2);
        }, "UserNotLoggedInException expected when " +
                "searching for bookmarks of not logged in user!");
    }

    @Test
    public void testDisconnectUserRemoveClientChannel() {
        manager.disconnectUser(sc1);
        assertFalse(manager.getLoggedInUsers().containsKey(sc1));
    }
}




