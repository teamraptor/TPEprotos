package transformers;

import java.util.regex.Pattern;

public class MIMETransformer {

	private static String contentTypeRegex = "^(c|C)(o|O)(n|N)(t|T)(e|E)(n|N)(t|T)-(t|T)(y|Y)(p|P)(e|E): ";
	private static Pattern contentTypePattern = Pattern.compile(contentTypeRegex);
}
