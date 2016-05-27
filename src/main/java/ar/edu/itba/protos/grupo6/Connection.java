package ar.edu.itba.protos.grupo6;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by luis on 5/26/2016.
 */
public class Connection {
    private SocketChannel pair;
    private String data;
    private int index;



    public Connection(SocketChannel pair) {
        this.pair = pair;
        this.index = 0;
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
        this.data = s;
    }

    public void appendData(String s) {
        if (this.data == null) {
            this.data = s;
            return;
        }

        this.data = data + s;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
