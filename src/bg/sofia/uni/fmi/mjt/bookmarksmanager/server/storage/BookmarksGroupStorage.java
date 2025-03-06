package bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.BookmarksGroup;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.NoSuchBookmarkException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.NoSuchGroupException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.outerimport.ChromeImporter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import java.net.URI;
import java.net.URISyntaxException;

public class BookmarksGroupStorage {

    //saves users bookmarks in files in JSON format
    //all the groups of ONE user
    //Key concepts to consider:
    //Serialization & Deserialization – To store and load bookmarks from a file.
    //Atomic Updates – To ensure that file modifications reflect in memory (groups map).

    private final String GROUP_FILE_PATH = "src" + File.separator +
            "bg" + File.separator + "sofia" + File.separator +
            "uni" + File.separator + "fmi" + File.separator + "mjt" +
            File.separator + "bookmarksmanager" + File.separator + "server"
            + File.separator + "storage" + File.separator + "bookmarksfiles" + File.separator;

    private final Map<String, BookmarksGroup> groups;
    private final String ownerUsername;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    public BookmarksGroupStorage(String username) {
        this.ownerUsername = username;
        this.groups = new HashMap<>();
    }

    public BookmarksGroupStorage(Map<String, BookmarksGroup> groups, String username) {
        this.groups = groups;
        this.ownerUsername = username;
    }

    public Map<String, BookmarksGroup> getGroups() {
        return groups;
    }

    public boolean containsGroup(String groupName) {
        return groups.containsKey(groupName);  //The BManager has already validated this
        //groupName so there is no need to do it here
    }

    public void createNewGroup(String groupName, String username) {
        if (groups.containsKey(groupName)) {
            throw new GroupAlreadyExistsException(String.format("A " +
                    "group with name %s already exists", groupName));
        }

        File usersDir = new File(GROUP_FILE_PATH + ownerUsername);

        if (!usersDir.exists()) {
            usersDir.mkdirs();
        }

        FileCreator.createFile(usersDir + groupName);
        groups.put(groupName, new BookmarksGroup(groupName, new HashMap<>()));
    }

    public void addNewBookmarkToGroup(Bookmark bookmark, String groupName, String username) {
        if (groupName == null || groupName.isEmpty() || groupName.isBlank() ||
                bookmark == null) {
            throw new IllegalArgumentException("Group's name/bookmark can not be null!");
        }
        if (!containsGroup(groupName)) {
            throw new NoSuchGroupException(String.format("There is no group %s.",
                    groupName));
        }
        groups.get(groupName).addNewBookmark(bookmark);
        String groupFile = GROUP_FILE_PATH + ownerUsername + File.separator + groupName;
        updateGroupFile(groupFile, List.of(bookmark), true);
    }

    public void removeBookmarkFromGroup(String bookmarkTitle, String groupName, String username) {
        if (groupName == null || groupName.isEmpty() || groupName.isBlank() ||
                bookmarkTitle == null || bookmarkTitle.isEmpty() ||
                bookmarkTitle.isBlank()) {
            throw (new IllegalArgumentException("Group name/bookmark's " +
                    "title can not be null!"));
        }
        if (!containsGroup(groupName)) {
            throw new NoSuchGroupException(String.format("There is no group %s.",
                    groupName));
        }
        Bookmark toRemove =  groups.get(groupName).getBookmarks().stream().
                filter(bookmark ->
                        bookmark.title().equalsIgnoreCase(bookmarkTitle)).
                findFirst().orElse(null);

        if (toRemove == null) {
            throw new NoSuchBookmarkException(String.format("Group %s has " +
                    "no bookmark %s to be removed!", groupName, bookmarkTitle));
        }

        groups.get(groupName).removeBookmark(toRemove);

        String groupFile = GROUP_FILE_PATH + username + File.separator + groupName;
        updateGroupFile(groupFile,
                groups.get(groupName).getBookmarks(), false);
    }

    public void cleanUp() {
        List<String> groupsToUpdate = new ArrayList<>();
        try (ExecutorService executor = Executors.newCachedThreadPool()) {
            HttpClient client = HttpClient.newBuilder().executor(executor).build();
            for (Map.Entry<String, BookmarksGroup> groupEntry: groups.entrySet()) {
                Set<Bookmark> invalidBookmarks = ConcurrentHashMap.newKeySet();
                BookmarksGroup currentGroup = groupEntry.getValue();
                  List <CompletableFuture<Void>> futuresOfInvalidBookmarks = currentGroup.getBookmarks().stream().
                            map(bookmark ->  {HttpRequest request = HttpRequest.newBuilder().
                                    uri(URI.create(bookmark.url())).build();
                                return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                                        .thenApply(HttpResponse::statusCode).thenAccept(statusCode ->
                                        { if (statusCode == 404) {invalidBookmarks.add(bookmark);}});}).toList();
                  CompletableFuture.allOf(futuresOfInvalidBookmarks.toArray(new
                          CompletableFuture[0])).thenRun(()->
                          currentGroup.getBookmarks().removeIf(invalidBookmarks::contains));
                  if (!invalidBookmarks.isEmpty()) {
                      groupsToUpdate.add(currentGroup.groupName());
                  }
            }
        }
        updateGroupsFile(groupsToUpdate, false);
    }

    public List<Bookmark> importBookmarksFromChrome() {
        Map<String, BookmarksGroup> chromeGroups = ChromeImporter.importChromeGroups();
        if (chromeGroups == null) {
            return null;   //exceptions have already been logged in the
            // methods of the ChromeImporter class, so not needed here
        }
        for (Map.Entry<String, BookmarksGroup> groupEntry: chromeGroups.entrySet()) {
            if (!groups.containsKey(groupEntry.getKey())) {
                String newGroupFile = groupEntry.getKey()+".txt";
                FileCreator.createFile(newGroupFile);
                groups.put(groupEntry.getKey(), groupEntry.getValue());
                updateGroupFile(newGroupFile, groupEntry.getValue().
                        getBookmarks(), false);
            }
        }
        return chromeGroups.values().stream().map(BookmarksGroup::
                getBookmarks).flatMap(Collection::stream).toList();

    }

    //Files' functions
    private void updateGroupFile(String fileName, List<Bookmark> bookmarks, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName,append))) {
            for (Bookmark bookmark : bookmarks) {
                writer.write(GSON.toJson(bookmark));
                writer.newLine();
            }
        } catch (IOException e) {
           ExceptionsLogger.logClientException(e);
        }
    }

    private void updateGroupsFile(List<String> groupNames, boolean append) {
      groupNames.stream()
                .forEach(groupName -> updateGroupFile(GROUP_FILE_PATH + ownerUsername + File.separator + groupName,
                        groups.get(groupName).getBookmarks(),
                        append));
    }
}
