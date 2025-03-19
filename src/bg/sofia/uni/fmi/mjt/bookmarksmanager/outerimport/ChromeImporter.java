package bg.sofia.uni.fmi.mjt.bookmarksmanager.outerimport;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.BookmarksGroup;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.HtmlTokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChromeImporter {
    private static final String WINDOWS_BOOKMARKS_PATH =  "\\AppData\\" +
            "Local\\Google\\Chrome\\User Data\\Default\\Bookmarks";
    private static final String LINUX_BOOKMARKS_PATH = "/.config/google-chrome/" +
            "Default/Bookmarks";
    private static final String MACOS_BOOKMARKS_PATH =  "/Library/Application" +
            " Support/Google/Chrome/Bookmarks";

    private static final String ROOT_GROUP_FIELD = "roots";
    private static final String URL_CHROME_FIELD = "url";
    private static final String FOLDER_CHROME_FIELD = "folder";
    private static final String BOOKMARK_NAME_CHROME_FIELD = "name";
    private static final String BOOKMARK_TYPE_CHROME_FIELD = "type";
    private static final String BOOKMARKS_LIST_FIELD = "children";

    private static final Gson gson = new Gson();
    private static final HtmlTokenizer tokenizer = new HtmlTokenizer();

    public static Map<String, BookmarksGroup> importChromeGroups() {
        Map<String, BookmarksGroup> chromeGroups = new HashMap<>();
        try {
            String chromeFile = findChromeBookmarksPath();
            if (chromeFile == null) {
                return null;
            }
            FileReader reader = new FileReader(chromeFile);
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            JsonObject roots = root.getAsJsonObject(ROOT_GROUP_FIELD);
            for (Map.Entry<String, JsonElement> entry : roots.entrySet()) {
                String groupName = entry.getKey();
                BookmarksGroup currentGroup = new BookmarksGroup(groupName,
                        new HashMap<>());
                JsonObject groupNode = entry.getValue().getAsJsonObject();
                JsonArray children = groupNode.getAsJsonArray(BOOKMARKS_LIST_FIELD);

                if (children != null) {
                    extractBookmarks(children, currentGroup);
                    chromeGroups.putIfAbsent(groupName, currentGroup);
                }
            }
        } catch (IOException e) {
            ExceptionsLogger.logClientException(e);
            return null;
        }
        return chromeGroups;
    }

    private static void extractBookmarks(JsonArray children, BookmarksGroup chromeGroup) {
        for (JsonElement element : children) {
            JsonObject bookmarkNode = element.getAsJsonObject();
            String type = bookmarkNode.get(BOOKMARK_TYPE_CHROME_FIELD).getAsString();

            if (URL_CHROME_FIELD.equals(type)) {
                String title = bookmarkNode.get(BOOKMARK_NAME_CHROME_FIELD).getAsString();
                String url = bookmarkNode.get(URL_CHROME_FIELD).getAsString();
                chromeGroup.addNewBookmark(new Bookmark(title, url, tokenizer.getKeywords(url), chromeGroup.getGroupName()));
            } else if (FOLDER_CHROME_FIELD.equals(type)) {
                JsonArray subChildren = bookmarkNode.getAsJsonArray(BOOKMARKS_LIST_FIELD);
                if (subChildren != null) {
                    extractBookmarks(subChildren, chromeGroup);
                }
            }
        }
    }

    private static String findChromeBookmarksPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (osName.contains("windows")) {
            return userHome + WINDOWS_BOOKMARKS_PATH;
        } else if (osName.contains("linux") || osName.contains("unix")) {  // Linux
            return userHome + LINUX_BOOKMARKS_PATH;
        } else if (osName.contains("mac")) {  // macOS
            return userHome + MACOS_BOOKMARKS_PATH;
        }
       ExceptionsLogger.logClientException(new UnsupportedOperationException("Unsupported" +
               " operating system " + osName));
        return null;
    }

}

