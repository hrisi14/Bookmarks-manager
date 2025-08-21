package bg.sofia.uni.fmi.mjt.bookmarksmanager.client;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.command.CommandTemplate;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.exceptions.logger.ExceptionsLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

//Credits to: java-course/11-network-ii/snippets/echoclientserver/src/bg/sofia/uni/fmi/mjt/echo/nio
//EchoClientNio.java

public class Client {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 4096;

    private static final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    public static void main(String[] args) {

        boolean quitCommunication = false;
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the server.");

            while (!quitCommunication) {
                quitCommunication = sendClientInputToServer(scanner, socketChannel);
            }
        } catch (IOException e) {
            ExceptionsLogger.logClientException(e);
            System.out.println("A problem occurred while connecting to " +
                    "the server. Please, try again later.");
        }
    }

    private static boolean sendClientInputToServer(Scanner scanner, SocketChannel socketChannel)
            throws IOException {
        System.out.print("Enter message: ");
        String message = scanner.nextLine();
        //System.err.println(message);
        //System.err.println(CommandTemplate.HELP);
        if (CommandTemplate.HELP.getCommandValue().equals(message.trim())) {
            displayPossibleCommands();
            return false;
        }
        System.out.println("Sending message <" + message + "> to the server...");
        buffer.clear(); // switch to writing mode
        buffer.put(message.getBytes()); // buffer fill
        buffer.flip(); // switch to reading mode
        socketChannel.write(buffer); // buffer drain
        getServerMessage(socketChannel);
        if (CommandTemplate.DISCONNECT.getCommandValue().equals(message)) {
            System.out.println("Quiting communication with server.");
            socketChannel.close();
            return true;
        }
        return false;
    }

    private static void getServerMessage(SocketChannel socketChannel) throws IOException {

        buffer.clear(); // switch to writing mode
        int readBytes = socketChannel.read(buffer); // buffer fill
        if (readBytes < 0) {
            System.out.println("Server closed the connection.");
            socketChannel.close();
        }
        buffer.flip(); // switch to reading mode
        byte[] byteArray = new byte[buffer.remaining()];

        buffer.get(byteArray);
        String reply = new String(byteArray, "UTF-8"); // buffer drain
        System.out.println("The server replied <" + reply + ">");
    }

    private static void displayPossibleCommands() {
        System.out.println("Here is a list of commands to guide " +
                "you through the use of the Bookmarks Manager:");

        System.out.println("1. To register, please, enter 'register <username> <password>'.");
        System.out.println("2. In case you already have an account, please, enter 'login <username> <password>'.");
        System.out.println("3. To create a new bookmarks' group, please, enter 'new-group <group-name>'.");
        System.out.println("4. To add a new bookmark to an already existing group, please, enter 'add-to " +
                "<group-name> <bookmark> {--shorten} (optional)'.");
        System.out.println("5. To remove a bookmark from a group, please, enter 'remove-from <group-name> <bookmark>'.");
        System.out.println("6. To list all your bookmarks, please, enter 'list'.");
        System.out.println("7. To list all bookmarks of a particular group, please, enter 'list --group-name <group-name>'.");
        System.out.println("8. To search for bookmarks via tags(keywords), please, enter 'search --tags <tag> [<tag> ...]'.");
        System.out.println("9. To search for bookmarks via title, please, enter 'search --title <title>'.");
        System.out.println("10. For all invalid bookmarks' removal, please, enter 'cleanup'.");
        System.out.println("11. To import all of your Google Chrome bookmarks, please, enter 'import-from-chrome'.");

        System.out.println("To disconnect from the app, please enter 'disconnect'.");
    }
}