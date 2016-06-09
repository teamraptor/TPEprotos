package transformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class POP3Utils {
	
	private static final String listRegex = "^RETR [0-9]+\\r\\n"; 
	private static final String userRegex = "^(u|U)(s|S)(e|E)(r|R) "; 
	private static final Pattern listPattern = Pattern.compile(listRegex);
	private static final Pattern userPattern = Pattern.compile(userRegex);

	private static final String DOT = "\r\n.\r\n";
	private static final String OK = "+OK\r\n";
	private static final int USER_COMMAND_SIZE = 6;

	public static boolean isMailComing(String command) {
		if(command.split(" ").length>2)
			return false;
		return listPattern.matcher(command).find();
	}
	
	public static boolean isOK(String responseLine) {
		return responseLine.equals(OK);
	}
	
	public static boolean isDot(String responseLine) {
		return responseLine.equals(DOT);
	}
	
	public static String getUsernameIfAvailable(String command) {
		if(command.split(" ").length>2)
			return null;
		Matcher aux = userPattern.matcher(command);
		if(command.length() > USER_COMMAND_SIZE && aux.find()) 
			return command.substring(aux.end());
		return null;
	}
}
