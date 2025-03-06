package bg.sofia.uni.fmi.mjt.bookmarksmanager.command;

public enum CommandTemplate {
        REGISTER("register <username> <password>"),
        LOGIN("login <username> <password>"),
        NEW_GROUP("new-group <group-name>"),
        ADD("add-to <group-name> <bookmark> {--shorten}"),
        REMOVE("remove-from <group-name> <bookmark>"),
        LIST("list"),
        LIST_GROUP("list --group-name <group-name>"),
        SEARCH_TAGS("search --tags <tag>..."),
        SEARCH_TITLE("search --title <title>"),
        CLEAN_UP("cleanup"),
        IMPORT("import-from-chrome"),
        DISCONNECT("disconnect"),
        HELP("?");

    private final String commandValue;

    CommandTemplate(String commandValue) {
        this.commandValue = commandValue;
    }

    String getCommandValue() {
        return commandValue;
    }
}
