package ar.edu.itba.protos.grupo6;

import java.nio.channels.SocketChannel;

/**
 * Created by luis on 5/26/2016.
 */
public class Connection {
    private SocketChannel pair;
    private String data;

    public Connection(SocketChannel pair) {
        this.pair = pair;
    }

    public SocketChannel getPair() {
        return pair;
    }

    public void setPair(SocketChannel pair) {
        this.pair = pair;
    }

    public String getData() {
        return data;
    }

    public void setData(String s) {
        this.data = data;
    }

    public void appendData(String s) {
        if (this.data == null) {
            this.data = s;
            return;
        }

        this.data = data + s;
    }
}
