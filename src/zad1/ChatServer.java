/**
 *
 *  @author Stryszawski Emil S20607
 *
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class ChatServer {

    private final InetSocketAddress address;
    private Thread serverThread;
    private ServerSocketChannel channel;
    private final StringBuilder serverLog = new StringBuilder();
    private final Map<SocketChannel, String> clients = new HashMap<>();;

    public ChatServer(String host, int port) {
        address = new InetSocketAddress(host, port);
    }

    private void configureConnection() {
        try {
            channel = ServerSocketChannel.open();
            channel.socket().bind(address);
            channel.configureBlocking(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getUserFromRequest(String req) {
        return req.substring(7);
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socket = socketChannel.accept();
        socket.configureBlocking(false);
        socket.register(key.selector(), OP_READ);
    }

    private StringBuilder requestHandler(SocketChannel client, String req) throws IOException {
        StringBuilder response = new StringBuilder();
        String username = "";

        if (clients.containsKey(client))
            username = clients.get(client);

        if (req.contains("log in")) {
            username = getUserFromRequest(req);
            clients.put(client, username);

            serverLog.append(LocalTime.now()).append(" ").append(username).append(" logged in").append("\n");

            response.append(username).append(" logged in").append("\n");

        } else if (req.matches("log out")) {
            serverLog.append(LocalTime.now()).append(" ").append(username).append(" logged out").append("\n");

            response.append(username).append(" logged out").append("\n");

            clients.remove(client);
        } else {
            serverLog.append(LocalTime.now()).append(" ").append(username).append(": ").append(req).append("\n");

            response.append(username).append(": ").append(req).append("\n");
        }
        return response;
    }

    public void startServer() {
        serverThread = new Thread(() -> {
            try {
                configureConnection();

                Selector selector = Selector.open();
                channel.register(selector, OP_ACCEPT);

                while (!serverThread.isInterrupted()) {

                    selector.select();

                    Set<SelectionKey> keys = selector.selectedKeys();

                    for (Iterator<SelectionKey> it = keys.iterator(); it.hasNext();) {

                        SelectionKey key = it.next();
                        it.remove();

                        if (key.isValid()) {

                            if (key.isAcceptable()) {
                                accept(key);

                            } else if (key.isReadable()) {

                                SocketChannel clientChannel = (SocketChannel) key.channel();
                                ByteBuffer buffer = ByteBuffer.allocate(2048);
                                clientChannel.read(buffer);

                                String req = new String(buffer.array()).trim();
                                String res = requestHandler(clientChannel, req).toString();
//                                System.out.println("res = " + res);;
                                for (Map.Entry<SocketChannel, String> entry : clients.entrySet()) {
                                    ByteBuffer byteBuffer = Charset.forName("ISO-8859-2").encode(res);
                                    entry.getKey().write(byteBuffer);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        serverThread.start();
        System.out.println("Server started\n");
    }

    public void stopServer() {
        serverThread.interrupt();
        System.out.println("Server stopped");
    }

    public String getServerLog() {
        return serverLog.toString();
    }
}
