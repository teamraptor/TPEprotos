package ar.edu.itba.protos.grupo6;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Tomi on 6/7/16.
 */
public enum ConfigService {
    INSTANCE;

    private Map<String, InetSocketAddress> mplexDir = new ConcurrentHashMap<String, InetSocketAddress>();
    private InetSocketAddress defaultMPlex = new InetSocketAddress("pop.gmx.com", 110);
    private AtomicBoolean transformations = new AtomicBoolean(true);
    private AtomicBoolean mplexing = new AtomicBoolean(true);
    private Map<String, String> userCredentials = new ConcurrentHashMap<String, String>();

    private ParsingStatus status;

    private enum ParsingStatus {
        ADMIN, MPLEX, MPLEXD, MPLEXU, TRANS, ERROR
    }

    public void initialize(String filename) {

        BufferedReader reader = null;
        boolean valid = false;

        try {
            reader = new BufferedReader(new FileReader(new File(filename)));
            String text = null;

            while ((text = reader.readLine()) != null) {

                if(text.charAt(0) == '#') {
                    status = toParsingStatus(text.substring(1));
                    if (status == ParsingStatus.ERROR) {
                        valid = false;
                        break;
                    }
                }
                else {
                    valid = parseLine(text);
                    if(!valid) break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!valid)
            System.out.println("[ERROR] Invalid config file");

    }

    private boolean parseLine(String line) {

        String[] tokens = line.split(" ");

        switch (status) {
            case ADMIN: {
                if (tokens.length != 2) return false;
                userCredentials.put(tokens[0], tokens[1]);
                break;
            }
            case MPLEX: {
                if (tokens.length != 1) return false;
                if (tokens[0].equalsIgnoreCase("true")) {
                    mplexing.set(true);
                } else mplexing.set(false);
                break;
            }
            case MPLEXD: {
                if (tokens.length != 2) return false;
                defaultMPlex = new InetSocketAddress(tokens[0], new Integer(tokens[1]));
                break;
            }
            case MPLEXU: {
                if (tokens.length != 3) return false;
                mplexDir.put(tokens[0], new InetSocketAddress(tokens[1], new Integer(tokens[2])));
                break;
            }
            case TRANS: {
                if (tokens.length != 1) return false;
                if (tokens[0].equalsIgnoreCase("true")) {
                    transformations.set(true);
                } else transformations.set(false);
                break;
            }
            default:
                break;
        }

        return true;
    }

    private ParsingStatus toParsingStatus(String command) {

        try {
            command = command.toUpperCase();
            return ParsingStatus.valueOf(command);
        } catch (Exception e) {
            return ParsingStatus.ERROR;
        }

    }

    public boolean addToMultiplexList(String username, String pop3host, String port) {
        mplexDir.put(username, new InetSocketAddress(pop3host, new Integer(port)));
        return true;
    }

    public boolean setDefaultServer(String pop3host, String port) {
        defaultMPlex = new InetSocketAddress(pop3host, new Integer(port));
        return true;
    }

    public boolean removeFromMultiplexList(String username) {
        mplexDir.remove(username);
        return true;
    }

    public boolean setMultiplexing(boolean enabled) {
        mplexing.set(enabled);
        return true;
    }

    public boolean setTransformations(boolean enabled) {
        transformations.set(enabled);
        return true;
    }

    public boolean areCredentialsValid(String username, String password) {
        String pass = userCredentials.get(username);
        if(pass == null)
            return false;
        return pass.equals(password);
    }

    public InetSocketAddress hostForUser(String user) {

        InetSocketAddress address = mplexDir.get(user);
        InetSocketAddress response = null;
        boolean mplexEnabled = mplexing.get();

        if(address == null || mplexEnabled == false) {
            synchronized (defaultMPlex) {
                response = new InetSocketAddress(defaultMPlex.getAddress(), defaultMPlex.getPort());
            }
        } else {
            synchronized (address) {
                response = new InetSocketAddress(address.getAddress(), address.getPort());
            }
        }

        return response;
    }

    public boolean transformationsEnabled() {
        return transformations.get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MPLEXU: ").append(mplexDir).append("\nMPLEXD: ").append(defaultMPlex).append("\nTRANS: ").append(transformations);
        sb.append("\nMPLEX: " + mplexing).append("\nCRED: ").append(userCredentials.size());

        return sb.toString();
    }

}
