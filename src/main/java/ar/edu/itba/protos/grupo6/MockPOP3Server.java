package ar.edu.itba.protos.grupo6;

import org.apache.log4j.Logger;

/**
 * Created by lumarzo on 31/05/16.
 */
public class MockPOP3Server {

    public static String response(String s) {

        Logger.getLogger(MockPOP3Server.class.getName()).warn(s.toUpperCase());
        String command = s.toUpperCase();
        if (command.equals("CAPA\n")) {
            return capbilities();
        } else if (command.equals("QUIT\n")) {
            return quit();
        } else {
            return error();
        }

    }


    public static String greeting() {
        return "+OK WHAT IS MY PURPOSE!!!\r\n";
    }

    private static String capbilities() {
        return "";
    }

    private static String error() {
        return "-ERR\r\n";
    }

    public static String quit() {
        return "+OK OH NO!\r\n";
    }
}
