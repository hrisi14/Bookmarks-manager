package bg.sofia.uni.fmi.mjt.bookmarksmanager.finder;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.UsersStorage;
import org.junit.Test;
import org.mockito.Mockito;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BookmarksFinderTest {
    private static final BookmarksFinder finder;
    private static final UsersStorage storage = Mockito.mock();
    private static final Bookmark b1, b2, b3;
    private static final List<Bookmark> linksOfUser1, linksOfUser2;

   static {
       b1 = new Bookmark("MjtCourse-github", "https://github.com/fmi/java-course/tree/master",
               Set.of("fmi", "mjt", "java"), "Educational");
       b2 = new Bookmark("Github", "https://github.com/",
               Set.of("github", "branch", "commit"), "DevOps");
       b3 = new Bookmark("Ozone", "https://www.ozone.bg/",
               Set.of("bookstore", "book", "gaming"), "OnlineStores");

       Map<String, List<Bookmark>> cachedBookmarks = new ConcurrentHashMap<>();

       linksOfUser1 = List.of(b1,b2);
       linksOfUser2 = List.of(b3);

       cachedBookmarks.put("User1", linksOfUser1);
       cachedBookmarks.put("User2", linksOfUser2);

       finder = new BookmarksFinder(cachedBookmarks);
   }


    @Test
    public void testSearchBookmarksByUser() {
        List<Bookmark> result = finder.searchBookmarksByUser("User1", storage);
        assertTrue(result.containsAll(linksOfUser1) && linksOfUser1.containsAll(result));
    }

    @Test
    public void testSearchBookmarksByGroup() {
        List<Bookmark> result = finder.searchBookmarksByGroup("Educational","User1", storage);
        List<Bookmark> expected = List.of(b1);
        assertTrue(result.containsAll(expected) && expected.containsAll(result));
    }

    @Test
    public void testSearchBookmarksByTags() {
        List<Bookmark> result = finder.searchBookmarksByTags("User2", Set.of("book", "gaming"), storage);
        assertTrue(result.containsAll(linksOfUser2) && linksOfUser2.containsAll(result));
    }

    @Test
    public void testSearchBookmarksByTitle() {
        List<Bookmark> result = finder.searchBookmarksByTitle("User1", "github", storage);

        assertTrue(result.containsAll(linksOfUser1) && linksOfUser1.containsAll(result));
    }

    @Test
    public void testInvalidateUserCache() {
        finder.invalidateUserCache("User2");
        assertFalse(finder.getCachedBookmarks().containsKey("User2"));
    }
}
