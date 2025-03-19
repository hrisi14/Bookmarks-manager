package bg.sofia.uni.fmi.mjt.bookmarksmanager;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.user.User;

import java.io.File;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BookmarksManagerAPI {

    String GROUP_FILE_PATH = "src" + File.separator +
            "bg" + File.separator + "sofia" + File.separator +
            "uni" + File.separator + "fmi" + File.separator + "mjt" +
            File.separator + "bookmarksmanager" + File.separator + "server"
            + File.separator + "storage" + File.separator + "bookmarksfiles"
            + File.separator + "groups-";

    String REGISTERED_USERS_FILE = "src" + File.separator +
            "bg" + File.separator + "sofia" + File.separator +
            "uni" + File.separator + "fmi" + File.separator + "mjt" +
            File.separator + "bookmarksmanager" + File.separator + "server"
            + File.separator + "storage" + File.separator + "users" + File.separator + "registeredUsers";

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
    void disconnectUser(SocketChannel clientChannel);
    Map<SocketChannel, User> getLoggedInUsers();
}
