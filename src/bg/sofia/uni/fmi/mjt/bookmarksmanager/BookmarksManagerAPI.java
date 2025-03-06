package bg.sofia.uni.fmi.mjt.bookmarksmanager;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Set;

public interface BookmarksManagerAPI {

    String register(SocketChannel clientChannel, String username, String password);
    String login(SocketChannel clientChannel, String username, String password);
    String createNewBookmarksGroup(SocketChannel clientChannel, String groupName);
    String addNewBookmarkToGroup(SocketChannel clientChannel, String groupName, String url, boolean isShortened);
    String removeBookmarkFromGroup(SocketChannel clientChannel, String groupName, String bookmarkTitle);
    String cleanUp(SocketChannel clientChannel);
    List<Bookmark> importFromChrome(SocketChannel clientChannel);
    List<Bookmark> listAll(SocketChannel clientChannel);
    List<Bookmark> listByGroup(SocketChannel clientChannel, String groupName);
    List<Bookmark> searchByTags(SocketChannel clientChannel, Set<String> keywords);
    List<Bookmark> searchByTitle(SocketChannel clientChannel, String title);
}
