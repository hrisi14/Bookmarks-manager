package bg.sofia.uni.fmi.mjt.bookmarksmanager.server;

import bg.sofia.uni.fmi.mjt.bookmarksmanager.BookmarksManager;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.bookmarksmanager.command.CommandExecutor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/*public class Server {
    public static final int SERVER_PORT = 7777;
    private static final int BUFFER_SIZE = 2048;
    private static final String HOST = "localhost";
    private static final String DISCONNECT_CASE = "disconnect";

    private  BookmarksManager manager;
    private  CommandExecutor commandExecutor;
    private  int port;
    private  boolean isServerWorking;

    private  ByteBuffer buffer;
    private  Selector selector;

    public Server() {
        this.port = SERVER_PORT;
        this.manager = new BookmarksManager();
        this.commandExecutor = new CommandExecutor(manager);
    }

    public Server(int port, BookmarksManager manager) {
        this.port = port;
        this.manager = manager;
        this.commandExecutor = new CommandExecutor(manager);
    }


    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;
            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        checkKeyStateCases(key);
                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    System.out.println("Error occurred while processing client request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to start server", e);
        }
    }

    public void stop() {
        isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void checkKeyStateCases(SelectionKey key) throws IOException {
        if (key.isReadable()) {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            String clientInput = getClientInput(clientChannel);
            if (clientInput == null) {
                return;
            }
            if (clientInput.equals(DISCONNECT_CASE)) {
                disconnectClient(clientChannel, key);
                return;
            }
            String output = commandExecutor.execute(CommandCreator.newCommand(clientInput), clientChannel);
            writeClientOutput(clientChannel, output);
        } else if (key.isAcceptable()) {
            accept(selector, key);
        }
    }

    private void disconnectClient(SocketChannel clientChannel, SelectionKey clientKey) throws IOException {
        clientKey.cancel();
        clientChannel.close();
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, SERVER_PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }
}*/

public class Server {
    public static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 4096;
    private static final CommandExecutor executor = new CommandExecutor();

    public static void main(String[] args) {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    // select() is blocking but may still return with 0, check javadoc
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();

                        buffer.clear();
                        int r = sc.read(buffer);
                        if (r < 0) {
                            System.out.println("Client has closed the connection");
                            sc.close();
                            continue;
                        }
                        buffer.flip();

                        byte[] clientInputBytes = new byte[buffer.remaining()];
                        buffer.get(clientInputBytes);

                       String clientInput = new String(clientInputBytes, StandardCharsets.UTF_8);

                        String output = executor.execute(CommandCreator.newCommand(clientInput), sc);

                        System.out.println("Sent to executor");

                        buffer.clear();
                        buffer.put(output.getBytes());
                        buffer.flip();
                        sc.write(buffer);

                    } else if (key.isAcceptable()) {
                        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
                        SocketChannel accept = sockChannel.accept();
                        accept.configureBlocking(false);
                        accept.register(selector, SelectionKey.OP_READ);
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the server socket", e);
        }
    }
}

