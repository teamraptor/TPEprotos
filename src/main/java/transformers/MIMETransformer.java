package transformers;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MIMETransformer {

	public enum State {
		UNFOLDER, BODYHEADER, BODYBODY, COMMONMAIL, IMAGE, HERROR, BERROR,
	}

	private static String contentTypeRegex = "^(c|C)(o|O)(n|N)(t|T)(e|E)(n|N)(t|T)-(t|T)(y|Y)(p|P)(e|E): (m|M)(u|U)(l|L)(t|T)(i|I)(p|P)(a|A)(r|R)(t|T)/(((m|M)(i|I)(x|X)(e|E)(d|D))|((a|A)(l|L)(t|T)(e|E)(r|R)(n|N)(a|A)(t|T)(i|I)(v|V)(e|E))|((r|R)(e|E)(l|L)(a|A)(t|T)(e|E)(d|D))); (b|B)(o|O)(u|U)(n|N)(d|D)(a|A)(r|R)(y|Y)=";
	private static String imageTypeRegex = "^(c|C)(o|O)(n|N)(t|T)(e|E)(n|N)(t|T)-(t|T)(y|Y)(p|P)(e|E): (i|I)(m|M)(a|A)(g|G)(e|E)/(((j|J)(p|P)(g|G))|((j|J)(p|P)(e|E)(g|G))); ";
	private static final String subRegex = "^(s|S)(u|U)(b|B)(j|J)(e|E)(c|C)(t|T): ";
	private static final String mimeRegex = "^(m|M)(i|I)(m|M)(e|E)-(v|V)(e|E)(r|R)(s|S)(i|I)(o|O)(n|N): 1.0";
	private static String contentTransferRegex = "^(c|C)(o|O)(n|N)(t|T)(e|E)(n|N)(t|T)-(t|T)(r|R)(a|A)(n|N)(s|S)(f|F)(e|E)(r|R)-(e|E)(n|N)(c|C)(o|O)(d|D)(i|I)(n|N)(g|G): ";
	private static final Pattern contentTypePattern = Pattern.compile(contentTypeRegex);
	private static final Pattern subPattern = Pattern.compile(subRegex);
	private static final Pattern mimePattern = Pattern.compile(mimeRegex);
	private static final Pattern imageTypePattern = Pattern.compile(imageTypeRegex);
	private static final Pattern contentTransferPattern = Pattern.compile(contentTransferRegex);
	
	private static final String CRLF = "\r\n";
	private static final int spaceASCII = 32;
	private static final int tabASCII = 9;

	private static final int MAX_LINE_LENGTH = 998;
	private static final int SUBTYPE_INDEX = 20;
	private static final int UNFOLDED_LIMIT = 2048;
	private static final int BUFFER_LIMIT = 12341234;
	
	private State state;
	private boolean isMime;
	private Stack<String> boundarys;
	private StringBuilder unfolded;
	private String imageSubtype;
	private String imageEncoding;
	private StringBuilder imageBuffer;
	
	private String boundaryRegex = "^--";
	private Pattern boundaryPattern = Pattern.compile(boundaryRegex);

	public MIMETransformer() {
		state = State.UNFOLDER;
		isMime = false;
		boundarys = new Stack<>();
		unfolded = new StringBuilder();
		imageSubtype = null;
		imageEncoding = null;
		imageBuffer = new StringBuilder();
	}

	public String mailTransformer(String chunk) {
		chunk = unfolded.append(chunk).toString();
		unfolded.setLength(0);
		String[] lines = chunk.split(CRLF);
		StringBuilder ans = new StringBuilder();
		if (lines[0].length() > MAX_LINE_LENGTH) 
			state = state == State.UNFOLDER ? State.HERROR : State.BERROR;
		
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].length() > MAX_LINE_LENGTH)
				state = State.HERROR;
			switch (state) {
			case UNFOLDER:
				if (lines[i].isEmpty()) {
					analizer(ans);
					ans.append(lines[i]).append(CRLF);
					unfolded.setLength(0);
					if(!isMime) {
						state = State.COMMONMAIL;
					} else {
						state = State.BODYBODY;
					}
					break;
				} else {
					if (unfolded.length() != 0) {
						if (lines[i].charAt(0) == spaceASCII|| lines[i].charAt(0) == tabASCII) {
							unfolded.append(lines[i]);
							break;
						} else {
							analizer(ans);
							unfolded.setLength(0);
							unfolded.append(lines[i]);
							break;
						}
					} else {
						if (lines[i].charAt(0) == spaceASCII|| lines[i].charAt(0) == tabASCII) {
							state = State.HERROR;
						} else {
							unfolded.append(lines[i]);
							break;
						}
					}
				}
			case BODYHEADER:
				if(lines[i].isEmpty()) {
					bodyAnalizer(ans);
					ans.append(lines[i]).append(CRLF);
					unfolded.setLength(0);
					state = imageSubtype==null?State.BODYBODY:State.IMAGE;
					break;
				}
				if (unfolded.length() != 0) {
					if (lines[i].charAt(0) == spaceASCII || lines[i].charAt(0) == tabASCII) {
						unfolded.append(lines[i]);
						break;
					} else {
						bodyAnalizer(ans);
						unfolded.setLength(0);
						unfolded.append(lines[i]);
						break;
					}
				} else {
					if (lines[i].charAt(0) == spaceASCII || lines[i].charAt(0) == tabASCII) {
						state = State.BERROR;
					} else {
						unfolded.append(lines[i]);
						break;
					}
				}
			case BODYBODY:
				if (unfolded.length() == 0) {
					unfolded.append(lines[i]);	
				} else {
					Matcher body = boundaryPattern.matcher(unfolded);
					if(body.find()) {
						String bound = unfolded.substring(body.end());
						if (bound.equals(("--"))) {
							if(boundarys.size()!=0) {
//								System.out.println("SACO BOUNDARY: "+ boundarys.peek());
								boundarys.pop();
								if(boundarys.size()!=0) {	
									boundaryRegex = "^--"+boundarys.peek();
									boundaryPattern = Pattern.compile(boundaryRegex);
								}
							}
						} else{
							if(bound.isEmpty()) {
								boundarys.add(unfolded.substring(2)); //removes --
//								System.out.println("POPNGO BOUNDARY: " + unfolded);
								state = State.BODYHEADER;
							}
						}
					}
					ans.append(unfolded).append(CRLF);
					unfolded.setLength(0);
					unfolded.append(lines[i]);						
				}
				break;
			case COMMONMAIL:
				if(lines[i].isEmpty()){
					ans.append(unfolded).append(CRLF);
					unfolded.setLength(0);
					break;
				}
				if(unfolded.length()==0) {
					unfolded.append(lines[i]);
				} else {
					ans.append(unfolded).append(CRLF);
					unfolded.setLength(0);
					unfolded.append(lines[i]);
				}
				break;
			case IMAGE:
				if (unfolded.length() != 0) {
					Matcher body = boundaryPattern.matcher(unfolded);
					if(body.find()) {
						String bound = unfolded.substring(body.end());
						if (bound.equals(("--"))) {
							if(boundarys.size()!=0) {
//								System.out.println("SACO BOUNDARY: "+ boundarys.peek());
								boundarys.pop();
								if(boundarys.size()!=0) {	
									boundaryRegex = "^--"+boundarys.peek();
									boundaryPattern = Pattern.compile(boundaryRegex);
								}
								state = State.BODYBODY;
							}
						} else{
							if(bound.isEmpty()) {
								boundarys.add(unfolded.substring(2)); //removes --
//								System.out.println("POPNGO BOUNDARY: " + unfolded);
								state = State.BODYHEADER;
							} else {
//								System.out.println("-----------------------add1---------------------");
								unfolded.append(lines[i]);
								break;
							}
						}
//						System.out.println("-----------------------rotame---------------------");
						ans.append(imageBuffer.toString()).append(CRLF);
						imageBuffer.setLength(0);
						imageSubtype=null;
						imageEncoding=null;
						ans.append(unfolded.toString()).append(CRLF);
						unfolded.setLength(0);
						unfolded.append(lines[i]);
						state = State.BODYHEADER;
					} else {
						imageBuffer.append(unfolded);
						unfolded.setLength(0);
						unfolded.append(lines[i]);							
//						System.out.println("-----------------------add2---------------------");
					}
				} else {
//					System.out.println("-----------------------empty unfo---------------------");
					unfolded.append(lines[i]);		
				}
				break;
			case BERROR:
				ans.append(lines[i]);
				state = State.BODYBODY;
				break;
			case HERROR:
				ans.append(lines[i]);
				state = State.UNFOLDER;
			default:
				System.out.println("what?");// remove
				break;
			}
		}
		
		if (chunk.endsWith(CRLF)) 
			unfolded.append(CRLF);

		return ans.toString();
	}

	private void bodyAnalizer(StringBuilder ans) {
		Matcher content = contentTypePattern.matcher(unfolded);
		if (content.find()) {
			int start = content.end();
			int finish = unfolded.length();
			if (unfolded.charAt(start) == '"') {
				if (unfolded.charAt(finish) == '"') {
					start++;
					finish--;
				} else {
					state = State.BERROR;
				}
			}
			ans.append(unfolded).append(CRLF);
//			System.out.println("DEFINO BOUNDARY: " + unfolded.substring(start, finish));
			boundaryRegex = "^--"+unfolded.substring(start, finish);
			boundaryPattern = Pattern.compile(boundaryRegex);
		} else {
			Matcher image = imageTypePattern.matcher(unfolded);
			if (image.find()) {
				ans.append(unfolded).append(CRLF);
				imageSubtype = unfolded.substring(SUBTYPE_INDEX,image.end()-2);
				System.out.println("type: "  + imageSubtype);
			} else{
				Matcher enc = contentTransferPattern.matcher(unfolded);
				if (imageSubtype!=null && enc.find()) {
					ans.append(unfolded).append(CRLF);
					imageEncoding = unfolded.substring(enc.end());
//					System.out.println("encoding = " + imageEncoding);
				} else{
					ans.append(unfolded).append(CRLF);
				}	
			}
		}
	}

	private void analizer(StringBuilder ans) {
		if (unfolded.length() <= MAX_LINE_LENGTH) {
			Matcher mime = mimePattern.matcher(unfolded);
			if (mime.find()) {
				if (isMime) {
					state = State.HERROR;
				} else {
					isMime = true;
					ans.append(unfolded).append(CRLF);
					return;
				}
			} else {
				Matcher content = contentTypePattern.matcher(unfolded);
				if (content.find()) {
					if (!isMime) {
						state = State.HERROR;
					} else {
						int start = content.end();
						int finish = unfolded.length();
						if (unfolded.charAt(start) == '"') {
							if (unfolded.charAt(finish) == '"') {
								start++;
								finish--;
							} else {
								state = State.HERROR;
							}
						}
						ans.append(unfolded).append(CRLF);
//						System.out.println("DEFINO BOUNDARY: " + unfolded.substring(start, finish));
						boundaryRegex = "^--"+unfolded.substring(start, finish);
						boundaryPattern = Pattern.compile(boundaryRegex);
						state = State.UNFOLDER;
						return;
					}
				} else {
					Matcher sub = subPattern.matcher(unfolded);
					if (sub.find()) {
						int start = sub.end();
						int finish = unfolded.length() - 1;
						String aux = L33t1f13r.l33t3r(unfolded.toString()
								.toCharArray(), start, finish);
						if (aux != null) {
							ans.append(unfolded.substring(0, sub.end()));
							ans.append(aux).append(CRLF);
							return;
						}
					}
					ans.append(unfolded).append(CRLF);
				}
			}
		} else {
			state = State.HERROR;
		}
	}

	private String done() {
		String ans = unfolded.toString();
		unfolded.setLength(0);
		return ans;
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
				"\nContent-Type: multipart/alternative; boundary=001a114f8cb679d78e0534eed653\r\n\r\n--001a114f8",
				"cb679d78e0534eed653\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\njijij estoy probando\r\n--001a114f8",
				"cb679d78e0534eed653--\r\n--001a114f8cb679d79a0534eed655\r\nasdasd\r\n--001a114f8cb679d79a0534eed655\r\nContent-Type: image/jpeg; name=Divine-Comedy-Dante-Alighieri.jpg\r\n",
				"Content-Transfer-Encoding: base64\r\n", 
				"X-Attachment-Id: f_ip9xohsn0\r\n",
				"\r\n/9j/wAARCAHMAVkDASEAAhEBAxEB/9sAhAAKBgcIBwYKCAgICwoKCw8ZEA8NDQ8eFRcSGSQfJiUj\r\nHyMiKC05MCgqNisiIzJEMjY7PUBBQCYwRktGPks5P0A9AQ8QEBYTFiwYGCxcPTQ9XFxcXFxcXFxc\r\nXFxcXFxcXFxcXFxcXFxcXFxcXFxcXFxcXFxcX",
				"JyaCKFDIQehwKpqb+I1J/9k=\r\n","--001a114f8cb679d79a0534eed655\r\n","Content-Type: application/pdf; name=E"};
		MIMETransformer parser = new MIMETransformer();
		for (String s : test) {
			System.out.print(parser.mailTransformer(s));
			//parser.mailTransformer(s);
		}
		System.out.print(parser.done());
	}

}
