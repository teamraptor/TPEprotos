package ar.edu.itba.protos.grupo6;

/**
 * Created by luis on 5/26/2016.
 */
public class Parser {


    public POP3 parse(String s) {

        String[] lines = s.split("\n");
        POP3 msg = new POP3();
        msg.setData(s);
        if (lines.length > 1) {
            if (s.contains("\r\n.\r\n")) {
                msg.setDone();
            }
        }else if (!s.equals("+OK\r\n")){
            msg.setDone();
        }





        return msg;
    }

}
