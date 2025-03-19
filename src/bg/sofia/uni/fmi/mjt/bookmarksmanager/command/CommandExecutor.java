package bg.sofia.uni.fmi.mjt.bookmarksmanager.command;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.BookmarksManager;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.bookmark.Bookmark;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.InvalidCredentialsException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.NoSuchUserException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.UserNotLoggedInException;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

//Credits to fmi-mjt/11-network-ii/todo-list-app
public class CommandExecutor {
    private static final String INVALID_ARGUMENTS_FORMAT_MESSAGE = "Command format expected: %s. Provided: %s.";
    private static final String UNKNOWN_COMMAND_MESSAGE = "There is no such command. " +
            "Please, type '?' to see the possible commands and use this app wholesomely.";
    private static final String NOT_LOGGED_WARNING = "User must " +
            "have logged in before using the app's commands!";
    private static final String INVALID_CREDENTIALS = "Invalid user's credentials (username or password)!";
    private static final String SHORTEN_FLAG = "--shorten";
    private static final int GENERAL_ARGS_COUNT = 2;

    private static final String REGISTER_CMD = "register";
    private static final String LOGIN_CMD = "login";
    private static final String NEW_GROUP_CMD = "new-group";
    private static final String ADD_CMD = "add-to";
    private static final String REMOVE_CMD = "remove-from";
    private static final String LIST_CMD = "list";
    private static final String SEARCH_CMD = "search";
    private static final String CLEAN_UP_CMD = "cleanup";
    private static final String IMPORT_CMD = "import-from-chrome";
    private static final String DISCONNECT_CMD = "disconnect";

    private final BookmarksManager manager;

    public CommandExecutor() {
        this.manager = new BookmarksManager();
    }

    public CommandExecutor(BookmarksManager manager) {
        this.manager = manager;
    }

    public String execute(Command cmd, SocketChannel clientChannel) {

            return switch (cmd.command()) {
            case REGISTER_CMD -> registerUser(clientChannel, cmd.arguments());
            case LOGIN_CMD -> login(clientChannel, cmd.arguments());
            case NEW_GROUP_CMD -> newGroup(clientChannel, cmd.arguments());
            case ADD_CMD -> addBookmark(clientChannel, cmd.arguments());
            case REMOVE_CMD -> removeBookmark(clientChannel, cmd.arguments());
            case LIST_CMD -> list(clientChannel, cmd.arguments());
            case SEARCH_CMD -> search(clientChannel, cmd.arguments());
            case CLEAN_UP_CMD -> cleanup(clientChannel, cmd.arguments());
            case IMPORT_CMD -> importFromChrome(clientChannel, cmd.arguments());
            case DISCONNECT_CMD -> disconnectClient(clientChannel);

            default -> UNKNOWN_COMMAND_MESSAGE;
        };
    }

