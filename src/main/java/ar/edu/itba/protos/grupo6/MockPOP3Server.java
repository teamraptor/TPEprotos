package ar.edu.itba.protos.grupo6;

/**
 * Created by lumarzo on 31/05/16.
 */
public class MockPOP3Server {

    public String greeting() {
        return "+OK hello";
    }

    public String capbilities() {
        return "ERR";
    }

}
