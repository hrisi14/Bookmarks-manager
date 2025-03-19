package bg.sofia.uni.fmi.mjt.bookmarksmanager.command;

import java.util.Arrays;

public record Command(String command, String[] arguments) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Command command1 = (Command) obj;
        return command.equals(command1.command) &&
                Arrays.equals(arguments, command1.arguments);
    }

    @Override
    public int hashCode() {
        return 31 * command.hashCode() + Arrays.hashCode(arguments);
    }
}
