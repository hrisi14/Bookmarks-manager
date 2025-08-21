package bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.storage.FileCreator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ExceptionsLogger {
    private static final String EXCEPTIONS_FILE = "src" + File.separator
            + "bg" + File.separator + "sofia" + File.separator + "uni" + File.separator
            + "fmi" + File.separator + "mjt" + File.separator + "bookmarksmanager" + File.separator +
            "exceptions" + File.separator + "logger" + File.separator + "exceptions.txt";

    public static void logClientException(Exception exception) {
        FileCreator.createFile(EXCEPTIONS_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(EXCEPTIONS_FILE,true))) {
            writer.append(exception.getMessage()).append(System.lineSeparator()).
                    append(Arrays.toString(exception.getStackTrace()));
            writer.write(System.lineSeparator());
        }  catch (IOException e) {
            throw new RuntimeException( "Unexpected error occurred while exception logging!" + e);
        }
    }

    public static void cleanUpLogs() throws IOException {
        Path path = Path.of(EXCEPTIONS_FILE);
        Files.deleteIfExists(path);
    }

    public static void logSth(int sth) {
        FileCreator.createFile(EXCEPTIONS_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(EXCEPTIONS_FILE,true))) {
            writer.append(Integer.toString(sth)).append(System.lineSeparator());
        }  catch (IOException e) {
            throw new RuntimeException( "Unexpected error occurred while exception logging!" + e);
        }
    }
}
