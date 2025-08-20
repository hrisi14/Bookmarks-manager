package bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.BookmarksGroup;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.GroupAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.NoSuchBookmarkException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.NoSuchGroupException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.outerimport.ChromeImporter;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.user.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.Files.exists;

public class BookmarksGroupStorage implements Serializable {
    private static final int ERROR_STATUS_CODE = 400 ;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    //saves users bookmarks in files in JSON format
    //all the groups of ONE user
    //Key concepts to consider:
    //Serialization & Deserialization – To store and load bookmarks from a file.
    //Atomic Updates – To ensure that file modifications reflect in memory (groups map).

    private final Map<String, BookmarksGroup> groups;
    transient
    private final String fileName;



    public BookmarksGroupStorage(String fileName) {
        this.groups = new HashMap<>();
        this.fileName = fileName;

        if (!exists(Path.of(fileName))) {
            FileCreator.createFile(fileName);
        }
    }

    public BookmarksGroupStorage(Map<String, BookmarksGroup> groups, String fileName) {
        this.groups = groups;
        this.fileName = fileName;
        FileCreator.createFile(this.fileName);
    }

    public Map<String, BookmarksGroup> getGroups() {
        return groups;
    }

    public boolean containsGroup(String groupName) {
        return groups.containsKey(groupName);  //The BManager has already validated this
        //groupName so there is no need to do it here
    }

    public void createNewGroup(String groupName) {
        if (groups.containsKey(groupName)) {
            throw new GroupAlreadyExistsException(String.format("A " +
                    "group with name %s already exists", groupName));
        }

        groups.put(groupName, new BookmarksGroup(groupName, new HashMap<>()));
        //updateGroupsFile();
    }

    public void addNewBookmarkToGroup(Bookmark bookmark, String groupName) {
        if (groupName == null || groupName.isEmpty() || groupName.isBlank() ||
                bookmark == null) {
            throw new IllegalArgumentException("Group's name/bookmark can not be null!");
        }
        if (!containsGroup(groupName)) {
            throw new NoSuchGroupException(String.format("There is no group %s.",
                    groupName));
        }
        if (groups.get(groupName).getBookmarks().contains(bookmark)) {
            return;
        }
        groups.get(groupName).addNewBookmark(bookmark);
        updateGroupsFile();
    }

    public void removeBookmarkFromGroup(String bookmarkTitle, String groupName) {
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
        updateGroupsFile();
    }

    public void cleanUp() {
        try (ExecutorService executor = Executors.newCachedThreadPool();
             HttpClient client = HttpClient.newBuilder().executor(executor).
                     version(HttpClient.Version.HTTP_2).build()) {
            for (Map.Entry<String, BookmarksGroup> groupEntry : groups.entrySet()) {
                BookmarksGroup currentGroup = groupEntry.getValue();
                handleInvalidBookmarks(executor, client, currentGroup);
            }
        }
        updateGroupsFile();
    }

    public List<Bookmark> importBookmarksFromChrome() {
        Map<String, BookmarksGroup> chromeGroups = ChromeImporter.importChromeGroups();
        if (chromeGroups == null) {
            return null;   //exceptions have already been logged in the
            // methods of the ChromeImporter class, so not needed here
        }
        for (Map.Entry<String, BookmarksGroup> groupEntry: chromeGroups.entrySet()) {
            if (!groups.containsKey(groupEntry.getKey())) {
                groups.put(groupEntry.getKey(), groupEntry.getValue());
            }
        }
        updateGroupsFile();
        return chromeGroups.values().stream().map(BookmarksGroup::
                getBookmarks).flatMap(Collection::stream).toList();
    }

    public void updateGroupsFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            //for (BookmarksGroup group : groups.values()) {
            //  writer.write(GSON.toJson(group));
            //writer.newLine();

            writer.write(GSON.toJson(this));
            //}
        } catch (IOException e) {
            ExceptionsLogger.logClientException(e);
        }
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof BookmarksGroupStorage)) {
            return false;
        }
        BookmarksGroupStorage storage = (BookmarksGroupStorage) obj;

        return fileName.equals(storage.getFileName()) &&
                groups.entrySet().containsAll(storage.
                        getGroups().entrySet()) &&
                storage.getGroups().entrySet().containsAll(groups.entrySet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(groups);
    }

    private void handleInvalidBookmarks(ExecutorService executor, HttpClient client,
                                        BookmarksGroup currentGroup) {
        Set<Bookmark> invalidBookmarks = ConcurrentHashMap.newKeySet();
        List<CompletableFuture<Void>> futures = currentGroup.getBookmarks().stream()
                .map(bookmark -> CompletableFuture.supplyAsync(() -> {
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(bookmark.url())).build();
                        HttpResponse<String> response = client.send(request,
                                HttpResponse.BodyHandlers.ofString());
                        return response.statusCode();
                    } catch (IOException | InterruptedException e) {
                        ExceptionsLogger.logClientException(e);
                        return -1;
                    }
                }, executor).thenAccept(statusCode -> {
                    if (statusCode >= ERROR_STATUS_CODE) {
                        invalidBookmarks.add(bookmark);}})).toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        synchronized (currentGroup.getBookmarks()) {
            currentGroup.removeBookmarks(invalidBookmarks);
        }
    }
}
