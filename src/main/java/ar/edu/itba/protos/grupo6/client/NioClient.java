package ar.edu.itba.protos.grupo6.client;

import ar.edu.itba.protos.grupo6.commons.ChannelMap;
import ar.edu.itba.protos.grupo6.commons.Message;
import ar.edu.itba.protos.grupo6.commons.Nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Queue;

/**
 * Created by lumarzo on 17/05/16.
 */
public class NioClient extends Nio {


    public NioClient(Nio nio, ChannelMap channelMap, Queue<Message> outbox, InetAddress address, int port) throws IOException {
        super(channelMap, outbox, address, port, "POP3");
        this.nio = nio;
    }

    @Override
    protected Selector initSelector() throws IOException {
        return SelectorProvider.provider().openSelector();
    }

    @Override
    protected void handleKey(SelectionKey key) throws IOException {
        if (key.isConnectable()) {
            this.connect(key);
        } else if (key.isReadable()) {
            this.read(key);
        } else if (key.isWritable()) {
            this.write(key);
        }
    }

    @Override
    protected void register(SocketChannel socket, int ops) throws IOException {
        switch (ops) {
            case SelectionKey.OP_CONNECT:
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                socketChannel.connect(new InetSocketAddress(this.hostAddress, this.port));
                channelMap.bindChannels(socket, socketChannel);
                socketChannel.register(this.selector, SelectionKey.OP_CONNECT);

                break;
            default:
                socket.register(this.selector, ops, ByteBuffer.allocate(256));

        }
    }


    private void connect(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            return;
        }
        socketChannel.register(this.selector, SelectionKey.OP_READ, ByteBuffer.allocate(256));
    }
}
