package ar.edu.itba.protos.grupo6.commons;

import java.nio.channels.SocketChannel;

/**
 * Created by Luis on 5/15/2016.
 */
public class ChangeRequest {
    public SocketChannel socket;
    public Type type;
    public int ops;

    public ChangeRequest(SocketChannel socket, Type type, int ops) {
        this.socket = socket;
        this.type = type;
        this.ops = ops;
    }

    public enum Type {
        REGISTER, CHANGEOP, DISCONNECT
    }
}
