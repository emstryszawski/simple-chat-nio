/**
 *
 *  @author Stryszawski Emil S20607
 *
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class ChatClient {

    private final String id;
    private final InetSocketAddress address;
    private final StringBuilder view;
    private final Charset charset = Charset.forName("ISO-8859-2");
    private SocketChannel channel;

    public ChatClient(String host, int port, String id) {
        this.id = id;
        address = new InetSocketAddress(host, port);
        view = new StringBuilder("=== " + id + " chat view\n");
    }

    public void login() {
        try {
            channel = SocketChannel.open();
            channel.connect(address);
            channel.configureBlocking(false);
            send("log in " + id);
        } catch (IOException e) {
            view.append("***").append(e.toString());
        }
    }

    public void logout() {
        send("log out");
    }

    public void send(String req) {
        StringBuilder response = new StringBuilder();
        try {
            ByteBuffer buffer = ByteBuffer.wrap(req.getBytes());
            channel.write(buffer);
            buffer.clear();

            int read = channel.read(buffer);

            while (read == 0) {
                read = channel.read(buffer);
            }

            while (read != 0) {
                buffer.flip();
                CharBuffer charBuffer = charset.decode(buffer);
                response.append(charBuffer);
                buffer.clear();
                read = channel.read(buffer);
            }
//            System.out.println("response = " + response);
            view.append(response);
        } catch (IOException e) {
            view.append("***").append(e.toString());
        }
    }

    public String getChatView() {
        return view.toString();
    }
}