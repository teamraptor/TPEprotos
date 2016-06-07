package transformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class POP3Command {
	
	private static final String listRegex = "LIST [0-9]+\\n"; 
	private static final Pattern listPattern = Pattern.compile(listRegex);
	private static final String userRegex = "(u|U)(s|S)(e|E)(r|R) "; 
	private static final Pattern userPattern = Pattern.compile(userRegex);
	
	private static final int USER_COMMAND_SIZE = 6;

	/*
	 * TODO validate that the user request a valid mail number.
	 * */
	public static boolean isMail(final String command) {
		if(command.split(" ").length>2)
			return false;
		return listPattern.matcher(command).find();
	}
	
	public static String getUsernameIfAvailable(String command) {
		if(command.split(" ").length>2)
			return null;
		Matcher aux = userPattern.matcher(command);
		if(command.length() > USER_COMMAND_SIZE && aux.find()) {
			if(aux.start()!=0) {
				for (int k = 0; k < aux.start(); k++) {
					if(command.charAt(k)!=' ')
						return null;
				}
			}
			command = command.substring(aux.end());
			for (int i = 0; i < command.length(); i++) {
				if(command.charAt(i)==' ')
					return null;
			}
			return command;
		}
		return null;
	}
}