    private String registerUser(SocketChannel clientChannel, String[] args) {
        if (args.length != GENERAL_ARGS_COUNT) {
            return  String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                   CommandTemplate.REGISTER.getCommandValue(),  REGISTER_CMD + Arrays.toString(args));
        }
        String username = args[0];
        String password = args[1];
        return manager.register(clientChannel, username, password);
    }

    private String login(SocketChannel clientChannel, String[] args) {
        if (args.length != GENERAL_ARGS_COUNT) {
            return String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                    CommandTemplate.LOGIN.getCommandValue(), LOGIN_CMD + Arrays.toString(args));
        }
        String username = args[0];
        String password = args[1];
        try {
            return manager.login(clientChannel, username, password);
        } catch (InvalidCredentialsException| NoSuchUserException e) {
            ExceptionsLogger.logClientException(e);
            return INVALID_CREDENTIALS;
        }
    }

    private String newGroup(SocketChannel clientChannel, String[] args) {
        if (args.length != GENERAL_ARGS_COUNT - 1) {
            return String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                    CommandTemplate.NEW_GROUP.getCommandValue(), NEW_GROUP_CMD +
                            Arrays.toString(args));
        }
        return manager.createNewBookmarksGroup(clientChannel, args[0]);
    }

    private String addBookmark(SocketChannel clientChannel, String[] args) {
        if (args.length == GENERAL_ARGS_COUNT) {
           return manager.addNewBookmarkToGroup(clientChannel, args[0], args[1], false);
        }
        if (args.length == GENERAL_ARGS_COUNT + 1 && Objects.equals(args[2], SHORTEN_FLAG)) {
            return manager.addNewBookmarkToGroup(clientChannel, args[0], args[1], true);
        }
        return String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.ADD.getCommandValue(),
                ADD_CMD + Arrays.toString(args));
    }

    private String removeBookmark(SocketChannel clientChannel, String[] args)  {
      if (args.length != GENERAL_ARGS_COUNT) {
          return String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                   CommandTemplate.REMOVE.getCommandValue(),
                  REMOVE_CMD + Arrays.toString(args));
        }
       return manager.removeBookmarkFromGroup(clientChannel, args[0], args[1]);
    }

    private String list(SocketChannel clientChannel, String[] args) {
        try {
            if (args.length == 0) {
                List<Bookmark> bookmarks = manager.listAll(clientChannel);
                return "List of all bookmarks:" + bookmarks.stream().
                        map(Record::toString);
            }
            if (args.length == GENERAL_ARGS_COUNT && "--group-name".equals(args[0])) {
                List<Bookmark> bookmarks = manager.listByGroup(clientChannel, args[1]);

                return String.format("Bookmarks of group %s", args[1]) +
                        bookmarks.stream().
                                map(Record::toString).toList();
            }
        } catch (UserNotLoggedInException e) {
            ExceptionsLogger.logClientException(e);
            return NOT_LOGGED_WARNING;
        }
        return String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.LIST.getCommandValue() + " or " +
                        CommandTemplate.LIST_GROUP.getCommandValue(),
                LIST_CMD + Arrays.toString(args));
    }

    private String search(SocketChannel clientChannel, String[] args) {
        try {
            if (args.length >= GENERAL_ARGS_COUNT && "--tags".equals(args[0])) {
                Set<String> keywords = Arrays.stream(args).skip(1).collect(Collectors.toSet());
                return manager.searchByTags(clientChannel, keywords).
                        stream().map(Bookmark::toString).toList().toString();
            }
            if (args.length == GENERAL_ARGS_COUNT && "--title".equals(args[0])) {
                return manager.searchByTitle(clientChannel, args[1]).
                        stream().map(Bookmark::toString).toList().toString();
            }
        }  catch (UserNotLoggedInException e) {
            ExceptionsLogger.logClientException(e);
            return NOT_LOGGED_WARNING;
        }
        return String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                CommandTemplate.SEARCH_TAGS.getCommandValue() + "or" +
                        CommandTemplate.SEARCH_TITLE.getCommandValue(),
                SEARCH_CMD + Arrays.toString(args)) ;
    }

    private String cleanup(SocketChannel clientChannel, String[] args) {
        if (args.length != 0) {
            return String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                    CommandTemplate.CLEAN_UP.getCommandValue(), CLEAN_UP_CMD +
                            Arrays.toString(args));
        }
        return manager.cleanUp(clientChannel);
    }

    private String importFromChrome(SocketChannel clientChannel, String[] args) {
        if (args.length != 0) {
            return String.format(INVALID_ARGUMENTS_FORMAT_MESSAGE,
                    CommandTemplate.IMPORT.getCommandValue(),
                    IMPORT_CMD + Arrays.toString(args));
        }
        List<Bookmark> importedFromChrome = manager.importFromChrome(clientChannel);
        if (importedFromChrome == null || importedFromChrome.isEmpty()) {
            return "No Chrome bookmarks to be imported";
        }
        return "List of Chrome bookmarks imported: " +
                importedFromChrome.
                stream().map(Bookmark::toString).toList();
    }

    private String disconnectClient(SocketChannel clientChannel) {
        if (clientChannel == null) {
            return "No such a connection to the server!";
        }
        manager.disconnectUser(clientChannel);
        return "Client has been successfully disconnected from server.";
    }
}