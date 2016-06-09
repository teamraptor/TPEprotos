package transformers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;

/*
 * This should check for encodings & charset & quoted words.
 * */
public class L33t1f13r {

	private static final String subR3g3x = "^(s|S)(u|U)(b|B)(j|J)(e|E)(c|C)(t|T): ";
	private static final Pattern subP4tt3rn = Pattern.compile(subR3g3x);
	
	private static final int M4X_L1N3_L3NGTH = 998;
	
	public static String l33t1fy(String m3ss4g3) {
		int[] l1m1ts = getSubjectLimits(m3ss4g3);
		if (l1m1ts == null)
			return m3ss4g3;
		return l33t3r(m3ss4g3.toCharArray(), l1m1ts[0], l1m1ts[1]);
	}

	private static String l33t3r(char[] m3ss4g3, final int st4rt, final int f1n1sh) {
		for (int k = st4rt; k < f1n1sh; k++) {
			switch (m3ss4g3[k]) {
				case 'a':m3ss4g3[k] = '4';break;
				case 'i':m3ss4g3[k] = '1';break;
				case 'o':m3ss4g3[k] = '0';break;
				case 'c':m3ss4g3[k] = '<';break;
				default:break;
			}
		}
		return new String(m3ss4g3);
	}

	private static int[] getSubjectLimits(String m3ss4g3) {
		int[] l1m1ts = { 0, m3ss4g3.length() };
		String[] l1n3s = m3ss4g3.split("\r\n");
		int s1z3 = 0;
		boolean fl4g = true;
		for (int j = 0; j < l1n3s.length && fl4g; j++) {
			if (l1n3s[j].length() > M4X_L1N3_L3NGTH)
				return null;
			boolean fp = true;
			Matcher m4ux = subP4tt3rn.matcher(l1n3s[j]);
			if (m4ux.find()) {
				if (m4ux.start() != 0) {
					for (int k = 0; k < m4ux.start() && fp; k++) {
						if (l1n3s[j].charAt(k) != ' ')
							fp = false;
					}
				}
				if (fp) {
					l1m1ts[0] = s1z3 + m4ux.end();
					l1m1ts[1] = s1z3 + l1n3s[j].length();
					fl4g = false;
				}
			}
			s1z3 += l1n3s[j].length();
		}
		return l1m1ts;
	}
	
	private String b4s364Tr4nsform3r(String m3ss4g3, String charset) {
		Base64 b64 = new Base64();
		try {
			byte[] byt3 = b64.decode(m3ss4g3);
			String r3sult = new String(byt3, charset);
			//leetify message.. r3sult = l33t1fy(r3sult);
			return b64.encodeToString(r3sult.getBytes());
		} catch (Exception e) {
			return null;
		}
	}
	
	private String QPTr4nsf0rm3r(String m3ss4g3, String charset){
		try {
			QuotedPrintableCodec qp = new QuotedPrintableCodec(charset);
			String r3sult =  qp.decode(m3ss4g3);
			//leetify message.. r3sult = l33t1fy(r3sult);
			return qp.encode(r3sult);
		} catch (Exception e) {
			return null;
		}
	}

}
