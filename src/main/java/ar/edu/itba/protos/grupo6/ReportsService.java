package ar.edu.itba.protos.grupo6;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lumarzo on 03/06/16.
 */
public enum ReportsService {
    INSTANCE;

    private AtomicInteger numberOfAccesses = new AtomicInteger(0);
    private AtomicInteger bytesTransfered = new AtomicInteger(0);

    public int numberOfAccesses() {
        return numberOfAccesses.get();
    }

    public int bytesTransfered() {
        return bytesTransfered.get();
    }

    public void reportTransfer(int bytes) {
        bytesTransfered.addAndGet(bytes);
    }

    public void reportAccess(int bytes) {
        numberOfAccesses.addAndGet(bytes);
    }
}
