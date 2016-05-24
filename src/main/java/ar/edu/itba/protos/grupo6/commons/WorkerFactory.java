package ar.edu.itba.protos.grupo6.commons;

import java.util.concurrent.ThreadFactory;

/**
 * Created by lumarzo on 18/05/16.
 */
public class WorkerFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r);
    }
}
