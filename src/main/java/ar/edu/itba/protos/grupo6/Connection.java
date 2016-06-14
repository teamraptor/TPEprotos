package ar.edu.itba.protos.grupo6;


import transformers.MIMETransformer;

import java.nio.channels.SocketChannel;

/**
 * Created by luis on 5/26/2016.
 */
public class Connection {
    private SocketChannel pair;
    private StringBuilder data;
    private StringBuilder processedData;
    private Status status;
    private String user = "";
    private MIMETransformer parser;
    private boolean client = false;
    private boolean mailComing = false;
    public Connection(SocketChannel pair) {
        this.data = new StringBuilder();
        this.processedData = new StringBuilder();
        this.pair = pair;
        this.parser = new MIMETransformer();
    }

    public MIMETransformer getParser() {
        return parser;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public SocketChannel getPair() {
        return pair;
    }

    public void setPair(SocketChannel pair) {
        this.pair = pair;
    }

    public String getData() {
        return data.toString();
    }

    public void setData(String s) {

        this.data.setLength(0);
        this.data.append(s);
    }

    public void appendData(String s) {
        this.data.append(s);
    }


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isClient() {
        return client;
    }

    public void setClient(boolean client) {
        this.client = client;
    }

    public void setMailComing(boolean mailComing) {
        this.mailComing = mailComing;
    }

    public boolean getMailComming() {
        return this.mailComing;
    }

    public void appendProcessed(String s) {
        this.processedData.append(s);
        this.data.setLength(0);
    }

    public String getProccesedData() {
        return this.processedData.toString();
    }

    public void consumed(int numWrite) {
        System.out.println(numWrite);
        System.out.println(processedData.length());

        if (numWrite > processedData.length()) {
            processedData = new StringBuilder();
            return;
        }
        processedData = new StringBuilder(processedData.substring(numWrite));
    }

    public void resetParser() {
        this.parser = new MIMETransformer();
        this.mailComing = false;
    }

    public enum Status {
        AUTH, CONENCTED, MULTIPLEX, FOWARDING
    }
}
