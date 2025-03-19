package bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.BookmarksGroup;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.NoSuchBookmarkException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.NoSuchGroupException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;


class BookmarksGroupStorageTest {

    private static final String TEST_FILE_NAME = "test" + File.separator +
            "bg" + File.separator + "sofia" + File.separator +
            "uni" + File.separator + "fmi" + File.separator + "mjt" +
            File.separator + "bookmarksmanager" + File.separator + "server"
            + File.separator + "storage" + File.separator + "testStorageFile.txt";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private BookmarksGroupStorage bookmarksGroupStorage;

    @BeforeEach
    void setUp() {
        Map<String, BookmarksGroup> groups = new HashMap<>();
        Bookmark bookmark = new Bookmark("Ozone", "https://www.ozone.bg/",
                Set.of("bookstore", "book", "gaming"), "OnlineStores");

        Map<String, Bookmark> bookmarks = new HashMap<>();
        bookmarks.put("Ozone", bookmark);

        BookmarksGroup group1 = new BookmarksGroup("Group1", bookmarks);
        groups.put("Group1", group1);
        bookmarksGroupStorage = new BookmarksGroupStorage(groups, TEST_FILE_NAME);
    }

   @AfterEach
    void cleanUp() throws IOException {
        Path path = Path.of(TEST_FILE_NAME);
        Files.deleteIfExists(path);
       ExceptionsLogger.cleanUpLogs();
    }

    @Test
    void testContainsGroupWithExistingGroup() {
        assertTrue(bookmarksGroupStorage.containsGroup("Group1"));
    }

    @Test
    void testContainsGroupWithNonExistingGroup() {
        assertFalse(bookmarksGroupStorage.containsGroup("NoSuchGroup"));
    }

    @Test
    void testCreateNewGroupSuccessfully() {
        bookmarksGroupStorage.createNewGroup("NewGroup");
        assertTrue(bookmarksGroupStorage.containsGroup("NewGroup"));
    }

    @Test
    void testCreateNewGroupThrows() {
        assertThrows(GroupAlreadyExistsException.class,
                () -> {
                    bookmarksGroupStorage.createNewGroup("Group1");
                },
                "GroupAlreadyExistsException expected" +
                        " when attempting to add an already created group!");
    }

    @Test
    void testAddNewBookmarkToGroupSuccessfully() {
        Bookmark newBookmark = new Bookmark("Ozone", "https://www.ozone.bg/",
                Set.of("bookstore", "book", "gaming"), "OnlineStores");
        bookmarksGroupStorage.addNewBookmarkToGroup(newBookmark, "Group1");
        assertTrue(bookmarksGroupStorage.getGroups().
                get("Group1").getBookmarks().contains(newBookmark));
    }

    @Test
    void testAddNewBookmarkToGroupThrowsWhenInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> {
                    bookmarksGroupStorage.
                            addNewBookmarkToGroup(null, "Group1");
                },
                "IllegalArgumentException expected " +
                        "when groupName/bookmark is null/blank.");
    }

    @Test
    void testAddNewBookmarkToGroupThrowsWhenNoSuchGroup() {
        Bookmark newBookmark = new Bookmark("Ozone", "https://www.ozone.bg/",
                Set.of("bookstore", "book", "gaming"), "OnlineStores");
        assertThrows(NoSuchGroupException.class, () -> {
                    bookmarksGroupStorage.
                            addNewBookmarkToGroup(newBookmark, "InexistingGroup");
                },
                "NoSuchGroupException expected " +
                        "when adding a bookmark to an inexisting group.");
    }

    @Test
    void testRemoveBookmarkFromGroupThrowsWhenInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> {
                    bookmarksGroupStorage.removeBookmarkFromGroup(null,
                            "Group1");
                },
                "IllegalArgumentException expected " +
                        "when calling removeBookmark with " +
                        "a null/blank groupName/bookmark.");
    }

    @Test
    void testRemoveBookmarkFromGroupThrowsWhenNoSuchGroup() {
        assertThrows(NoSuchGroupException.class, () -> {
                    bookmarksGroupStorage.
                            removeBookmarkFromGroup("Ozone",
                                    "InexistingGroup");
                },
                "NoSuchGroupException expected " +
                        "when removing a bookmark from an inexisting group.");
    }

    @Test
    void testRemoveBookmarkFromGroupThrowsWhenNoSuchBookmark() {
        assertThrows(NoSuchBookmarkException.class, () -> {
                    bookmarksGroupStorage.
                            removeBookmarkFromGroup("InexistingBookmark",
                                    "Group1");
                },
                "NoSuchBookmarkException expected " +
                        "when trying to remove an " +
                        "inexisting bookmark from a group.");
    }

    @Test
    void testRemoveBookmarkFromGroupSuccessfully() {
        bookmarksGroupStorage.removeBookmarkFromGroup("Ozone", "Group1");
        assertFalse(bookmarksGroupStorage.getGroups().get("Group1").
                containsBookmark("Ozone"));
    }

    @Test
    void testCleanUpInvalidBookmark() {
        Bookmark invalidBookmark = new Bookmark("Invalid",
                "https://example.com/nonexistentpage",
                Set.of("noExistent"), "Group1");
        bookmarksGroupStorage.addNewBookmarkToGroup(invalidBookmark,
                "Group1");

        bookmarksGroupStorage.cleanUp();
        assertFalse(bookmarksGroupStorage.getGroups().
                get("Group1").containsBookmark("Invalid"));
    }

    @Test
    public void testUpdateGroupsFileCreatesValidJson() throws IOException {
        Bookmark newBookmark = new Bookmark("Bookmark1",
                "https://example.com", Set.of("tag1"), "Group1");
        Map<String, Bookmark> bookmarks = new HashMap<>();
        bookmarks.put("Bookmark1", newBookmark);
        BookmarksGroup group1 = new BookmarksGroup("Group1", bookmarks);

        Map<String, BookmarksGroup> groups = new HashMap<>();
        groups.put("Group1", group1);
        BookmarksGroupStorage storageNew = new BookmarksGroupStorage(groups, TEST_FILE_NAME);
        storageNew.updateGroupsFile();

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        BookmarksGroupStorage resultStorage = GSON.fromJson(result.toString(),
                BookmarksGroupStorage.class);
        assertTrue(resultStorage.containsGroup("Group1"),
                "BookmarksGroupStorage should contain the same " +
                        "group(s) after reading it from file");

        assertTrue(resultStorage.getGroups().get("Group1").getBookmarks().contains(newBookmark),
                "BookmarksGroupStorage should contain " +
                        "the same bookmark after reading it from file.");
       }
}
