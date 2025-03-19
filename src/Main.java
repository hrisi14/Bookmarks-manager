import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.BookmarksGroup;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.BookmarksGroupStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    private static final String TEST_FILE_NAME = "test" + File.separator +
            "bg" + File.separator + "sofia" + File.separator +
            "uni" + File.separator + "fmi" + File.separator + "mjt" +
            File.separator + "bookmarksmanager" + File.separator + "server"
            + File.separator + "storage" + File.separator + "testStorageFile.txt";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

     public static void main(String[] args) {

         /*Map<String, BookmarksGroup> groups = new HashMap<>();
         Bookmark bookmark = new Bookmark("Ozone", "https://www.ozone.bg/",
                 Set.of("bookstore", "book", "gaming"), "OnlineStores");

         Map<String, Bookmark> bookmarks = new HashMap<>();
         bookmarks.put("Ozone", bookmark);

         BookmarksGroup group1 = new BookmarksGroup("Group1", bookmarks);
         groups.put("Group1", group1);
         BookmarksGroupStorage bookmarksGroupStorage = new BookmarksGroupStorage(groups, TEST_FILE_NAME);

        System.out.println(GSON.toJson(bookmarksGroupStorage));*/

         List<String> list1 = List.of("item1", "item2", "item3");
         List<String> list2 = List.of("item1", "item2", "item3");
         System.out.println(list1.equals(list2));

     }
}
