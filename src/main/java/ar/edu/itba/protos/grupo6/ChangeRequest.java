package ar.edu.itba.protos.grupo6;

import java.nio.channels.SocketChannel;

/**
 * Created by Luis on 5/15/2016.
 */
public class ChangeRequest {
    private SocketChannel socket;
    private String data;
    private Type type;
    private int ops;

    public ChangeRequest(Type type, SocketChannel socket) {
        initRequest(type, 0, socket, "");
    }

    public ChangeRequest(Type type, int ops, SocketChannel socket) {
        initRequest(type, ops, socket, "");
    }

    public ChangeRequest(Type type, int ops, SocketChannel socket, String data) {
        initRequest(type, ops, socket, data);
    }

    public String getData() {
        return data;
    }

    public Type getType() {
        return type;
    }

    public SocketChannel getSocket() {
        return socket;
    }

    public int getOps() {
        return ops;
    }

    private void initRequest(Type type, int ops, SocketChannel socket, String data) {
        this.socket = socket;
        this.type = type;
        this.ops = ops;
        this.data = data;
    }

    public enum Type {
        CHANGEOP, DISCONNECT, CONNECT
    }
}
