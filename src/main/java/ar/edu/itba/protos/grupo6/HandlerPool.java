package ar.edu.itba.protos.grupo6;

import java.nio.channels.SelectionKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lumarzo on 27/05/16.
 */
public class HandlerPool {
    BlockingQueue<SelectionKey> inbox;

    public HandlerPool(BlockingQueue<SelectionKey> inbox, Server server) {
        this.inbox = inbox;
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 10 ; i++) {
            executor.execute(new Handler(server,inbox,"Handler_" + i));
        }
    }
}
