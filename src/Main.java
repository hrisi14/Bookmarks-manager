import bg.sofia.uni.fmi.mjt.bookmarksmanager.command.Command;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.server.Server;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.tokenizer.HtmlTokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {

        Command cmd = CommandCreator.newCommand("import-from-chrome ");
        System.out.println(cmd.command());
        System.out.println(Arrays.toString(cmd.arguments()));

        /*try (Reader reader = new FileReader(fileName))
        {
            HtmlTokenizer tokenizer = new HtmlTokenizer(reader);
            Set<String> keywords = tokenizer.getKeywords("https://dev.bitly.com/");
            System.out.println(keywords);
        } catch (IOException e)
        {
            throw new IllegalArgumentException("Could not find stopwords file!");
        }*/
    }
}
