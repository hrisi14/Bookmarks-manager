package bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class ExceptionsLogger {
    private static final String exceptionsFile = "src" + File.separator
            + "bg" + File.separator + "sofia" + File.separator + "uni" + File.separator
            + "fmi" + File.separator + "mjt" + File.separator + "bookmarksmanager" + File.separator +
            "exceptions" + File.separator + "logger" + File.separator + "exceptions.txt";

    public static void logClientException(Exception exception) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(exceptionsFile,true))) {
            writer.append(exception.getMessage()).append(System.lineSeparator()).
                    append(Arrays.toString(exception.getStackTrace()));
            writer.write(System.lineSeparator());
        }  catch (IOException e) {
            throw new RuntimeException( "Unexpected error occurred while exception logging!" + e);
        }
    }
}
