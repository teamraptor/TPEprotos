package ar.edu.itba.protos.grupo6.server;


import ar.edu.itba.protos.grupo6.client.NioClient;
import ar.edu.itba.protos.grupo6.commons.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Queue;

/**
 * Created by Luis on 5/15/2016.
 */
public class NioServer extends Nio {


    public NioServer(InetAddress address, InetAddress pop3ServerAdress, int serverPort, int pop3ServerPort, Queue<Message> outbox) throws IOException {

        super(new ChannelMap(), outbox, address, serverPort, "SERVER");
        this.nio = new NioClient(this, this.channelMap, outbox, pop3ServerAdress, pop3ServerPort);

    }

    @Override
    protected Selector initSelector() throws IOException {
        Selector socketSelector = SelectorProvider.provider().openSelector();

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
        serverChannel.socket().bind(isa);


        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }

    @Override
    protected void handleKey(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            this.accept(key);
        } else if (key.isReadable()) {
            this.read(key);
        } else if (key.isWritable()) {
            this.write(key);
        }
    }


    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        //multiplexar

        // String hello = "Hello Server POP3 ok\n";
        // messenger.processData(this, socketChannel, hello.getBytes(), hello.length(), null);

        ChangeRequest changeRequest = new ChangeRequest(socketChannel, ChangeRequest.Type.REGISTER, SelectionKey.OP_CONNECT);
        Message message = Message.buildMessage(nio, changeRequest, Message.Type.CONNECT);
        outbox.add(message);

    }

    protected void register(SocketChannel socket, int ops) throws IOException {
        socket.register(this.selector, ops, ByteBuffer.allocate(256));
    }

}
