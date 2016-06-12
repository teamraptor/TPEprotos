package ar.edu.itba.protos.grupo6;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
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
    private Logger logger;
    private String name;

    private Queue<ChangeRequest> requests;

    public Server(InetSocketAddress me, InetSocketAddress pop3, BlockingQueue<SelectionKey> outbox) {
        this.logger = Logger.getLogger(Server.class.getName());
        this.name = "SERVER";
        this.requests = new LinkedBlockingQueue<>();
        this.outbox = outbox;
        this.pop3 = pop3;
        try {
            this.selector = initSelector(me);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
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

        boolean notFull;
        while (true) {
            logger.trace(this.name + " KEYS: " + selector.keys().size());
            logger.trace(this.name + " REQUESTS " + requests.size());
            try {
                selector.select(1000);
                logger.trace(this.name + " selected");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                final SelectionKey key = selectedKeys.next();
                selectedKeys.remove();

                if (key.isValid()) {
                    int ops = key.interestOps();
                    key.interestOps(0);
                    notFull = outbox.offer(key);
                    if (!notFull) {
                        logger.error(this.name + " outbox FULL");
                        key.interestOps(ops);
                    }
                } else {
                    logger.info(this.name + " invalid key");
                    this.closeConnection(key);
                }


            }

            Iterator<ChangeRequest> iterator = requests.iterator();
            while (iterator.hasNext()) {
                this.handleRequest(iterator.next());
                iterator.remove();
            }
        }
    }

    public void changeRequest(ChangeRequest request) {
        requests.add(request);
        selector.wakeup();
    }


    private void handleChangeOp(ChangeRequest request) {
        Connection c;
        logger.info(this.name + " CHANGEOP");
        logger.info(this.name + " " + request.getChannel());
        SelectionKey key = request.getChannel().keyFor(selector);
        if (key == null) {
            logger.warn("KEY == NULL");
            this.closeChannel(request.getChannel());
            return;
        }
        if (key.isValid()) {
            logger.info("current ops: " + key.interestOps() + " new ops: " + request.getOps());
            key.interestOps(request.getOps());
            c = (Connection) key.attachment();
            c.setData(request.getData());
        } else {
            this.closeConnection(key);
        }
    }

    private void closeChannel(SelectableChannel channel) {
        logger.info(this.name + " close channel");
        try {
            channel.close();
        } catch (IOException e2) {
            e2.printStackTrace();
            logger.error(e2.getMessage());
            return;
        }
    }

    private void handleConnect(ChangeRequest request) {
        Connection c;
        logger.info(this.name + " CONENCT");
        SocketChannel client = (SocketChannel) request.getChannel();
        InetSocketAddress to = request.getTo();

        if (to == null) {
            SelectionKey key;
            logger.info(this.name + " Connectiong Client");
            c = new Connection(null);
            c.setStatus(Connection.Status.AUTH);
            c.appendData(MockPOP3Server.greeting());
            try {
                key = client.register(selector, SelectionKey.OP_WRITE, c);

            } catch (ClosedChannelException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            }

            return;
        }
        logger.info(this.name + " Connectiong POP");
        SocketChannel pop3Server = null;
        try {
            pop3Server = SocketChannel.open();
            pop3Server.configureBlocking(false);
            pop3Server.connect(request.getTo());
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            this.closeChannel(client);
            return;
        }

        c = (Connection) client.keyFor(selector).attachment();
        c.setStatus(Connection.Status.CONENCTED);
        c.setPair(pop3Server);

        c = new Connection(client);
        c.setUser(request.getData());
        c.setStatus(Connection.Status.MULTIPLEX);
        SelectionKey popKey;
        try {
            popKey = pop3Server.register(selector, SelectionKey.OP_CONNECT, c);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            this.closeChannel(client);
            return;
        }



    }

    private void handleAccept(ChangeRequest request) {
        logger.info(this.name + " ACCEPT");
        request.getChannel().keyFor(selector).interestOps(SelectionKey.OP_ACCEPT);
    }

    private void handleDisconnect(ChangeRequest request) {
        SocketChannel channel = (SocketChannel) request.getChannel();
        SelectionKey key = channel.keyFor(selector);
        closeConnection(key);
    }

    private void closeConnection(SelectionKey key) {
        logger.info(this.name + " close connection");
        if (key == null) {
            return;
        }
        Connection c = (Connection) key.attachment();


        key.cancel();
        this.closeChannel(key.channel());
        SocketChannel channel = c.getPair();
        if (channel == null) {
            return;
        }
        SelectionKey pair = c.getPair().keyFor(selector);
        if (pair != null) {
            pair.cancel();
            this.closeChannel(pair.channel());
        }
    }


    private void handleRequest(ChangeRequest request) {

        if (request.getChannel() == null) {
            return;
        }

        if (request.getType() != ChangeRequest.Type.CONNECT && request.getChannel().keyFor(selector) == null) {
            this.closeChannel(request.getChannel());
            return;
        }
        switch (request.getType()) {
            case CHANGEOP:
                this.handleChangeOp(request);
                break;
            case CONNECT:
                this.handleConnect(request);
                break;
            case DISCONNECT:
                this.handleDisconnect(request);
                break;
            case ACCEPT:
                this.handleAccept(request);
            default:

        }
    }
}
