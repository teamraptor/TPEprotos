package transformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class L33tSubject {

	private static final String subjectRegex = "(s|S)(u|U)(b|B)(j|J)(e|E)(c|C)(t|T): ";
	private static final Pattern subjectPattern = Pattern.compile(subjectRegex);

	public static String transformToL33t(String message) {
		int[] limits = getSubjectLimits(message);
		return l33tReplace(message.toCharArray(), limits[0], limits[1]);
	}

	private static String l33tReplace(char[] message, int start, int end) {
		for (int i = start; i < end; i++) {
			if (message[i] == 'a')
				message[i] = '4';
			if (message[i] == 'i')
				message[i] = '1';
			if (message[i] == 'o')
				message[i] = '0';
			if (message[i] == 'c')
				message[i] = '<';
		}
		
		return new String(message);
	}

	private static int[] getSubjectLimits(String message) {
		int[] limits = { 0, message.length() };
		String[] lines = message.split("\r\n");
		int size = 0;
		boolean flag = true; 
		for (int i = 0; i < lines.length && flag; i++) {
			boolean fp = true;
			Matcher aux = subjectPattern.matcher(lines[i]);
			if (aux.find()) {
				if(aux.start()!=0) {
					for (int k = 0; k < aux.start() && fp; k++) {
						if(lines[i].charAt(k)!=' ')
							fp=false;
					}
				}
				if(fp) {
					limits[0] = size + aux.end();
					limits[1] = size + lines[i].length();
					flag = false;
				}
			}
			size+=lines[i].length();
		}
		return limits;
	}
	
	public static void main(String[] args) {
		String test = "alfkj apjcas cas \n aspodkjfsañflkasfmv asa subject: aaNOLOENCUENTRES \r\nSubJecT: AHORA SI papa PAPA"; 
		System.out.println(transformToL33t(test));
	}
}
