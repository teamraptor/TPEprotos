package transformers;

import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MIMETransformer {

	public enum State {
		HEADERS, ANALIZE, BODY, ERROR
	}

	private static String contentTypeRegex = "^(c|C)(o|O)(n|N)(t|T)(e|E)(n|N)(t|T)-(t|T)(y|Y)(p|P)(e|E): (m|M)(u|U)(l|L)(t|T)(i|I)(p|P)(a|A)(r|T)/(m|M)(i|I)(x|X)(e|E)(d|D); (b|B)(o|O)(u|U)(n|N)(d|D)(a|A)(r|R)(y|Y)=";
	private static final String subRegex = "^(s|S)(u|U)(b|B)(j|J)(e|E)(c|C)(t|T): ";
	private static final String mimeRegex = "^(m|M)(i|I)(m|M)(e|E)-(v|V)(e|E)(r|R)(s|S)(i|I)(o|O)(n|N): 1.0\r\n";
	private static final Pattern contentTypePattern = Pattern.compile(contentTypeRegex);
	private static final Pattern subPattern = Pattern.compile(subRegex);
	private static final Pattern mimePattern = Pattern.compile(mimeRegex);
	private static final String CRLF = "\r\n";
	
	private static final int spaceASCII = 32;
	private static final int tabASCII = 9;

	private static final int MAX_LINE_LENGTH = 998;

	private State state;
	private boolean isMime;
	private Queue<String> boundarys;

	public MIMETransformer() {
		state = State.HEADERS;
		isMime = false;
		boundarys = new LinkedList<String>();
	}

	public String mailTransformer(String chunk) {
		String[] lines = chunk.split(CRLF);
		for (int i = 0; i < lines.length; i++) {
			switch (state) {
			case HEADERS:

				break;
			case ANALIZE:
				if (lines[i].length() <= MAX_LINE_LENGTH) {
					if (lines[i].equals("")) {
						state = State.BODY;
					} else {
						Matcher mime = mimePattern.matcher(lines[i]);
						if (mime.find()) {
							if (isMime) {
								state = State.ERROR;
							} else {
								isMime = true;
							}
						} else {
							Matcher content = contentTypePattern.matcher(lines[i]);
							if (content.find()) {
								if (!isMime) {
									state = State.ERROR;
								} else {
									int start = content.end();
									int finish = lines[i].length();
									if (lines[i].charAt(start) == '"') {
										if (lines[i].charAt(finish) == '"') {
											start++;
											finish--;
										} else {
											state = State.ERROR;
										}
									} else {
										if (isValidBoundary(lines[i].toCharArray(), start,finish)) {
											boundarys.add(lines[i].substring(start, finish));
											state = State.HEADERS;
										} else {
											state = State.ERROR;
										}
									}
								}
							} else {
								Matcher sub = subPattern.matcher(lines[i]);
								if (sub.find()) {
									int start = sub.end();
									int finish = lines[i].length();
									String aux = L33t1f13r.l33t3r(lines[i].toCharArray(), start,finish);
									if (aux != null)
										lines[i] = aux;
								}
								state = State.HEADERS;
							}
						}
					}
					break;
				} else {
					state = State.ERROR;
				}
				break;
			default:
				break;
			}
		}
		return chunk;
	}

	private boolean isValidBoundary(char[] charArray, int start, int finish) {
		// TODO Auto-generated method stub
		return false;
	}
}
