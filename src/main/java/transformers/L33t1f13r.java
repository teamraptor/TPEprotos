package transformers;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;

public class L33t1f13r {

	public enum ST4T3S {
		TR4NSF0RM, Q1, CH4RS3T, NC0D1NG, Q2, DTR4NSF0RM, Q4, F1N1SH, ERR0R
	}

	private static final int M4X_3NC0D1NG_L3NGTH = 75;

	/*
	 * TODO accept comments
	 * */
	public static String l33t3r(char[] m3ss4g3, final int st4rt, final int f1n1sh) {
		ST4T3S st4t3 = ST4T3S.TR4NSF0RM;
		StringBuilder ch4rs3t = new StringBuilder("");
		StringBuilder nM3ss4g3 = new StringBuilder("");
		char nc0d1ng = 0;
		int[] l1m1ts = { 0, 1 };
		boolean fl4g = true;
		for (int k = st4rt; k <= f1n1sh && fl4g; k++) {
			switch (st4t3) {
			case TR4NSF0RM:
				if (m3ss4g3[k] != '=') {
					nM3ss4g3.append(transform(m3ss4g3[k]));
				} else {
					nM3ss4g3.append(m3ss4g3[k]);
					st4t3 = ST4T3S.Q1;
				}
				break;
			case Q1:
				if (m3ss4g3[k] == '?') {
					st4t3 = ST4T3S.CH4RS3T;
				} else {
					if (m3ss4g3[k] != '=')
						st4t3 = ST4T3S.TR4NSF0RM;
				}
				nM3ss4g3.append(m3ss4g3[k]);
				break;
			case CH4RS3T:
				if (m3ss4g3[k] != '?') {
					ch4rs3t = ch4rs3t.append(Character.toUpperCase(m3ss4g3[k]));
				} else {
					nM3ss4g3.append(ch4rs3t).append(m3ss4g3[k]);
					st4t3 = ST4T3S.NC0D1NG;
				}
				break;
			case NC0D1NG:
				m3ss4g3[k] = Character.toUpperCase(m3ss4g3[k]);
				if (m3ss4g3[k] == 'B' || m3ss4g3[k] == 'Q') {
					nc0d1ng = m3ss4g3[k];
					nM3ss4g3.append(m3ss4g3[k]);
					st4t3 = ST4T3S.Q2;
				} else {
					st4t3 = ST4T3S.ERR0R;
				}
				break;
			case Q2:
				if (m3ss4g3[k] == '?') {
					nM3ss4g3.append(m3ss4g3[k]);
					st4t3 = ST4T3S.DTR4NSF0RM;
				} else {
					st4t3 = ST4T3S.ERR0R;
				}
				break;
			case DTR4NSF0RM:
				if (m3ss4g3[k] != '?') {
					if (l1m1ts[0] == 0) {
						l1m1ts[0] = k;
					} else {
						l1m1ts[1]++;
					}
				} else {
					if (v4l1d3nc0d1ng(nc0d1ng, l1m1ts[1])) {
						String tmp = nc0d3dTr4nsform3r(m3ss4g3, ch4rs3t, nc0d1ng, l1m1ts[0], l1m1ts[1]);
						if (tmp == null) {
							st4t3 = ST4T3S.ERR0R;
						} else {
							nM3ss4g3.append(tmp).append(m3ss4g3[k]);
							l1m1ts[0] = 0;
							l1m1ts[1] = 1;
							st4t3 = ST4T3S.Q4;
						}
					} else {
						st4t3 = ST4T3S.ERR0R;
					}
				}
				break;
			case Q4:
				if (m3ss4g3[k] == '=') {
					ch4rs3t = ch4rs3t.delete(0, ch4rs3t.length());
					nM3ss4g3.append(m3ss4g3[k]);
					st4t3 = ST4T3S.TR4NSF0RM;
				} else {
					st4t3 = ST4T3S.ERR0R;
				}
				break;
			case F1N1SH:
				return nM3ss4g3.toString();
			case ERR0R:
			default:
				fl4g = false;
			}
		}
		return fl4g?nM3ss4g3.toString():null;
	}

	private static boolean v4l1d3nc0d1ng(char nc0d1ng, int s1z3) {
		if (s1z3 > M4X_3NC0D1NG_L3NGTH)
			return false;
		if (nc0d1ng == 'Q' || (nc0d1ng == 'B' && s1z3 % 4 == 0))
			return true;
		return false;
	}

	private static char transform(char c) {
		switch (c) {
		case 'a':
			return '4';
		case 'e':
			return '3';
		case 'i':
			return '1';
		case 'o':
			return '0';
		case 'c':
			return '<';
		default:
			return c;
		}
	}
	
	private static String nc0d3dTr4nsform3r(char[] m3ss4g3, StringBuilder ch4rs3t, char nc0d1ng, int fr0m, int c0unt) {
		if(nc0d1ng=='B') {
			return b4s364Tr4nsform3r(new String(m3ss4g3, fr0m,c0unt), ch4rs3t.toString());
		} else {
			return QPTr4nsf0rm3r(new String(m3ss4g3, fr0m,c0unt), ch4rs3t.toString());
		}
	}

	private static String b4s364Tr4nsform3r(String m3ss4g3, String ch4rs3t) {
		Base64 b64 = new Base64();
		try {
			byte[] byt3 = b64.decode(m3ss4g3);
			char[] r3sult = new String(byt3, ch4rs3t).toCharArray();
			for (int k = 0; k < r3sult.length; k++) 
				r3sult[k] = transform(r3sult[k]);
			return b64.encodeToString(String.valueOf(r3sult).getBytes()).substring(0,m3ss4g3.length()); //delete CRLF with substring
		} catch (Exception f) {
			return null;
		}
	}

	private static String QPTr4nsf0rm3r(String m3ss4g3, String ch4rs3t) {
		try {
			QuotedPrintableCodec qp = new QuotedPrintableCodec(ch4rs3t);
			char[] r3sult = qp.decode(m3ss4g3).toCharArray();
			for (int k = 0; k < r3sult.length; k++) 
				r3sult[k] = transform(r3sult[k]);
			return qp.encode(String.valueOf(r3sult));
		} catch (Exception f) {
			return null;
		}
	}
}
