package ar.edu.itba.protos.grupo6;

import org.apache.log4j.Logger;

import java.net.InetSocketAddress;

/**
 * Created by luis on 6/9/2016.
 */
public class Multplexer {

    public static InetSocketAddress getHost(String user) {

        Logger.getLogger(Multplexer.class.getName()).info(user);

        InetSocketAddress address = ConfigService.INSTANCE.hostForUser(user.trim());

        Logger.getLogger(Multplexer.class.getName()).info(address);

        return address;
    }
}
