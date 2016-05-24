package ar.edu.itba.protos.grupo6.commons;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by lumarzo on 17/05/16.
 */
public abstract class Nio implements Runnable {

    private static final int INITIAL_CAPACITY = 256;
    protected Selector selector;
    protected Nio nio;
    protected ChannelMap channelMap;


    protected Queue<Message> outbox;

    protected InetAddress hostAddress;
    protected int port;

    private Queue<ChangeRequest> changeRequests;

    private Map<SocketChannel, Queue<ByteBuffer>> pendingData;

    private String name;

    public Nio(ChannelMap channelMap, Queue<Message> outbox, InetAddress address, int port, String name) throws IOException {
        this.hostAddress = address;
        this.port = port;
        this.selector = initSelector();
        this.channelMap = channelMap;
        this.changeRequests = new ConcurrentLinkedQueue<>();
        this.pendingData = new HashMap<>();
        this.outbox = outbox;
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    protected abstract Selector initSelector() throws IOException;

    protected abstract void handleKey(SelectionKey key) throws IOException;

    protected void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int numRead;

        ChangeRequest changeRequest;
        ByteBuffer buf = (ByteBuffer) key.attachment();
        Message message;
        try {
            numRead = socketChannel.read(buf);
        } catch (IOException e) {
            key.cancel();
            socketChannel.close();
            changeRequest = new ChangeRequest(channelMap.disconnect(socketChannel), ChangeRequest.Type.DISCONNECT, 0);
            message = Message.buildMessage(nio, changeRequest, Message.Type.DISCONNECT);
            outbox.add(message);
            return;
        }

        if (numRead == -1) {
            key.channel().close();
            key.cancel();
            changeRequest = new ChangeRequest(channelMap.disconnect(socketChannel), ChangeRequest.Type.DISCONNECT, -1);
            message = Message.buildMessage(nio, changeRequest, Message.Type.DISCONNECT);
            outbox.add(message);
            return;
        }

        buf.flip();
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);

        changeRequest = new ChangeRequest(channelMap.getConnection(socketChannel), ChangeRequest.Type.CHANGEOP, SelectionKey.OP_WRITE);
        message = Message.buildMessage(nio, changeRequest, Message.Type.READ,  new String(bytes,"UTF-8"));
        outbox.add(message);

        buf.compact();

    }

    protected void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

            Queue<ByteBuffer> queue = this.pendingData.get(socketChannel);

            while (!queue.isEmpty()) {
                ByteBuffer buf = queue.peek();
                //buf.flip();
                socketChannel.write(buf);
                if (!buf.hasRemaining()) {
                    queue.remove();
                }
                buf.compact();

            }

            if (queue.isEmpty()) {
                key.interestOps(SelectionKey.OP_READ);
            }

    }


    public void send(SocketChannel socket, byte[] data, ChangeRequest.Type type, int op) {
        this.changeRequests.add(new ChangeRequest(socket, type, op));
        if (data != null) {
            synchronized (this.pendingData) {
                Queue<ByteBuffer> queue = this.pendingData.get(socket);
                if (queue == null) {
                    queue = new LinkedList<>();
                    this.pendingData.put(socket, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }

        this.selector.wakeup();
    }


    @Override
    public void run() throws RuntimeException {
        while (true) {
            try {

                for (ChangeRequest changeRequest : changeRequests) {
                    switch (changeRequest.type) {
                        case CHANGEOP:
                            SelectionKey key = changeRequest.socket.keyFor(this.selector);
                            if (key != null) {
                                System.out.println(this + ": CHANGEOP channel:" + key.channel() + " OP:" + changeRequest.ops);
                                key.interestOps(changeRequest.ops);
                                break;
                            }
                        case REGISTER:
                            register(changeRequest.socket, changeRequest.ops);
                            break;
                        case DISCONNECT:
                            changeRequest.socket.close();
                            break;
                    }
                }
                this.changeRequests.clear();

                int i = this.selector.select();
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }
                    handleKey(key);
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("AAAA");
            }
        }
    }

    protected abstract void register(SocketChannel socket, int ops) throws IOException;


    public Nio getClient() {
        return nio;
    }
}
