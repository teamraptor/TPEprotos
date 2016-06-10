package ar.edu.itba.protos.grupo6;

import org.apache.log4j.Logger;

/**
 * Created by lumarzo on 31/05/16.
 */
public class MockPOP3Server {

    public static String response(String s) {
        Logger logger = Logger.getLogger(MockPOP3Server.class.getName());
        String command = s.toUpperCase();

        if (command.matches("CAPA(\r)?\n")) {
            return capbilities();
        } else if (command.matches("QUIT(\r)?\n")) {
            return quit();
        } else if (command.matches("YOU PASS BUTTER(\r)?\n")) {
            return "OH MY GOD!\r\n";
        } else {
            return error();
        }

    }


    public static String greeting() {
        return "+OK WHAT IS MY PURPOSE!!!\r\n";
    }

    private static String capbilities() {
        return "+OK capabilities:\r\nUSER\r\n.\r\n";
    }

    private static String error() {
        return "-ERR\r\n";
    }

    public static String quit() {
        return "+OK BYE!\r\n";
    }
}
