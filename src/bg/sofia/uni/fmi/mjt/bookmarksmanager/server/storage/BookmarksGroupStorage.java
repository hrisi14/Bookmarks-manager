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
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger.logSth;
import static java.nio.file.Files.exists;

public class BookmarksGroupStorage implements Serializable {
    private static final int ERROR_STATUS_CODE = 400;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    //saves users bookmarks in files in JSON format
    //all the groups of ONE user
    //Key concepts to consider:
    //Serialization & Deserialization – To store and load bookmarks from a file.
    //Atomic Updates – To ensure that file modifications reflect in memory (groups map).

    private final Map<String, BookmarksGroup> groups;
    transient
    private final String fileName;

    private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(5);
    private static final int NOT_FOUND = 400;

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
        Bookmark toRemove = groups.get(groupName).getBookmarks().stream().
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



    public List<Bookmark> importBookmarksFromChrome() {
        Map<String, BookmarksGroup> chromeGroups = ChromeImporter.importChromeGroups();
        if (chromeGroups == null) {
            return null;   //exceptions have already been logged in the
            // methods of the ChromeImporter class, so not needed here
        }
        for (Map.Entry<String, BookmarksGroup> groupEntry : chromeGroups.entrySet()) {
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


    public int cleanUp() {

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(PROBE_TIMEOUT)
                .build();

        int removedTotal = 0;

        for (BookmarksGroup group : groups.values()) {
            var snapshot = new java.util.ArrayList<>(group.getBookmarks());
            var badUrls = new java.util.HashSet<String>();

            for (Bookmark bm : snapshot) {
                int code = probeGet(client, bm.url());
                if (code >= NOT_FOUND || code == -1) {
                    badUrls.add(bm.url());
                    System.err.println("Invalid url added: " + bm.url());
                }
            }

            if (!badUrls.isEmpty()) {
                int before = group.getBookmarks().size();
                group.removeBookmarksByUrl(badUrls);
                removedTotal += (before - group.getBookmarks().size());
            }
        }

        if (removedTotal > 0) {
            updateGroupsFile();
            System.err.println("Group's file updated.");
        }
        return removedTotal;
    }

    String URL = "https://chatgpt.com/";

    private int probeGet(HttpClient client, String url) {
        final URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException e) {
            ExceptionsLogger.logClientException(e);
            System.err.println("Uri never created!");
            return -1;
        }

        try {
            HttpRequest req = HttpRequest.newBuilder(uri)
                    .GET()
                    .timeout(PROBE_TIMEOUT)
                    .build();
            HttpResponse<Void> res = client.send(req, HttpResponse.BodyHandlers.discarding());
            System.err.println("Status code: " + res.statusCode());
            return res.statusCode();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            ExceptionsLogger.logClientException(ie);
            //System.err.println("InterruptedException: " + ie.getMessage());
            return -1;
        } catch (IOException ioe) {
            ExceptionsLogger.logClientException(ioe);
            //System.err.println("IOException: " + ioe.getMessage());
            return -1;
        }
    }
}
