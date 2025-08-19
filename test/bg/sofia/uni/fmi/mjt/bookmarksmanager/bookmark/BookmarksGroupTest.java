package bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BookmarksGroupTest {
    @Test
    void testAddNewBookmark() {
        Map<String, Bookmark> bookmarks = new HashMap<>();
        BookmarksGroup group = new BookmarksGroup("TestGroup", bookmarks);

        Bookmark bookmark = new Bookmark("Title1", "http://example.com",
                Set.of("tag1"), "TestGroup");

        group.addNewBookmark(bookmark);

        assertTrue(bookmarks.containsKey("Title1"));
        assertEquals(bookmark, bookmarks.get("Title1"));
    }

    @Test
    void testAddNullBookmarkDoesNothing() {
        Map<String, Bookmark> bookmarks = new HashMap<>();
        BookmarksGroup group = new BookmarksGroup("TestGroup", bookmarks);

        group.addNewBookmark(null);

        assertTrue(bookmarks.isEmpty());
    }

    @Test
    void testRemoveBookmark() {
        Bookmark bookmark = new Bookmark("Title1", "http://example.com",
                Set.of("tag1"), "TestGroup");

        Map<String, Bookmark> bookmarksMap = new HashMap<>();
        bookmarksMap.put(bookmark.title(), bookmark);

        BookmarksGroup group = new BookmarksGroup("TestGroup", bookmarksMap);
        group.removeBookmark(bookmark);

        assertFalse(bookmarksMap.containsKey("Title1"));
    }

    @Test
    void testRemoveNullBookmarkDoesNothing() {
        Bookmark bookmark = new Bookmark("Title1", "http://example.com",
                Set.of("tag1"), "TestGroup");

        Map<String, Bookmark> bookmarksMap = new HashMap<>();
        bookmarksMap.put(bookmark.title(), bookmark);

        BookmarksGroup group = new BookmarksGroup("TestGroup", bookmarksMap);
        group.removeBookmark(null);

        assertTrue(bookmarksMap.containsKey("Title1"));
    }

    @Test
    void testRemoveMultipleBookmarks() {
        Bookmark bm1 = new Bookmark("Title1", "http://example.com",
                Set.of("tag1"), "TestGroup");
        Bookmark bm2 = new Bookmark("Title2", "http://example2.com",
                Set.of("tag2"), "TestGroup");

        Map<String, Bookmark> bookmarksMap = new HashMap<>();
        bookmarksMap.put(bm1.title(), bm1);
        bookmarksMap.put(bm2.title(), bm2);

        BookmarksGroup group = new BookmarksGroup("TestGroup", bookmarksMap);
        group.removeBookmarks(Set.of(bm1, bm2));

        assertTrue(bookmarksMap.isEmpty());
    }

    @Test
    void testContainsBookmark() {
        Bookmark bm = new Bookmark("Title1", "http://example.com",
                Set.of("tag1"), "TestGroup");

        Map<String, Bookmark> bookmarksMap = new HashMap<>();
        bookmarksMap.put(bm.title(), bm);

        BookmarksGroup group = new BookmarksGroup("TestGroup", bookmarksMap);

        assertTrue(group.containsBookmark("Title1"));
        assertFalse(group.containsBookmark("Unknown"));
        assertFalse(group.containsBookmark(null));
        assertFalse(group.containsBookmark("   "));
    }

    @Test
    void testGetGroupName() {
        BookmarksGroup group = new BookmarksGroup("TestGroup", new HashMap<>());
        assertEquals("TestGroup", group.getGroupName());
    }

    @Test
    void testGetBookmarksReturnsList() {
        Bookmark bm1 = new Bookmark("Title1", "http://example.com",
                Set.of("tag1"), "TestGroup");
        Bookmark bm2 = new Bookmark("Title2", "http://example2.com",
                Set.of("tag2"), "TestGroup");

        Map<String, Bookmark> bookmarksMap = new HashMap<>();
        bookmarksMap.put(bm1.title(), bm1);
        bookmarksMap.put(bm2.title(), bm2);

        BookmarksGroup group = new BookmarksGroup("TestGroup", bookmarksMap);

        List<Bookmark> bookmarksList = group.getBookmarks();
        assertEquals(2, bookmarksList.size());
        assertTrue(bookmarksList.contains(bm1));
        assertTrue(bookmarksList.contains(bm2));
    }
}
