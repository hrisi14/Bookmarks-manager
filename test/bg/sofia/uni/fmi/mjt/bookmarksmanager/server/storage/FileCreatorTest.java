package bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileCreatorTest {
    @TempDir
    Path tempDir;

    @Test
    void testCreateFileCreatesNewFileWhenMissing() throws Exception {
        Path file = tempDir.resolve("newFile.txt");
        assertFalse(Files.exists(file), "File should not exist beforehand.");

        FileCreator.createFile(file.toString());

        assertTrue(Files.exists(file), "File should be created.");
        assertTrue(Files.isRegularFile(file), "Created path should be a regular file.");
    }

    @Test
    void testCreateFileWhenAlreadyExists() throws Exception {
        Path file = tempDir.resolve("already-there.txt");
        Files.createFile(file);

        assertDoesNotThrow(() -> FileCreator.createFile(file.toString()));
        assertTrue(Files.exists(file));
        assertTrue(Files.isRegularFile(file));
    }

    @Test
    void testCreateFileThrowsRuntimeExceptionWhenParentDirectoryIsMissing() {
        Path file = tempDir.resolve("missing").resolve("deep").resolve("file.txt");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> FileCreator.createFile(file.toString()),
                "Expected RuntimeException wrapping the underlying IOException.");

        assertNotNull(ex.getCause(), "Wrapped cause should be present.");
        assertFalse(Files.exists(file));
        assertFalse(Files.exists(file.getParent()));
    }
}
