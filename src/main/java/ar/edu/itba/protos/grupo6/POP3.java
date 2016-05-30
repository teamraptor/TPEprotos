package ar.edu.itba.protos.grupo6;

/**
 * Created by luis on 5/26/2016.
 */
public class POP3 extends Message {
    private String data;
    private boolean done;
    private boolean multiline;

    public POP3() {
        this.done = false;
        this.multiline = false;
        this.data = "";
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setMultiline() {
        this.multiline = true;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String data() {
        return data;
    }

    public boolean isDone() {
        return this.done;
    }
    public void setDone() {
        this.done = true;
    }
}
