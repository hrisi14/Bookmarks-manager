package bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class BookmarksGroup implements Serializable {

    private final String groupName;
    private final Map<String, Bookmark> bookmarks;


    public BookmarksGroup(String groupName, Map<String, Bookmark> bookmarks) {
        this.groupName = groupName;
        this.bookmarks = bookmarks;
    }
    public void addNewBookmark(Bookmark bookmark) {
        if (bookmark != null) {
            bookmarks.put(bookmark.title(), bookmark);
        }
    }

    public void removeBookmarksByUrl(java.util.Collection<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        bookmarks.entrySet().removeIf(e -> urls.contains(e.getValue().url()));
    }

    public void cleanUp() {
        bookmarks.entrySet().removeIf(e -> e.getValue().title() == null);
        System.err.println(bookmarks);
    }

    public void removeBookmark(Bookmark bookmark) {
        if (bookmark != null) {
            bookmarks.remove(bookmark.title());
        }
    }

    public void removeBookmarks(Set<Bookmark> bookmarksForRemoval) {
        for (Bookmark bookmark: bookmarksForRemoval) {
            bookmarks.remove(bookmark.title());
        }

        for (Iterator<Map.Entry<String, Bookmark>> it = bookmarks.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Bookmark> e = it.next();
            if (e.getValue().title() == null) {
                it.remove();
            }
        }
    }

    public boolean containsBookmark(String bookmarkTitle) {
        if (bookmarkTitle == null || bookmarkTitle.isBlank()
        ||bookmarkTitle.isEmpty()) {
            return false;
        }
        return bookmarks.containsKey(bookmarkTitle);
    }
    public String getGroupName() {
        return groupName;
    }

    public List<Bookmark> getBookmarks() {
        return bookmarks.values().stream().toList();
    }
}
