package bg.sofia.uni.fmi.mjt.bookmarksmanager.finder;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.UsersStorage;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class BookmarksFinder implements BookmarksFinderAPI {

   private final Map<String, List<Bookmark>> cachedBookmarks;

    public BookmarksFinder() {
        this.cachedBookmarks = new ConcurrentHashMap<>();
    }

    public BookmarksFinder(Map<String, List<Bookmark>> cachedBookmarks) {
        this.cachedBookmarks = cachedBookmarks;
    }

    @Override
    public List<Bookmark> searchBookmarksByUser(String username, UsersStorage storage) {
        if (!cachedBookmarks.containsKey(username)) {
            List<Bookmark> updatedResult = updateUserBookmarksCache(username, storage);
            cachedBookmarks.put(username, updatedResult);
        }
        return cachedBookmarks.get(username);
    }

    @Override
    public List<Bookmark> searchBookmarksByGroup(String groupName, String username, UsersStorage storage) {
        return searchBookmarksByUser(username, storage).stream().
                filter(bookmark->bookmark.groupName().equals(groupName)).toList();
    }

    @Override
    public List<Bookmark> searchBookmarksByTags(String username, Set<String> keywords, UsersStorage storage) {
        return searchBookmarksByUser(username, storage).stream().
                filter(bookmark->bookmark.keywords().containsAll(keywords)).toList();
    }

    @Override
    public List<Bookmark> searchBookmarksByTitle(String username, String title, UsersStorage storage) {
        return searchBookmarksByUser(username, storage).stream().
                filter(bookmark->bookmark.title().toLowerCase().contains(title.toLowerCase())).toList();
    }

    public void invalidateUserCache(String username) {
        cachedBookmarks.remove(username);
    }

    public Map<String, List<Bookmark>> getCachedBookmarks() {
        return cachedBookmarks;
    }

    private List<Bookmark> updateUserBookmarksCache(String username, UsersStorage storage) {
        return storage.getUsers().get(username).getStorage().getGroups().values().stream()
                .flatMap(group -> group.getBookmarks().stream()).toList();
    }
}
