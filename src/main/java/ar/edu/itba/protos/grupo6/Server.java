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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by luis on 5/26/2016.
 */
public class Server implements Runnable {
    private Selector selector;
    private ExecutorService executorService;
    private Handler handler;
    private InetSocketAddress pop3;

    private Queue<ChangeRequest> requests;

    public Server(InetSocketAddress me, InetSocketAddress pop3) {
        this.handler = new Handler(this, new Parser(), new Worker());
        this.requests = new LinkedBlockingQueue<>();
        this.pop3 = pop3;
        this.executorService = Executors.newCachedThreadPool();
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

                if (!key.isValid()) {
                    key.cancel();
                    continue;
                }


                executorService.submit(() -> handler.handleKey(key));
            }

            requests.stream().forEach((request) -> this.handleRequest(request));
            requests.clear();
        }
    }

    public void changeRequest(ChangeRequest request) {
        synchronized (requests) {
            requests.offer(request);
        }
        selector.notifyAll();
    }

    private void handleRequest(ChangeRequest request) {
        switch (request.getType()) {
            case CHANGEOP:
                SelectionKey key = request.getSocket().keyFor(selector);
                key.interestOps(request.getOps());
                ((Connection) key.attachment()).setData(request.getData());
                break;
            case CONNECT:
                SocketChannel client = request.getSocket();
                SocketChannel pop3Server;
                try {
                    pop3Server = SocketChannel.open();
                    pop3Server.configureBlocking(false);
                    pop3Server.connect(pop3);
                    Connection c = new Connection(client);
                    pop3Server.register(selector, SelectionKey.OP_CONNECT, c);
                } catch (IOException e) {
                    client.keyFor(selector).cancel();
                    //close channel?
                }
                break;
            case DISCONNECT:
                SocketChannel socket = request.getSocket();
                socket.keyFor(selector).cancel();
                //close channel?
                break;
            default:

        }
    }
}
