package bg.sofia.uni.fmi.mjt.bookmarksmanager.user;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.BookmarksGroupStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private BookmarksGroupStorage mockStorage;
    private User user;

    @BeforeEach
    void setUp() {
        mockStorage = Mockito.mock(BookmarksGroupStorage.class);
        user = new User("john", "password123", mockStorage);
    }

    @Test
    void testSetPasswordValidPassword() {
        user.setPassword("newPass123");
        assertTrue(user.validatePassword("newPass123"));
    }

    @Test
    void testSetPasswordNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> user.setPassword(null));
    }

    @Test
    void testSetPasswordBlankThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> user.setPassword("  "));
    }

    @Test
    void testSetUsernameValid() {
        user.setUsername("newJohn");
        assertEquals("newJohn", user.getUsername());
    }

    @Test
    void testSetUsernameInvalidThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> user.setUsername(""));
    }

    @Test
    void testValidatePasswordCorrectPassword() {
        assertTrue(user.validatePassword("password123"));
    }

    @Test
    void testValidatePasswordIncorrectPassword() {
        assertFalse(user.validatePassword("wrongPass"));
    }

    @Test
    void testValidatePasswordNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> user.validatePassword(null));
    }

    @Test
    void testGetStorageReturnsSameStorage() {
        assertEquals(mockStorage, user.getStorage());
    }

    @Test
    void testEqualsSameUser() {
        User another = new User("john", "differentPass", mockStorage);
        assertEquals(user, another);
    }

    @Test
    void testEqualsDifferentUsername() {
        User another = new User("jane", "password123", mockStorage);
        assertNotEquals(user, another);
    }

    @Test
    void testEqualsDifferentType() {
        assertNotEquals(user, "NotAUser");
    }

    @Test
    void testHashCodeConsistentWithEquals() {
        User another = new User("john", "password123", mockStorage);
        assertEquals(user.hashCode(), another.hashCode());
    }
}
