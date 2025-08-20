package bg.sofia.uni.fmi.mjt.bookmarksmanager.finder;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.BookmarksGroup;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.BookmarksGroupStorage;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.UsersStorage;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

public class BookmarksFinderTest {
    private BookmarksFinder finder;

    private Bookmark b1, b2, b3;
    private List<Bookmark> linksOfUser1, linksOfUser2;
    private Map<String, List<Bookmark>> initialCache;
    private UsersStorage storage;
    private Map<String, User> usersMap;
    private User user3;
    private BookmarksGroupStorage user3Storage;
    private Map<String, BookmarksGroup> groupsMap;
    private BookmarksGroup groupA, groupB;

    @BeforeEach
    void setUp() {
        b1 = new Bookmark("MjtCourse-github", "https://github.com/fmi/java-course/tree/master",
                Set.of("fmi", "mjt", "java"), "Educational");
        b2 = new Bookmark("Github", "https://github.com/",
                Set.of("github", "branch", "commit"), "DevOps");
        b3 = new Bookmark("Ozone", "https://www.ozone.bg/",
                Set.of("bookstore", "book", "gaming"), "OnlineStores");

        linksOfUser1 = List.of(b1, b2);
        linksOfUser2 = List.of(b3);

        initialCache = new ConcurrentHashMap<>();
        initialCache.put("User1", linksOfUser1);
        initialCache.put("User2", linksOfUser2);
        finder = new BookmarksFinder(initialCache);

        storage = Mockito.mock(UsersStorage.class);
        usersMap = Mockito.mock(Map.class);
        user3 = Mockito.mock(User.class);
        user3Storage = Mockito.mock(BookmarksGroupStorage.class);
        groupsMap = Mockito.mock(Map.class);
        groupA = Mockito.mock(BookmarksGroup.class);
        groupB = Mockito.mock(BookmarksGroup.class);

        when(storage.getUsers()).thenReturn(usersMap);
        when(usersMap.get("User3")).thenReturn(user3);
        when(user3.getStorage()).thenReturn(user3Storage);
        when(user3Storage.getGroups()).thenReturn(groupsMap);

        Bookmark b4 = new Bookmark("Docs: Mockito", "https://site.example/mok",
                Set.of("testing", "mockito"), "Learn");
        Bookmark b5 = new Bookmark("JUnit Guide", "https://site.example/junit",
                Set.of("testing", "junit"), "Learn");
        Bookmark b6 = new Bookmark("CI Pipelines", "https://site.example/ci",
                Set.of("devops", "ci"), "Build");

        when(groupsMap.values()).thenReturn(List.of(groupA, groupB));
        when(groupA.getBookmarks()).thenReturn(List.of(b4, b5));
        when(groupB.getBookmarks()).thenReturn(List.of(b6));
    }


    @Test
    void testSearchBookmarksByUserInCache() {
        List<Bookmark> result = finder.searchBookmarksByUser("User1", storage);
        assertEquals(new HashSet<>(linksOfUser1), new HashSet<>(result));
    }

    @Test
    void testSearchBookmarksByUserNotInCache() {
        List<Bookmark> first = finder.searchBookmarksByUser("User3", storage);
        assertEquals(3, first.size());

        verify(storage, atLeastOnce()).getUsers();
        verify(usersMap, atLeastOnce()).get("User3");
        verify(user3, atLeastOnce()).getStorage();
        verify(user3Storage, atLeastOnce()).getGroups();
        verify(groupsMap, atLeastOnce()).values();

        Assertions.assertTrue(finder.getCachedBookmarks().containsKey("User3"));
        assertEquals(new HashSet<>(first),
                new HashSet<>(finder.getCachedBookmarks().get("User3")));

        List<Bookmark> second = finder.searchBookmarksByUser("User3", storage);
        assertEquals(new HashSet<>(first), new HashSet<>(second));
    }


    @Test
    void testSearchBookmarksByGroup_returnsMatches() {
        List<Bookmark> result = finder.searchBookmarksByGroup("Educational",
                "User1", storage);
        assertEquals(List.of(b1), result);
    }

    @Test
    void testSearchBookmarksByGroup_noMatchReturnsEmpty() {
        List<Bookmark> result = finder.searchBookmarksByGroup("NonExistingGroup",
                "User1", storage);
        Assertions.assertTrue(result.isEmpty());
    }


    @Test
    void testSearchBookmarksByTags_containsAllRequiredTags() {
        List<Bookmark> result = finder.searchBookmarksByTags("User2",
                Set.of("book", "gaming"), storage);
        assertEquals(linksOfUser2, result);
    }

    @Test
    void testSearchBookmarksByTags_missingOneTagYieldsEmpty() {
        List<Bookmark> result = finder.searchBookmarksByTags("User2",
                Set.of("book", "gaming", "extra"), storage);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testSearchBookmarksByTitle_caseInsensitiveAndPartial() {
        List<Bookmark> result = finder.searchBookmarksByTitle("User1",
                "github", storage);
        assertEquals(new HashSet<>(linksOfUser1), new HashSet<>(result));

        List<Bookmark> result2 = finder.searchBookmarksByTitle("User1",
                "Course-Git", storage);
        assertEquals(List.of(b1), result2);
    }


    @Test
    void testInvalidateUserCache_removesOnlyThatUser() {
        Assertions.assertTrue(finder.getCachedBookmarks().containsKey("User2"));
        finder.invalidateUserCache("User2");
        Assertions.assertFalse(finder.getCachedBookmarks().containsKey("User2"));
        Assertions.assertTrue(finder.getCachedBookmarks().containsKey("User1"));
    }

    @Test
    void testInvalidateUserCache_nonExistingUserIsNoOp() {
        finder.invalidateUserCache("NoSuchUser");
        Assertions.assertTrue(finder.getCachedBookmarks().containsKey("User1"));
        Assertions.assertTrue(finder.getCachedBookmarks().containsKey("User2"));
    }

    @Test
    void testConstructorsAndGetter_behaveAsExpected() {
        Map<String, List<Bookmark>> provided = new ConcurrentHashMap<>();
        BookmarksFinder f1 = new BookmarksFinder(provided);
        assertSame(provided, f1.getCachedBookmarks(),
                "The method getCachedBookmarks should return the provided map instance");

        BookmarksFinder f2 = new BookmarksFinder();
        Assertions.assertTrue(f2.getCachedBookmarks().isEmpty());
    }
}
