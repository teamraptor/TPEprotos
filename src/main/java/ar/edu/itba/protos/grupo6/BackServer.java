package ar.edu.itba.protos.grupo6;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BackServer implements Runnable{

    private Boolean listening = Boolean.TRUE;
    private int port = 0000;

    public BackServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (listening) {
                new BackServerThread(serverSocket.accept()).start();
            }
        } catch (Exception e) {
            // TODO - Do something
        }
    }
}
