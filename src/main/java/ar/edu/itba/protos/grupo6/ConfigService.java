package ar.edu.itba.protos.grupo6;

/**
 * Created by Tomi on 6/7/16.
 */
public class ConfigService {

    public static boolean addToMultiplexList(String username, String pop3host, String port) {
        return true;
    }

    public static boolean setDefaultServer(String pop3host, String port) { return true; }

    public static boolean removeFromMultiplexList(String username) {
        return true;
    }

    public static boolean setMultiplexing(boolean enabled) { return true; }

    public static boolean setTransformations(boolean enabled) { return true; }

}
