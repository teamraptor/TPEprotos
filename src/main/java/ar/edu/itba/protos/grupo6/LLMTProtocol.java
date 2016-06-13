package ar.edu.itba.protos.grupo6;

/**
 * Created by Tomi on 6/7/16.
 */
public class LLMTProtocol {

    public enum LLMTCommand {
        USER, PASS, MPLEX, TRANS, STAT, QUIT, ERROR;
    }

    public enum LLMTState {
        GREETING, AUTHORIZATION, TRANSACTION, CLOSING;
    }

    private static final String SUCCESS_PREFIX = "+OK ";
    private static final String ERROR_PREFIX = "-ERROR ";

    private static final String INVALID_SYNTAX = "invalid syntax";
    private static final String UNKNOWN_COMMAND = "unknown command";
    private static final String COMMAND_NOT_ALLOWED = "command not allowed in this state";

    private LLMTState currentState = LLMTState.GREETING;
    private String usernameToMatchWithPassword = null;
    private boolean previousCommandWasUSER = false;

    public String handleInput(String input) {

        if(currentState == LLMTState.GREETING) {
            currentState = LLMTState.AUTHORIZATION;
            return successMessageWithMessage("hi i'm a llmt server");
        }

        String response = "";
        String trimmedInput = input.trim();
        String[] parts = trimmedInput.split(" ");

        int numberOfArguments = parts.length - 1;
        LLMTCommand command = toLLMTCommandEnum(parts[0]);

        if(currentState == LLMTState.AUTHORIZATION) {

            switch (command) {

                case USER:

                    /*
                     *  Provides the username for the authentication. A PASS command must follow
                     *
                     *  @param the username
                     */

                    if(numberOfArguments != 1) {
                        return errorMessageWithMessage(INVALID_SYNTAX);
                    }

                    usernameToMatchWithPassword = parts[1];
                    previousCommandWasUSER = true;

                    response = successMessageWithMessage("password required for user " + usernameToMatchWithPassword);

                    break;

                case PASS:

                    /*
                     *  Provides the password for the authentication. A USER command must precede
                     *
                     *  @param the password
                     */

                    if(numberOfArguments != 1) {
                        return errorMessageWithMessage(INVALID_SYNTAX);
                    }

                    String password = parts[1];

                    if(previousCommandWasUSER && usernameToMatchWithPassword != null) {

                        if(ConfigService.INSTANCE.areCredentialsValid(usernameToMatchWithPassword, password)) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("hi ").append(usernameToMatchWithPassword).append(" welcome");
                            response = successMessageWithMessage(sb.toString());
                            currentState = LLMTState.TRANSACTION;
                        } else {
                            return errorMessageWithMessage("invalid username or password");
                        }

                    } else {
                        previousCommandWasUSER = false;
                        usernameToMatchWithPassword = null;

                        return errorMessageWithMessage("please supply USER first");
                    }

                    break;

                case QUIT:

                    /*
                     *  Terminates the connection with the server
                     *
                     */

                    previousCommandWasUSER = false;
                    usernameToMatchWithPassword = null;

                    response = successMessageWithMessage("bye");
                    currentState = LLMTState.CLOSING;

                    break;

                case ERROR:

                    /*
                     *  Unsupported command
                     *
                     */

                    previousCommandWasUSER = false;
                    usernameToMatchWithPassword = null;

                    return errorMessageWithMessage(UNKNOWN_COMMAND);

                default:

                    /*
                     *  Supported commands in an unsupported state
                     *
                     */

                    previousCommandWasUSER = false;
                    usernameToMatchWithPassword = null;

                    return errorMessageWithMessage(COMMAND_NOT_ALLOWED);
            }

        } else if(currentState == LLMTState.TRANSACTION) {

            switch (command) {

                case STAT: {

                    /*
                     *  Returns the desired stat
                     *
                     */

                    if (numberOfArguments < 1) {
                        return errorMessageWithMessage(INVALID_SYNTAX);
                    }

                    numberOfArguments = numberOfArguments - 1;

                    String action = parts[1];

                    if (action.equalsIgnoreCase("naccess")) {

                        if (numberOfArguments != 0) {
                            return errorMessageWithMessage(INVALID_SYNTAX);
                        }

                        response = successMessageWithMessage(Integer.toString(ReportsService.INSTANCE.numberOfAccesses()));

                    } else if (action.equalsIgnoreCase("tbytes")) {

                        if(numberOfArguments != 0) {
                            return errorMessageWithMessage(INVALID_SYNTAX);
                        }

                        response = successMessageWithMessage(Integer.toString(ReportsService.INSTANCE.bytesTransfered()));

                    } else {
                        return errorMessageWithMessage(INVALID_SYNTAX);
                    }

                    break;
                }

                case MPLEX: {

                    /*
                     *  Configures the multiplexing functionality of the server
                     *
                     */

                    if (numberOfArguments < 1) {
                        return errorMessageWithMessage(INVALID_SYNTAX);
                    }

                    numberOfArguments = numberOfArguments - 1;

                    String action = parts[1];

                    if (action.equalsIgnoreCase("A")) {

                        if (numberOfArguments != 3) {
                            return errorMessageWithMessage(INVALID_SYNTAX);
                        }

                        String username = parts[2];
                        String pop3host = parts[3];
                        String port = parts[4];

                        if (ConfigService.INSTANCE.addToMultiplexList(username, pop3host, port)) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("user ").append(username).append(" was added to the POP3 server ").append(pop3host).append(" at port ").append(port);
                            response = successMessageWithMessage(sb.toString());
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("failed adding ").append(username).append(" to the POP3 server ").append(pop3host);
                            return errorMessageWithMessage(sb.toString());
                        }

                    } else if (action.equalsIgnoreCase("R")) {

                        if (numberOfArguments != 1) {
                            return errorMessageWithMessage(INVALID_SYNTAX);
                        }

                        String username = parts[2];

                        if (ConfigService.INSTANCE.removeFromMultiplexList(username)) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("user ").append(username).append(" was removed from the multiplexing list");
                            response = successMessageWithMessage(sb.toString());
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("failed removing ").append(username).append(" from the multiplexing list");
                            return errorMessageWithMessage(sb.toString());
                        }

                    } else if (action.equalsIgnoreCase("D")) {

                        if (numberOfArguments != 2) {
                            return errorMessageWithMessage(INVALID_SYNTAX);
                        }

                        String pop3host = parts[2];
                        String port = parts[3];

                        if (ConfigService.INSTANCE.setDefaultServer(pop3host, port)) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(pop3host).append(" was set as the default POP3 server at port ").append(port);
                            response = successMessageWithMessage(sb.toString());
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("failed setting ").append(pop3host).append(" as the default POP3 server");
                            return errorMessageWithMessage(sb.toString());
                        }

                    } else if (action.equalsIgnoreCase("ON")) {

                        if (numberOfArguments != 0) {
                            return errorMessageWithMessage(INVALID_SYNTAX);
                        }

                        if (ConfigService.INSTANCE.setMultiplexing(true)) {
                            response = successMessageWithMessage("multiplexing is now enabled");
                        } else {
                            return errorMessageWithMessage("failed enabling multiplexing");
                        }

                    } else if (action.equalsIgnoreCase("OFF")) {

                        if (numberOfArguments != 0) {
                            return errorMessageWithMessage(INVALID_SYNTAX);
                        }

                        if (ConfigService.INSTANCE.setMultiplexing(false)) {
                            response = successMessageWithMessage("multiplexing is now disabled");
                        } else {
                            return errorMessageWithMessage("failed disabling multiplexing");
                        }

                    } else {
                        return errorMessageWithMessage(INVALID_SYNTAX);
                    }

