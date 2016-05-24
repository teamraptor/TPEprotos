package ar.edu.itba.protos.grupo6.main;


import ar.edu.itba.protos.grupo6.commons.Message;
import ar.edu.itba.protos.grupo6.commons.Nio;
import ar.edu.itba.protos.grupo6.commons.Worker;
import ar.edu.itba.protos.grupo6.commons.WorkerFactory;
import ar.edu.itba.protos.grupo6.server.NioServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by lumarzo on 18/05/16.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Queue<Message> outbox = new ConcurrentLinkedQueue<>();
        Worker worker = new Worker(outbox);

        ThreadFactory workerFactory = new WorkerFactory();
        ExecutorService executor = Executors.newCachedThreadPool(workerFactory);
        executor.execute(worker);

        InetAddress address = null;
        InetAddress pop3ServerAddress = InetAddress.getByName("pop.gmx.com");
        int serverPort = 8080;
        int pop3Port = 110;

        Nio server = new NioServer(address, pop3ServerAddress, serverPort, pop3Port, outbox);
        Nio client = server.getClient();
        new Thread(server).start();
        new Thread(client).start();
    }
}
