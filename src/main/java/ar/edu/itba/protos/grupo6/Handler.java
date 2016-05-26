package ar.edu.itba.protos.grupo6;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by luis on 5/26/2016.
 */
public class Handler {
    private Server server;
    private ByteBuffer buf;
    private Parser parser;
    private Worker worker;

    public Handler(Server server, Parser parser, Worker worker) {
        this.server = server;
        this.buf = ByteBuffer.allocateDirect(4096);
        this.parser = parser;
        this.worker = worker;
    }

    public void handleKey(SelectionKey key) {
        if (key.isAcceptable()) {
            this.handleAccept(key);
        }
        if (key.isConnectable()) {
            this.handleConnect(key);
        }
        if (key.isReadable()) {
            this.handleRead(key);
        }
        if (key.isWritable()) {
            this.handleWrite(key);
        }
    }

    private void handleWrite(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Connection c = (Connection) key.attachment();
        try {
            socketChannel.write(ByteBuffer.wrap(c.getData().getBytes()));
        } catch (IOException e) {
            closeConnection(key);
        }
        buf.compact();

        if (!buf.hasRemaining()) {
            ChangeRequest read = new ChangeRequest(ChangeRequest.Type.CHANGEOP, SelectionKey.OP_READ, c.getPair());
            server.changeRequest(read);
        }

    }

    private void handleRead(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        int numRead = 0;
        try {
            numRead = socketChannel.read(buf);
        } catch (IOException e) {
            this.closeConnection(key);
        }

        if (numRead == -1) {
            this.closeConnection(key);
        }

        byte[] read = new byte[numRead];
        buf.get(read, 0, numRead);

        Connection c = (Connection) key.attachment();
        try {
            c.appendData(new String(read, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        POP3 msg = parser.parse(c.getData());
        if (msg != null) {
            msg = worker.process(msg);
        }

        if (msg != null) {
            ChangeRequest write = new ChangeRequest(ChangeRequest.Type.CHANGEOP, SelectionKey.OP_WRITE, c.getPair(), msg.data());
            server.changeRequest(write);
        }
    }

    private void closeConnection(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        SocketChannel pair = ((Connection) key.attachment()).getPair();
        ChangeRequest disconnect = new ChangeRequest(ChangeRequest.Type.DISCONNECT, 0, pair);
        server.changeRequest(disconnect);
        key.cancel();
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnect(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            closeConnection(key);
        }
        ChangeRequest register = new ChangeRequest(ChangeRequest.Type.CHANGEOP, SelectionKey.OP_READ, socketChannel);
        server.changeRequest(register);
    }

    private void handleAccept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            ChangeRequest request = new ChangeRequest(ChangeRequest.Type.CONNECT, 0, socketChannel);
            server.changeRequest(request);
        } catch (IOException e) {
            key.cancel();
        }

    }
}
