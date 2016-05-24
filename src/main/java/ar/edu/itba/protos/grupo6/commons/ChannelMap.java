package ar.edu.itba.protos.grupo6.commons;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lumarzo on 17/05/16.
 */
public class ChannelMap {

    private Map<SocketChannel, SocketChannel> map;

    public ChannelMap() {
        this.map = new ConcurrentHashMap<>();
    }

    public void bindChannels(SocketChannel client, SocketChannel pop3) {
            map.put(client, pop3);
            map.put(pop3, client);
    }

    public SocketChannel getConnection(SocketChannel channel) {
        return map.get(channel);
    }

    public SocketChannel disconnect(SocketChannel channel) {
        SocketChannel aux;
        aux = map.get(channel);
        map.remove(channel);
        map.remove(aux);
        return aux;
    }
}
