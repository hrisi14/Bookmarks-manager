package bg.sofia.uni.fmi.mjt.bookmarksmanager.outerimport;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.BookmarksGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.FileReader;
import java.io.StringReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

public class ChromeImporterTest {

    private String originalOs;
    private String originalHome;

    @BeforeEach
    void rememberSystemProperties() {
        originalOs = System.getProperty("os.name");
        originalHome = System.getProperty("user.home");
    }

    @AfterEach
    void restoreSystemProperties() {
        if (originalOs != null) System.setProperty("os.name", originalOs);
        if (originalHome != null) System.setProperty("user.home", originalHome);
    }

    private static void setLinuxLikeOs() {
        System.setProperty("os.name", "Linux");
        System.setProperty("user.home", "/home/testuser");
    }


    private static MockedConstruction<FileReader> mockFileReaderFromString(String json) {
        return mockConstruction(FileReader.class, (mock, context) -> {
            StringReader src = new StringReader(json);
            when(mock.read(any(char[].class), anyInt(), anyInt()))
                    .thenAnswer(inv -> {
                        char[] buf = inv.getArgument(0);
                        int off = inv.getArgument(1);
                        int len = inv.getArgument(2);
                        return src.read(buf, off, len);
                    });
            doAnswer(inv -> {
                src.close();
                return null;
            }).when(mock).close();
        });
    }

    @Test
    void testImportChromeGroupsWithValidJsonFromStaticString() {
        setLinuxLikeOs();

        String json = """
            {
              "roots": {
                "bookmark_bar": {
                  "children": [
                    { "type": "url",    "name": "Google", "url": "https://www.google.com" },
                    { "type": "folder", "name": "Dev",    "children": [
                        { "type": "url", "name": "GitHub", "url": "https://github.com" }
                    ]}
                  ]
                },
                "other": { "children": [] },
                "emptyNode": { }
              }
            }
            """;

        try (MockedConstruction<FileReader> ignored = mockFileReaderFromString(json)) {
            Map<String, BookmarksGroup> groups = ChromeImporter.importChromeGroups();
            assertNotNull(groups, "Should return a non-null map");
            assertTrue(groups.containsKey("bookmark_bar"), "Should contain 'bookmark_bar' group");
            assertTrue(groups.containsKey("other"), "Should contain 'other' group");
            assertFalse(groups.containsKey("emptyNode"), "Groups without 'children' should be skipped");

            BookmarksGroup bar = groups.get("bookmark_bar");
            assertNotNull(bar);
            assertTrue(bar.containsBookmark("Google"), "Should import 'Google' bookmark");
            assertTrue(bar.containsBookmark("GitHub"),
                    "Should recurse into folders and import 'GitHub'");
            BookmarksGroup other = groups.get("other");
            assertNotNull(other);
            assertFalse(other.getBookmarks().iterator().hasNext(), "'other' should be empty");
        }
    }

    @Test
    void testImportChromeGroupsReturnsNullOnUnsupportedOs() {
        System.setProperty("os.name", "Plan9");
        System.setProperty("user.home", "/home/testuser");

        Map<String, BookmarksGroup> groups = ChromeImporter.importChromeGroups();
        assertNull(groups, "Unsupported OS should make importer return null");
    }

    @Test
    void testImportChromeGroupsReturnsNullWhenFileNotFound() {
        System.setProperty("os.name", "Linux");
        System.setProperty("user.home", "/this/definitely/does/not/exist");

        Map<String, BookmarksGroup> groups = ChromeImporter.importChromeGroups();
        assertNull(groups, "IOException at FileReader " +
                "construction should be caught and return null");
    }
}
