package transformers;

import java.util.regex.Pattern;

public class POP3Command {

	private static final String ASCIIRegex = "^[ -~]+$";
	private static final String spaceRegex = ".* {2,}.*";
	private static final String separationRegex = "\\s{1}";
	private static final String formRegex = ".*\\s$|^\\s.*";
	private static final Pattern ASCIIPattern = Pattern.compile(ASCIIRegex);
	private static final Pattern spacePattern = Pattern.compile(spaceRegex);
	private static final Pattern separationPattern = Pattern.compile(separationRegex);
	private static final Pattern formPattern = Pattern.compile(formRegex);
	
	private static final String listRegex = "LIST ^[0-9]+"; //TODO fix regex, not working properly
	private static final Pattern listPattern = Pattern.compile(listRegex);

	/*
	 * TODO validate that the user request a valid mail number.
	 * */
	public static boolean isMail(String command) {
		if(!isValid(command))
			return false;
		return listPattern.matcher(command).find();
	}
	
	private static boolean isValid(String message) {
		return ASCIIPattern.matcher(message).find()
				&& spacePattern.matcher(message).find()
				&& separationPattern.matcher(message).find()
				&& formPattern.matcher(message).find();
	}
	
	public static void main(String[] args) {
		String c = "LIST 12876";
		System.out.println(isMail(c));
	}
}
