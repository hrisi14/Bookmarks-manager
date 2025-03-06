package bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public record BookmarksGroup(String groupName,
   Map<String, Bookmark> bookmarks) {

    public void addNewBookmark(Bookmark bookmark) {
        if (bookmark != null) {
            bookmarks.put(bookmark.title(), bookmark);
        }
    }

    public void removeBookmark(Bookmark bookmark) {
        if (bookmark != null) {
            bookmarks.remove(bookmark.title());
        }
    }

    public List<Bookmark> getBookmarks() {
        return bookmarks.values().stream().toList();
    }
}
