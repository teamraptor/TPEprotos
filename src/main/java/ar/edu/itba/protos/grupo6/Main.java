package ar.edu.itba.protos.grupo6;

import java.net.InetSocketAddress;

/**
 * Created by luis on 5/26/2016.
 */
public class Main {
    public static void main(String[] args) {

        InetSocketAddress serverAddr = new InetSocketAddress("localhost", 8080);
        InetSocketAddress pop3 = new InetSocketAddress("pop.gmx.com", 110);
        Server server = new Server(serverAddr, pop3);
        new Thread(server).run();
    }
}
