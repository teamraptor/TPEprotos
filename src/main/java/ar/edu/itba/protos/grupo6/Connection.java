package ar.edu.itba.protos.grupo6;


import java.nio.channels.SocketChannel;

/**
 * Created by luis on 5/26/2016.
 */
public class Connection {
    private SocketChannel pair;
    private StringBuilder data;
    private int index;



    public Connection(SocketChannel pair) {
        this.data = new StringBuilder();
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
        return data.toString();
    }

    public void setData(String s) {
        this.data = new StringBuilder(s);
    }

    public void appendData(String s) {
        this.data.append(s);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
