package bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.user.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UsersStorageTest {

    private static final String TEST_FILE_USERS = "test" + File.separator +
            "bg" + File.separator + "sofia" + File.separator +
            "uni" + File.separator + "fmi" + File.separator + "mjt" +
            File.separator + "bookmarksmanager" + File.separator + "server"
            + File.separator + "storage" + File.separator + "testUsersFile.dat";


    private static UsersStorage storage = new UsersStorage(TEST_FILE_USERS);

    @BeforeAll
    static void setUpStorage() {
        storage.register("user1", "userPassword1");
        storage.register("user2", "userPassword2");
    }

    @AfterAll
    static void cleanUp() throws IOException {
        Path path = Path.of(TEST_FILE_USERS);
        Files.deleteIfExists(path);
        ExceptionsLogger.cleanUpLogs();
    }

    @Test
    void testRegisterInvalidUsername() {
        String expectedResult = "Username/password " +
                "must not be null or blank!";
        assertEquals(expectedResult, storage.register(null,
                "somePasswd1"));

    }

    @Test
    void testRegisterPasswordNotMatchingPattern() {
        String expectedResult = "Password must consist " +
                "of at least five characters and " +
                "contain at least one capital letter, " +
                "one small letter and one digit.";
        assertEquals(expectedResult, storage.register("newUser",
                "invalidpasswd"));

    }

    @Test
    void testRegisterUserAlreadyExists() {
        assertThrows(UserAlreadyExistsException.class, () -> {
            storage.register("user1", "userPassword1");
        }, "Register should throw when passing an " +
                "already existing user as an argument!" );

    }

    @Test
    void testRegisterSuccessful() {
        String expectedResult = "User NewUser has been successfully registered.";
        assertEquals(expectedResult, storage.register("NewUser",
                "newUserPasswd123"));

    }

    @Test
    void testIsARegisteredUser() {
        assertTrue(storage.isARegisteredUser("user1"));
        assertFalse(storage.isARegisteredUser("noSuchUser"));
    }

    @Test
    void testReadAndWriteUsersToFile() {
        Map<String, User> users = new HashMap<>();

        users.put("newUser1", new User("newUser1",
                "newUserPasswd1",
                Mockito.mock(BookmarksGroupStorage.class)));
        users.put("newUser2", new User("newUser2",
                "newUserPasswd2",
                Mockito.mock(BookmarksGroupStorage.class)));
        UsersStorage expectedStorage = new UsersStorage(users,
                TEST_FILE_USERS);
        expectedStorage.saveUsers();
        UsersStorage actualStorage = new UsersStorage(TEST_FILE_USERS);
        assertTrue(actualStorage.isARegisteredUser("newUser1"));
        assertTrue(actualStorage.isARegisteredUser("newUser2"));
        assertTrue(actualStorage.getUsers().get("newUser1").
                validatePassword("newUserPasswd1"));
        assertTrue(actualStorage.getUsers().get("newUser2").
                validatePassword("newUserPasswd2"));
    }
}
