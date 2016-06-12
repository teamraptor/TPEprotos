package ar.edu.itba.protos.grupo6;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Tomi on 6/7/16.
 */
public class BackServerThread extends Thread{

    private Socket socket = null;

    public BackServerThread(Socket socket) {
        super();
        this.socket = socket;
    }

    @Override
    public void run(){

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine, outputLine;
            LLMTProtocol llmtProtocol = new LLMTProtocol();
            outputLine = llmtProtocol.handleInput(null);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = llmtProtocol.handleInput(inputLine);
                out.println(outputLine);
                if (llmtProtocol.shouldEndConnection())
                    break;
            }
            socket.close();

        } catch (Exception e) {
            // TODO - Do something
        }

    }

}
