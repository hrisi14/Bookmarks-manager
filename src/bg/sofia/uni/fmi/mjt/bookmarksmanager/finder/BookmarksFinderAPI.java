package bg.sofia.uni.fmi.mjt.bookmarksmanager.finder;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.UsersStorage;

import java.util.List;
import java.util.Set;

public interface BookmarksFinderAPI {
   //    list - извежда списък с всички линкове на потребителя
    //    list --group-name <group-name> - извежда списък с всички линкове от дадената група
    //    Търсене на bookmarks по тагове
    //    search --tags <tag> [<tag> ...]
    //    Търсене на bookmarks по заглавие
    //    search --title <title> - връща всички линкове, в чиито заглавия се среща <title>

    List<Bookmark> searchBookmarksByUser(String username, UsersStorage storage);


    List<Bookmark> searchBookmarksByGroup(String groupName, String username, UsersStorage storage);
    List<Bookmark> searchBookmarksByTags(String username, Set<String> keywords, UsersStorage storage);
    List<Bookmark> searchBookmarksByTitle(String username, String title, UsersStorage storage);
}
