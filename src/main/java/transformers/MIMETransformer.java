package transformers;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MIMETransformer {

	private static final String subRegex = "^(s|S)(u|U)(b|B)(j|J)(e|E)(c|C)(t|T): ";
	private static final String mimeRegex = "^(m|M)(i|I)(m|M)(e|E)-(v|V)(e|E)(r|R)(s|S)(i|I)(o|O)(n|N): 1.0";
	private static final Pattern subPattern = Pattern.compile(subRegex);
	private static final Pattern mimePattern = Pattern.compile(mimeRegex);
	private static final String CRLF = "\r\n";
	private static final int spaceASCII = 32;
	private static final int tabASCII = 9;
	private static final int MAX_LINE_LENGTH = 998;
	private static final int SUBTYPE_INDEX = 20;
	private static final int IMAGE_LIMIT = 10 * (int) Math.pow(2, 20); //creo que son 10MiB lo que pasa es que no me acuerdo nada de arqui
	private static String contentTypeRegex = "^(c|C)(o|O)(n|N)(t|T)(e|E)(n|N)(t|T)-(t|T)(y|Y)(p|P)(e|E): (m|M)(u|U)(l|L)(t|T)(i|I)(p|P)(a|A)(r|R)(t|T)/(((m|M)(i|I)(x|X)(e|E)(d|D))|((a|A)(l|L)(t|T)(e|E)(r|R)(n|N)(a|A)(t|T)(i|I)(v|V)(e|E))|((r|R)(e|E)(l|L)(a|A)(t|T)(e|E)(d|D))); (b|B)(o|O)(u|U)(n|N)(d|D)(a|A)(r|R)(y|Y)=";
	private static final Pattern contentTypePattern = Pattern.compile(contentTypeRegex);
	private static String imageTypeRegex = "^(c|C)(o|O)(n|N)(t|T)(e|E)(n|N)(t|T)-(t|T)(y|Y)(p|P)(e|E): (i|I)(m|M)(a|A)(g|G)(e|E)/(((j|J)(p|P)(g|G))|((j|J)(p|P)(e|E)(g|G))|((p|P)(n|N)(g|G))); ";
	private static final Pattern imageTypePattern = Pattern.compile(imageTypeRegex);
	private static String contentTransferRegex = "^(c|C)(o|O)(n|N)(t|T)(e|E)(n|N)(t|T)-(t|T)(r|R)(a|A)(n|N)(s|S)(f|F)(e|E)(r|R)-(e|E)(n|N)(c|C)(o|O)(d|D)(i|I)(n|N)(g|G): ";
	private static final Pattern contentTransferPattern = Pattern.compile(contentTransferRegex);
	private State state;
	private boolean isMime;
	private Stack<String> boundarys;
	private StringBuilder unfolded;
	private String imageSubtype;
	private String imageEncoding;
	private StringBuilder imageBuffer;
	private PrintWriter p;
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
		try {
			p = new PrintWriter("mail.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		MIMETransformer parser = new MIMETransformer();

		String asd = "*OKasd\r\n" +
				"MIME-Version: 1.0\r\n" +
				"Received: by 10.79.70.1 with HTTP; Tue, 14 Jun 2016 11:07:58 -0700 (PDT)\r\n" +
				"Date: Tue, 14 Jun 2016 15:07:58 -0300\r\n" +
				"Delivered-To: lmarzora@itba.edu.ar\r\n" +
				"Message-ID: <CA+TfVzcPLzuPRRS8R4B4ejuamYU-stK6DTOz+wT04M76fvox9A@mail.gmail.com>\r\n" +
				"Subject: hola\r\n" +
				"From: Luis Ignacio Marzoratti <lmarzora@itba.edu.ar>\r\n" +
				"To: protos1234@gmx.com\r\n" +
				"Content-Type: multipart/alternative; boundary=94eb2c066dee84ab65053540e278\r\n" +
				"\r\n" +
				"--94eb2c066dee84ab65053540e278\r\n" +
				"Content-Type: text/plain; charset=UTF-8\r\n" +
				"\r\n" +
				"hola\r\n" +
				"\r\n" +
				"--94eb2c066dee84ab65053540e278\r\n" +
				"Content-Type: text/html; charset=UTF-8\r\n" +
				"\r\n" +
				"<div dir=\"ltr\">hola<br></div>\r\n" +
				"\r\n" +
				"--94eb2c066dee84ab65053540e278--\r\n";

		MIMETransformer q = new MIMETransformer();


		return;

	}

	public String mailTransformer(String chunk) {
		if (chunk.isEmpty()) {
			return "";
		}
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
				case START:
					state = State.UNFOLDER;
					break;
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
								boundarys.pop();
								if(boundarys.size()!=0) {	
									boundaryRegex = "^--"+boundarys.peek();
									boundaryPattern = Pattern.compile(boundaryRegex);
								}
							}
						} else{
							if(bound.isEmpty()) {
								if(boundarys.isEmpty() || !unfolded.substring(2).equals(boundarys.peek()))		//si no estaba en la cima del stack apilo, sino sigo en la misma
									boundarys.add(unfolded.substring(2)); //removes --
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
								state = State.BODYHEADER;
							} else {
								unfolded.append(lines[i]);
								break;
							}
						}
						ans.append(ImageUtils.rotate180(imageBuffer, imageSubtype, imageEncoding)).append(CRLF);
						imageBuffer.setLength(0);
						imageSubtype=null;
						imageEncoding=null;
						ans.append(unfolded.toString()).append(CRLF);
						unfolded.setLength(0);
						unfolded.append(lines[i]);
					} else {
						imageBuffer.append(unfolded);
						if (imageBuffer.length() > IMAGE_LIMIT) {
							ans.append(imageBuffer).append(CRLF);
							imageBuffer.setLength(0);
							imageSubtype = null;
							imageEncoding = null;
							ans.append(unfolded).append(CRLF);
							unfolded.setLength(0);
							unfolded.append(lines[i]);
							state = State.BODYBODY;
						}
						unfolded.setLength(0);
						unfolded.append(lines[i]);
					}
				} else {
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
			boundaryRegex = "^--"+unfolded.substring(start, finish);
			boundaryPattern = Pattern.compile(boundaryRegex);
		} else {
			Matcher image = imageTypePattern.matcher(unfolded);
			if (image.find()) {
				ans.append(unfolded).append(CRLF);
				imageSubtype = unfolded.substring(SUBTYPE_INDEX,image.end()-2);
			} else{
				Matcher enc = contentTransferPattern.matcher(unfolded);
				if (imageSubtype!=null && enc.find()) {
					ans.append(unfolded).append(CRLF);
					imageEncoding = unfolded.substring(enc.end());
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

	public String done() {
		StringBuilder ans = new StringBuilder();
		Matcher lastBoundary = boundaryPattern.matcher(unfolded);
		if (lastBoundary.find()) {
			String bound = unfolded.substring(lastBoundary.end());
			if (bound.equals(("--"))) {
				ans.append(ImageUtils.rotate180(imageBuffer, imageSubtype, imageEncoding)).append(CRLF);
				ans.append(unfolded).append(CRLF);
			}
		} else {
			ans.append(imageBuffer).append(CRLF).append(unfolded);
		}
		unfolded.setLength(0);
		state = State.UNFOLDER;
		isMime = false;
		boundarys = new Stack<>();
		unfolded = new StringBuilder();
		imageSubtype = null;
		imageEncoding = null;
		imageBuffer = new StringBuilder();
		return ans.toString();
	}

	public enum State {
		START, UNFOLDER, BODYHEADER, BODYBODY, COMMONMAIL, IMAGE, HERROR, BERROR,
	}

}
