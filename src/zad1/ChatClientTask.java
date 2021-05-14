/**
 *
 *  @author Stryszawski Emil S20607
 *
 */

package zad1;


import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<ChatClient> {

    private final ChatClient client;

    private ChatClientTask(ChatClient client, Callable<ChatClient> callable) {
        super(callable);
        this.client = client;
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
        return new ChatClientTask(c, () -> {
           c.login();
           if (wait != 0) Thread.sleep(wait);
           for (String msg : msgs) {
               c.send(msg);
               if (wait != 0) Thread.sleep(wait);
           }
           c.logout();
           if (wait != 0) Thread.sleep(wait);
           return c;
        });
    }

    public ChatClient getClient() {
        return client;
    }
}
