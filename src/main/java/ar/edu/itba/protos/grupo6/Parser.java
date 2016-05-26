package ar.edu.itba.protos.grupo6;

/**
 * Created by luis on 5/26/2016.
 */
public class Parser {

    public POP3 parse(String s){
        POP3 msg = new POP3();
        msg.setData(s);
        return msg;
    }
}
