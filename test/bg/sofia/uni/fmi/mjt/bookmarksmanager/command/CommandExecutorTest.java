package bg.sofia.uni.fmi.mjt.bookmarksmanager.command;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.BookmarksManager;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.InvalidCredentialsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.UserNotLoggedInException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandExecutorTest {

    private static final String INVALID_ARGUMENTS_FORMAT_MESSAGE = "Command format expected: %s. Provided: %s.";
    private static final String UNKNOWN_COMMAND_MESSAGE = "There is no such command. " +
            "Please, type '?' to see the possible commands and use this app wholesomely.";
    private static final String NOT_LOGGED_WARNING = "User must " +
            "have logged in before using the app's commands!";
    private static final String INVALID_CREDENTIALS = "Invalid user's credentials (username or password)!";

    private static final String REGISTER_CMD = "register";
    private static final String LOGIN_CMD = "login";
    private static final String NEW_GROUP_CMD = "new-group";
    private static final String ADD_CMD = "add-to";
    private static final String REMOVE_CMD = "remove-from";
    private static final String LIST_CMD = "list";
    private static final String SEARCH_CMD = "search";
    private static final String CLEAN_UP_CMD = "cleanup";
    private static final String IMPORT_CMD = "import-from-chrome";
    private static final String DISCONNECT_CMD = "disconnect";

    private BookmarksManager manager;
    private CommandExecutor commandExecutor;

    private String testUserName = "user1";
    private String testUserPassword = "passwordUser1";
    private String testGroupName = "Group1";
    private String testBookmarkTitle = "Bookmark1";
    private SocketChannel sc1;

    @BeforeEach
    void setUp() throws IOException {
        manager = mock(BookmarksManager.class);
        commandExecutor = new CommandExecutor(manager);
        sc1 = SocketChannel.open();
    }

    @AfterAll
    static void cleanUpLogger() throws IOException {
        ExceptionsLogger.cleanUpLogs();
    }

    @Test
    void testRegisterCommand() {
        String [] arguments = {testUserName, testUserPassword, testGroupName};
        String result = commandExecutor.execute(new Command(REGISTER_CMD, arguments), sc1);
        String expectedResult = String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.REGISTER.getCommandValue(),
                REGISTER_CMD + Arrays.toString(arguments));
        assertEquals(result, expectedResult);
    }

    @Test
    void testLoginCommand() {
        String [] arguments = {testUserName, testUserPassword, testGroupName};
        String result = commandExecutor.execute(new Command(LOGIN_CMD, arguments), sc1);

        String expectedResult = String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.LOGIN.getCommandValue(), LOGIN_CMD + Arrays.toString(arguments));
        assertEquals(result, expectedResult);
    }

    @Test
    void testLoginCommandThrows() {
        when(manager.login(sc1, testUserName, testUserPassword)).
                thenThrow(new InvalidCredentialsException("Invalid username/password!"));

        String [] arguments = {testUserName, testUserPassword};
        String result = commandExecutor.execute(new Command(LOGIN_CMD, arguments), sc1);
        assertEquals(result, INVALID_CREDENTIALS);
    }

    @Test
    void testCreateNewGroup() {
        String [] arguments = {testUserName, testGroupName, testUserPassword};
        String result = commandExecutor.execute(new Command(NEW_GROUP_CMD, arguments), sc1);

        String expectedResult = String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.NEW_GROUP.getCommandValue(), NEW_GROUP_CMD +
                        Arrays.toString(arguments));
        assertEquals(result, expectedResult);
    }

    @Test
    void testAddNewBookmarkToGroup() {
        String [] arguments = {testUserName, testGroupName, testUserPassword};
        String result = commandExecutor.execute(new Command(ADD_CMD, arguments), sc1);

        String expectedResult = String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.ADD.getCommandValue(),
                ADD_CMD + Arrays.toString(arguments));
        assertEquals(result, expectedResult);
    }

    @Test
    void testRemoveBookmarkFromGroup() {
        String [] arguments = {testUserName, testGroupName, testUserPassword};
        String result = commandExecutor.execute(new Command(REMOVE_CMD, arguments), sc1);

        String expectedResult = String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.REMOVE.getCommandValue(),
                REMOVE_CMD + Arrays.toString(arguments));
        assertEquals(result, expectedResult);
    }

    @Test
    void testListBookmarks() {
        String [] arguments = {testUserName, testGroupName, testUserPassword};
        String result = commandExecutor.execute(new Command(LIST_CMD, arguments), sc1);

        String expectedResult = String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.LIST.getCommandValue() + " or " +
                        CommandTemplate.LIST_GROUP.getCommandValue(),
                LIST_CMD + Arrays.toString(arguments));
        assertEquals(result, expectedResult);
    }

    @Test
    void testListBookmarksNotLoggedInListAll() {
        when(manager.listAll(sc1)).thenThrow(UserNotLoggedInException.class);
        String result = commandExecutor.
                execute(new Command(LIST_CMD, new String[] {}), sc1);
        assertEquals(result, NOT_LOGGED_WARNING);
    }

    @Test
    void testListBookmarksNotLoggedInListByGroup() {
        when(manager.listByGroup(sc1, testGroupName)).
                thenThrow(UserNotLoggedInException.class);
        String result = commandExecutor.
                execute(new Command(LIST_CMD, new String[] {"--group-name", testGroupName}), sc1);
        assertEquals(result, NOT_LOGGED_WARNING);
    }

    @Test
    void testSearchBookmarks() {
        String result = commandExecutor.execute(new Command(SEARCH_CMD, new String[] {}), sc1);
        String expectedResult = String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.SEARCH_TAGS.getCommandValue() + "or" +
                        CommandTemplate.SEARCH_TITLE.getCommandValue(),
                SEARCH_CMD + "[]");
        assertEquals(result, expectedResult);
    }

    @Test
    void testSearchBookmarksByTitleUserNotLoggedIn() {
        when(manager.searchByTitle(sc1, testBookmarkTitle)).
                thenThrow(UserNotLoggedInException.class);
        String result = commandExecutor.
                execute(new Command(SEARCH_CMD, new String[]
                        {"--title", testBookmarkTitle}), sc1);
        assertEquals(result, NOT_LOGGED_WARNING);
    }

    @Test
    void testSearchBookmarksByTagsUserNotLoggedIn() {
        when(manager.searchByTags(sc1, Set.of("tag1", "tag2"))).
                thenThrow(UserNotLoggedInException.class);
        String result = commandExecutor.
                execute(new Command(SEARCH_CMD, new String[] {"--tags", "tag1", "tag2"}), sc1);
        assertEquals(result, NOT_LOGGED_WARNING);
    }

    @Test
    void testCleanUp() {
        String [] arguments = {testUserName, testUserPassword};
        String expectedResult = String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.CLEAN_UP.getCommandValue(), CLEAN_UP_CMD +
                        Arrays.toString(arguments));

        String result = commandExecutor.execute(new Command(CLEAN_UP_CMD, arguments), sc1);
        assertEquals(expectedResult, result);
    }

    @Test
    void testImportFromChrome() {
        String [] arguments = {testUserName, testUserPassword};
        String expectedResult = String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.IMPORT.getCommandValue(),
                IMPORT_CMD + Arrays.toString(arguments));

        String result = commandExecutor.execute(new
                Command(IMPORT_CMD, arguments), sc1);
        assertEquals(result, expectedResult);

    }

    @Test
    void testDisconnectClient() {
        assertEquals(commandExecutor.execute(new
                Command(DISCONNECT_CMD, new String [] {}), null),
                "No such a connection to the server!");
    }
}
