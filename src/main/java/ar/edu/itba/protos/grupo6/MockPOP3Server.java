package ar.edu.itba.protos.grupo6;

/**
 * Created by lumarzo on 31/05/16.
 */
public class MockPOP3Server {

    public static String response(String s) {

        if (s.isEmpty()) {
            return greeting();
        } else if (s.toUpperCase().equals("CAPA")) {
            return capbilities();
        } else {
            return error();
        }
    }

    public static String greeting() {
        return "+OK hello\r\n";
    }

    private static String capbilities() {
        return "";
    }

    private static String error() {
        return "-ERR\r\n";
    }

}
