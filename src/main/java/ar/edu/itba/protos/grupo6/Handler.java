package ar.edu.itba.protos.grupo6;

import org.apache.log4j.Logger;
import transformers.POP3Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

/**
 * Created by luis on 5/26/2016.
 */
public class Handler implements Runnable {
    Logger logger;
    private Server server;
    private ByteBuffer buf;
    private Parser parser;
    private Worker worker;
    private BlockingQueue<SelectionKey> inbox;
    private String name;

    public Handler(Server server, BlockingQueue<SelectionKey> inbox, String name) {

        this.server = server;
        this.buf = ByteBuffer.allocateDirect((int) Math.pow(2, 20));
        this.parser = new Parser();
        this.worker = new Worker();
        this.inbox = inbox;
        this.name = name;
        this.logger = Logger.getLogger(Handler.class.getName());
    }

    @Override
    public void run() {

        while (true) {
            try {
                logger.trace(this.name + " TAKE");
                SelectionKey key = inbox.take();
                logger.trace(this.name + " HANDLING");
                handleKey(key);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void handleKey(SelectionKey key) {
        logger.info(this.name + " handling " + key.channel());
        if (key.isValid() && key.isAcceptable()) {
            logger.info(this.name + ": ACCEPT");
            this.handleAccept(key);
        } else if (key.isValid() && key.isConnectable()) {
            logger.info(this.name + ":CONNECT");
            this.handleConnect(key);
        } else if (key.isValid() && key.isReadable()) {
            logger.info(this.name + ":READ");
            this.handleRead(key);
        } else if (key.isValid() && key.isWritable()) {
            logger.info(this.name + ":WRITE");
            this.handleWrite(key);
        }
        if (!key.isValid()) {
            closeConnection(key);
        }
    }

    private void handleWrite(SelectionKey key) {
        SocketChannel socket = (SocketChannel) key.channel();
        Connection c = (Connection) key.attachment();
        byte[] data = c.getData().getBytes();
        int i = c.getIndex();
        int length = Math.min(buf.limit(), data.length - i);

        buf.put(c.getData().getBytes(), i, length);

        try {
            buf.flip();
            int numWrite = socket.write(buf);
            logger.info(this.name + " wrote " + numWrite + " bytes");
            buf.clear();
            c.setIndex(i + numWrite);
            if (c.getIndex() < data.length) {
                logger.info(this.name + " NOT DONE WRITING");
                ChangeRequest write = new ChangeRequest(ChangeRequest.Type.CHANGEOP, SelectionKey.OP_WRITE, socket, c.getData());
                server.changeRequest(write);
            } else {
                c.setIndex(0);
                logger.info(this.name + " DONE WRITING");
                logger.info(c.getData());
                if (c.getStatus() == Connection.Status.AUTH) {
                    if (c.getData().equals(MockPOP3Server.quit())) {
                        closeConnection(key);
                    }
                }
                ChangeRequest read = new ChangeRequest(ChangeRequest.Type.CHANGEOP, SelectionKey.OP_READ, socket);
                server.changeRequest(read);
                //para cerrar el otro channel en el caso de que se cierre la conexion
                read = new ChangeRequest(ChangeRequest.Type.CHANGEOP, SelectionKey.OP_READ, c.getPair());
                server.changeRequest(read);
            }

        } catch (IOException e) {
            closeConnection(key);
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    private void handleRead(SelectionKey key) {

        SocketChannel socketChannel = (SocketChannel) key.channel();

        int numRead = 0;
        try {
            numRead = socketChannel.read(buf);
            logger.info(this.name + " read " + numRead + " bytes");
        } catch (IOException e) {
            this.closeConnection(key);
            e.printStackTrace();
            logger.error(e.getMessage());
            return;
        }

        if (numRead == -1) {
            this.closeConnection(key);
            logger.debug("Other side closed connection");
            return;
        }


        buf.flip();
        byte[] read = new byte[buf.remaining()];
        buf.get(read);
        buf.clear();

        Connection c = (Connection) key.attachment();
        try {
            c.appendData(new String(read, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            closeConnection(key);
            logger.error(e.getMessage());
            return;
        }

        POP3 msg = parser.parse(c.getData());


        switch (c.getStatus()) {
            case AUTH:
                logger.info(this.name + " AUTH");
                String user = POP3Utils.getUsernameIfAvailable(c.getData());
                if (user != null) {
                    InetSocketAddress popServerAddr = Multplexer.getHost(user);
                    ChangeRequest connect = new ChangeRequest(ChangeRequest.Type.CONNECT, socketChannel, popServerAddr, msg.data());
                    server.changeRequest(connect);
                    return;

                }
                ChangeRequest write = new ChangeRequest(ChangeRequest.Type.CHANGEOP, SelectionKey.OP_WRITE, socketChannel, MockPOP3Server.response(msg.data()));
                server.changeRequest(write);
                return;
            case MULTIPLEX:
                logger.info(this.name + " MULTIPLEX");
                c.setStatus(Connection.Status.CONENCTED);
                ChangeRequest writeUser = new ChangeRequest(ChangeRequest.Type.CHANGEOP, SelectionKey.OP_WRITE, socketChannel, c.getUser());
                server.changeRequest(writeUser);
                return;
        }



        if (msg.isDone()) {
            logger.info(this.name + " DONE READING");
            msg = worker.process(msg);
            ChangeRequest write = new ChangeRequest(ChangeRequest.Type.CHANGEOP, SelectionKey.OP_WRITE, c.getPair(), msg.data());
            server.changeRequest(write);
            return;
        }
        logger.info(this.name + " NOT DONE READING");
        ChangeRequest request = new ChangeRequest(ChangeRequest.Type.CHANGEOP, SelectionKey.OP_READ, socketChannel, msg.data());
        server.changeRequest(request);
    }

    private void closeConnection(SelectionKey key) {
        logger.info(this.name + " CLOSE CONNECTION");
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ChangeRequest disconnect = new ChangeRequest(ChangeRequest.Type.DISCONNECT, 0, socketChannel);
        server.changeRequest(disconnect);
    }

    private void handleConnect(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            closeConnection(key);
            e.printStackTrace();
            logger.error(e.getMessage());
            return;
        }
        ChangeRequest register = new ChangeRequest(ChangeRequest.Type.CHANGEOP, SelectionKey.OP_READ, socketChannel);
        server.changeRequest(register);
    }

    private void handleAccept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel;
        try {
            socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            ChangeRequest request = new ChangeRequest(ChangeRequest.Type.CONNECT, 0, socketChannel);
            server.changeRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        } finally {
            ChangeRequest accept = new ChangeRequest(ChangeRequest.Type.ACCEPT, SelectionKey.OP_ACCEPT, key.channel());
            server.changeRequest(accept);
        }


    }

}
