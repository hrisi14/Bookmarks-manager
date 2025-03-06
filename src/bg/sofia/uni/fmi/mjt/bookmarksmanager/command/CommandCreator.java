package bg.sofia.uni.fmi.mjt.bookmarksmanager.command;
import java.util.ArrayList;
import java.util.List;

public class CommandCreator {

    //Credits to fmi-mjt/11-network-ii/todo-list-app

    public static List<String> getCommandArguments(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c == ' ' && !sb.toString().isBlank()) {
                tokens.add(sb.toString());
                sb.delete(0, sb.length());
            } else {
                if (c != '\n') {  //to replace somehow with the System.lineSeparator()
                    sb.append(c);
                }
            }
        }
        tokens.add(sb.toString());
        return tokens;
    }

    public static Command newCommand(String clientInput) {
        String refactoredInput = clientInput.strip().replaceAll(" +", " ");
        List<String> tokens = CommandCreator.getCommandArguments(refactoredInput);
        if (tokens.size() == 1) {
            return new Command(tokens.getFirst(), new String[0]);
        }
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);
        return new Command(tokens.getFirst(), args);
    }
}
