package bg.sofia.uni.fmi.mjt.bookmarksmanager.command;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandCreatorTest {

    private String testUserName = "user1";
    private String testUserPassword = "passwordUser1";
    private String testUserGroup = "Group1";
    private String testUserBookmark = "Bookmark1";

    @Test
    void testGetCommandArgumentsMultipleArguments() {
        List<String> expectedResultRegister =
                List.of("register", testUserName, testUserPassword);
        List<String> result = CommandCreator.
                getCommandArguments("register user1 passwordUser1");
        assertTrue(expectedResultRegister.containsAll(result) &&
                result.containsAll(expectedResultRegister));
    }

    @Test
    void testGetCommandArgumentsWithTags() {
        List<String> expectedResultLogin =
                List.of("add-to", testUserGroup, testUserBookmark, "--shorten");
        List<String> result = CommandCreator.
                getCommandArguments("add-to Group1 Bookmark1 --shorten");
        assertTrue(expectedResultLogin.containsAll(result) &&
                result.containsAll(expectedResultLogin));
    }

    @Test
    void testGetCommandArgumentsSingleArgument() {
        List<String> result = CommandCreator.
                getCommandArguments("cleanup");

        assertTrue(result.contains("cleanup") && result.size() == 1);
    }

    @Test
    void testNewCommand() {
        Command expectedCommand = new Command("register",
                new String [] {testUserName, testUserPassword});
        Command resultCommand = CommandCreator.
                newCommand("register    user1   passwordUser1   ");
        assertEquals(resultCommand, expectedCommand);
    }

    @Test
    void testNewCommandNoArguments() {
        Command expectedCommand = new Command("import-from-chrome", new String [] {});
        Command resultCommand = CommandCreator.newCommand("import-from-chrome");
        assertEquals(resultCommand, expectedCommand);
    }

    @Test
    void testNewCommandMultipleTags() {
        Command expectedCommand = new Command("search",
                new String [] {"--tags", "tag1", "tag2", "tag3"});
        Command resultCommand = CommandCreator.
                newCommand("  search   --tags tag1 tag2    tag3  ");
        assertEquals(resultCommand, expectedCommand);
    }





    /*public static Command newCommand(String clientInput) {
        String refactoredInput = clientInput.strip().replaceAll(" +", " ");
        List<String> tokens = CommandCreator.getCommandArguments(refactoredInput);
        if (tokens.size() == 1) {
            return new Command(tokens.getFirst(), new String[0]);
        }
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);
        return new Command(tokens.getFirst(), args);
    }*/
}
