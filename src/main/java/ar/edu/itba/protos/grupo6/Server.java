package ar.edu.itba.protos.grupo6;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by luis on 5/26/2016.
 */
public class Server implements Runnable {
    private Selector selector;
    private BlockingQueue<SelectionKey> outbox;
    private InetSocketAddress pop3;

    private Queue<ChangeRequest> requests;

    public Server(InetSocketAddress me, InetSocketAddress pop3, BlockingQueue<SelectionKey> outbox) {
        this.requests = new LinkedBlockingQueue<>();
        this.outbox = outbox;
        this.pop3 = pop3;
        try {
            this.selector = initSelector(me);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Selector initSelector(InetSocketAddress me) throws IOException {
        Selector socketSelector = SelectorProvider.provider().openSelector();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(me);
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }

    @Override
    public void run() {
        while (true) {

            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                final SelectionKey key = selectedKeys.next();
                selectedKeys.remove();

                if (key.isValid()) {
                    outbox.offer(key);
                    key.interestOps(0);
                } else {
                    key.cancel();
                }


            }

            requests.stream().forEach((request) -> this.handleRequest(request));
            requests.clear();
        }
    }

    public void changeRequest(ChangeRequest request) {
        synchronized (requests) {
            requests.offer(request);
        }
        selector.wakeup();

    }

    private void handleRequest(ChangeRequest request) {
        Connection c;
        switch (request.getType()) {
            case CHANGEOP:
                SelectionKey key = request.getChannel().keyFor(selector);
                key.interestOps(request.getOps());
                c = (Connection) key.attachment();
                c.setData(request.getData());
                break;
            case CONNECT:
                SocketChannel client = (SocketChannel) request.getChannel();
                SocketChannel pop3Server = null;
                try {
                    pop3Server = SocketChannel.open();
                    pop3Server.configureBlocking(false);
                    pop3Server.connect(pop3);
                    c = new Connection(client);
                    pop3Server.register(selector, SelectionKey.OP_CONNECT, c);
                } catch (IOException e) {
                    client.keyFor(selector).cancel();
                    //close channel?
                }
                c = new Connection(pop3Server);
                try {
                    client.register(selector, 0, c);
                } catch (IOException e) {
                    client.keyFor(selector).cancel();
                    pop3Server.keyFor(selector).cancel();
                }

                break;
            case DISCONNECT:
                SocketChannel socket = (SocketChannel) request.getChannel();
                SelectionKey key1 = socket.keyFor(selector);
                c = (Connection)key1.attachment();
                SelectionKey key2 = c.getPair().keyFor(selector);

                try {
                    key1.channel().close();
                    key2.channel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                key1.cancel();
                key2.cancel();

                break;
            case ACCEPT:
                request.getChannel().keyFor(selector).interestOps(SelectionKey.OP_ACCEPT);
            default:

        }
    }
}
