package ar.edu.itba.protos.grupo6;

import org.apache.log4j.Logger;

/**
 * debería ser una interfaz y hacer una clase POP3Parser que la implemente
 */
public class Parser {
    Logger logger;

    public Parser() {
        logger = Logger.getLogger(Parser.class.getName());
    }

    public POP3 parse(String s) {
        //String[] lines = s.split("\r\n");
        //logger.warn(lines.length);


        POP3 msg = new POP3();
        msg.setData(s);
        if (s.getBytes().length > 255) {
            if (s.contains("\r\n.\r\n")) {
                msg.setDone();
            }
        } else if (!s.equals("+OK\r\n")) {
            msg.setDone();
        }


        return msg;
    }

}
