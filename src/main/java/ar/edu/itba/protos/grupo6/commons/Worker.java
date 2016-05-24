package ar.edu.itba.protos.grupo6.commons;

import java.io.UnsupportedEncodingException;
import java.util.Queue;

/**
 * Created by lumarzo on 18/05/16.
 */
public class Worker implements Runnable {
    private Queue<Message> inbox;


    public Worker(Queue<Message> inbox) {
        this.inbox = inbox;
    }

    @Override
    public void run() {
        Message message;

        while (true) {
            while (!inbox.isEmpty()) {
                message = inbox.remove();
                this.handleMessage(message);
            }
        }
    }

    private void handleMessage(Message message) {

        ChangeRequest request = message.getRequest();
        Nio to = message.getTo();
        String data;
        switch (message.getType()) {
            case CONNECT:
                System.out.println("WORKER: CONNECT");
                to.send(request.socket, null, request.type, request.ops);
                break;
            case DISCONNECT:
                System.out.println("WORKER: DISCONNECT");
                to.send(request.socket, null, request.type, request.ops);
                break;
            case READ:
                System.out.println("WORKER: READ");
                data = message.getData();
                System.out.println(data);
                try {
                    to.send(request.socket, data.getBytes("UTF-8"), request.type, request.ops);
                }catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case WRITE:
                data = message.getData();
                System.out.println("WORKER: WRITE");
                System.out.println(data);
                try {
                    to.send(request.socket, data.getBytes("UTF-8"), request.type, request.ops);
                }catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}