                    break;
                }
                case TRANS: {

                    /*
                     *  Configures the transformations made by the proxy
                     */

                    if (numberOfArguments < 1) {
                        return errorMessageWithMessage(INVALID_SYNTAX);
                    }

                    numberOfArguments = numberOfArguments - 1;

                    String action = parts[1];

                    if (action.equalsIgnoreCase("ON")) {

                        if (numberOfArguments != 0) {
                            return errorMessageWithMessage(INVALID_SYNTAX);
                        }

                        if (ConfigService.INSTANCE.setTransformations(true)) {
                            response = successMessageWithMessage("transformations are now enabled");
                        } else {
                            return errorMessageWithMessage("failed enabling transformations");
                        }

                    } else if (action.equalsIgnoreCase("OFF")) {

                        if (numberOfArguments != 0) {
                            return errorMessageWithMessage(INVALID_SYNTAX);
                        }

                        if (ConfigService.INSTANCE.setTransformations(false)) {
                            response = successMessageWithMessage("transformations are now disabled");
                        } else {
                            return errorMessageWithMessage("failed disabling transformations");
                        }

                    } else {
                        return errorMessageWithMessage(INVALID_SYNTAX);
                    }

                    break;
                }
                case QUIT:

                    /*
                     *  Terminates the connection with the server
                     *
                     */

                    response = successMessageWithMessage("bye");
                    currentState = LLMTState.CLOSING;

                    break;

                case ERROR:

                    /*
                     *  Unsupported command
                     *
                     */

                    return errorMessageWithMessage(UNKNOWN_COMMAND);

                default:

                    /*
                     *  Supported commands in an unsupported state
                     *
                     */

                    return errorMessageWithMessage(COMMAND_NOT_ALLOWED);
            }

        }

        return response;
    }

    public boolean shouldEndConnection() {
        return currentState == LLMTState.CLOSING;
    }

    private LLMTCommand toLLMTCommandEnum(String command) {

        try {
            command = command.toUpperCase();
            return LLMTCommand.valueOf(command);
        } catch (Exception e) {
            return LLMTCommand.ERROR;
        }

    }

    private String successMessageWithMessage(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(SUCCESS_PREFIX).append(message);
        return sb.toString();
    }

    private String errorMessageWithMessage(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(ERROR_PREFIX).append(message);
        return sb.toString();
    }

}
