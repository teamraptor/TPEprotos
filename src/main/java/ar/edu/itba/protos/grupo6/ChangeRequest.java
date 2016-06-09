package ar.edu.itba.protos.grupo6;

import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;

/**
 * Created by Luis on 5/15/2016.
 */
public class ChangeRequest {
    private SelectableChannel channel;
    private InetSocketAddress to;
    private String data;
    private Type type;
    private int ops;

    public ChangeRequest(Type type, SelectableChannel channel, InetSocketAddress to, String data) {
        initRequest(type, 0, channel, to, data);
    }

    public ChangeRequest(Type type, SelectableChannel channel) {
        initRequest(type, 0, channel, null, "");
    }

    public ChangeRequest(Type type, int ops, SelectableChannel channel) {
        initRequest(type, ops, channel, null, "");
    }

    public ChangeRequest(Type type, int ops, SelectableChannel channel, String data) {
        initRequest(type, ops, channel, null, data);
    }

    public InetSocketAddress getTo() {
        return to;
    }

    public String getData() {
        return data;
    }

    public Type getType() {
        return type;
    }

    public SelectableChannel getChannel() {
        return channel;
    }

    public int getOps() {
        return ops;
    }

    private void initRequest(Type type, int ops, SelectableChannel channel, InetSocketAddress to, String data) {
        this.channel = channel;
        this.type = type;
        this.ops = ops;
        this.data = data;
        this.to = to;
    }


    public enum Type {
        ACCEPT, CHANGEOP, DISCONNECT, CONNECT
    }
}
