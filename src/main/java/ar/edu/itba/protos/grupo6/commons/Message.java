package ar.edu.itba.protos.grupo6.commons;

/**
 * Created by lumarzo on 18/05/16.
 */
public class Message {

    private Nio to;
    private ChangeRequest request;
    private Type type;
    private String data;
    private Message(Nio to, ChangeRequest request, Type type, String data) {
        this.to = to;
        this.request = request;
        this.type = type;
        this.data = data;
    }

    public static Message buildMessage(Nio to, ChangeRequest request, Type type, String data) {
        return new Message(to, request, type, data);
    }

    public static Message buildMessage(Nio to, ChangeRequest request, Type type) {
        return new Message(to, request, type, null);
    }

    public Nio getTo() {
        return to;
    }

    public ChangeRequest getRequest() {
        return request;
    }

    public String getData() {
        return data;
    }

    public Type getType() {
        return type;
    }

    public enum Type {DISCONNECT, CONNECT, READ, WRITE}
}
