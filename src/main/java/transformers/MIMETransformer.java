package transformers;

import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MIMETransformer {

	public enum State {
		UNFOLDER, ANALIZE, BODY, ERROR
	}

	private static String contentTypeRegex = "^(c|C)(o|O)(n|N)(t|T)(e|E)(n|N)(t|T)-(t|T)(y|Y)(p|P)(e|E): (m|M)(u|U)(l|L)(t|T)(i|I)(p|P)(a|A)(r|T)/(m|M)(i|I)(x|X)(e|E)(d|D); (b|B)(o|O)(u|U)(n|N)(d|D)(a|A)(r|R)(y|Y)=";
	private static final String subRegex = "^(s|S)(u|U)(b|B)(j|J)(e|E)(c|C)(t|T): ";
	private static final String mimeRegex = "^(m|M)(i|I)(m|M)(e|E)-(v|V)(e|E)(r|R)(s|S)(i|I)(o|O)(n|N): 1.0";
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
	private String keyBoundary;
	private StringBuilder unfolded;

	public MIMETransformer() {
		state = State.UNFOLDER;
		isMime = false;
		boundarys = new LinkedList<String>();
		unfolded = new StringBuilder();
	}

	public String mailTransformer(String chunk) {
		String[] lines = chunk.split(CRLF);
		StringBuilder ans = new StringBuilder();
		if(lines[0].length() <= MAX_LINE_LENGTH) {
			lines[0] = unfolded.toString() + lines[0];
			unfolded.setLength(0);
		} else {
			state = State.ERROR;
		}
		for (int i = 0; i < lines.length; i++) {
			if(lines[i].length() > MAX_LINE_LENGTH)
				state = State.ERROR;
			switch (state) {
			case UNFOLDER:
				if (lines[i].isEmpty()) {
					analizer(ans);
					ans.append(lines[i]).append(CRLF);
					unfolded.setLength(0);
					state = State.BODY;
					break;
				} else {
					if (unfolded.length() != 0) {
						if (lines[i].charAt(0) == spaceASCII || lines[i].charAt(0) == tabASCII) {
							unfolded.append(lines[i]);
							break;
						} else {							
							analizer(ans);
							unfolded.setLength(0);
							unfolded.append(lines[i]);
							break;
							//state = State.ANALIZE;
						}
					} else {
						if (lines[i].charAt(0) == spaceASCII || lines[i].charAt(0) == tabASCII) {
							state = State.ERROR;
						} else {
							unfolded.append(lines[i]);
							break;
						}
					}
				}
			case ANALIZE:
			case BODY:
			case ERROR:
			default:
				ans.append(lines[i]);
				state = State.UNFOLDER;
			}
		}
		if(chunk.endsWith(CRLF)) {
			ans.append(unfolded).append(CRLF);
			unfolded.setLength(0);
			state = State.UNFOLDER;
		}	
		return ans.toString();
	}

	private boolean isValidBoundary(char[] charArray, int start, int finish) {
		// TODO Auto-generated method stub
		return true;
	}
	
	private void analizer(StringBuilder ans) {
		if (unfolded.length() <= MAX_LINE_LENGTH) {
				Matcher mime = mimePattern.matcher(unfolded);
				if (mime.find()) {
					if (isMime) {
						state = State.ERROR;
					} else {
						isMime = true;
						ans.append(unfolded).append(CRLF);
						return;//break;
					}
				} else {
					Matcher content = contentTypePattern.matcher(unfolded);
					if (content.find()) {
						if (!isMime) {
							state = State.ERROR;
						} else {
							int start = content.end();
							int finish = unfolded.length();
							if (unfolded.charAt(start) == '"') {
								if (unfolded.charAt(finish) == '"') {
									start++;
									finish--;
								} else {
									state = State.ERROR;
								}
							}
							ans.append(unfolded);
							if (isValidBoundary(unfolded.toString().toCharArray(), start, finish)) {
								keyBoundary = unfolded.substring(start,finish);
								state = State.UNFOLDER;
								return;
								//break;
							} else {
								state = State.ERROR;
							}
						}
					} else {
						Matcher sub = subPattern.matcher(unfolded);
						if (sub.find()) {
							int start = sub.end();
							int finish = unfolded.length()-1;
							String aux = L33t1f13r.l33t3r(unfolded.toString().toCharArray(), start,finish);
							if (aux != null) {
								ans.append(aux).append(CRLF);
								return;
								//break;
							}
						}
						ans.append(unfolded).append(CRLF);
					}
				}
			//break;
		} else {
			state = State.ERROR;
		}
	}

	public static void main(String[] args) {
		String[] test = {
				"MIME-Version: 1.0\r\nSender: fragamati@gmail.com\r\nReceived: by 10.79.120.213 with HTTP; Fri",
				", 10 Jun 2016 09:14:48 -0700 (PDT)\r\nDate: Fri, 10 Jun 2016 13:14:48 -0300\r\nDelivered-To: fragamati",
				"@gmail.com\r\nX-Google-Sender-Auth: Hn_MyIkGJA3bJQVcLO2C77y74aM\r\nMessage-ID: <CAP8gGWtNr20jzXut+dk3GSGEtb",
				"=+-Brm+rR8YgWD2PSLUa1kow@mail.gmail.com>\r\nSubject: =?UTF-8?B?UHJ1ZWLDoSBwc",
				"sO2dG9zISIjJCUmLygpIGp",
				"hamF8wrBqYcKswrQrfXsuLV86O8ORW8KoUA==?=\r\n	=?UTF-",
				"8?B?w5FuYW5kYT8=?=\r\nfrom: Matias Fraga <mati.fraga@hotmail.com>\r\nTo: Mat",
				"ias Fraga <fragamati@gmail.com>\r\nCon",
				"tent-Type: multipart/mixed; boundary=001a114",
				"f8cb679d79a0534eed655\r\n\r\n--001a114f8cb679d79a0534eed655\r",
				"\nContent-Type: multipart/alternative; boundary=001a114f8cb679d78e0534eed653\r\n--001a114f8",
				"cb679d78e0534eed653\r\nContent-Type: text/plain; charset=UTF-8\r\njijij estoy probando" };
		MIMETransformer parser = new MIMETransformer();
		for (String s : test) {
			System.out.print(parser.mailTransformer(s));
		}
	}
}
