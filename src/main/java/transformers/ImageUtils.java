package transformers;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;

public class ImageUtils {

	public static String rotate180(StringBuilder image, String imageSubtype, String imageEncoding) {
		InputStream is;
		if(imageEncoding.equals("base64")) {
			byte[] aux = decode(image.toString());
			if(aux==null)
				return image.toString();
			is = new ByteArrayInputStream(aux);
		} else {
			return image.toString();
		}
		try {
			BufferedImage img = ImageIO.read(is);
			double rotationRequired = Math.toRadians (180);
			double locationX = img.getWidth() / 2;
			double locationY = img.getHeight() / 2;
			AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
			img = op.filter(img, null);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			OutputStream b64 = new Base64OutputStream(os);
			ImageIO.write(img, imageSubtype , b64);
			return os.toString("UTF-8");
			
		} catch (IOException e1) {
			return image.toString();
		}
	}
	
	private static byte[] decode(String image) {
		Base64 b64 = new Base64();
		try {
			return b64.decode(image);
		} catch (Exception f) {
			return null;
		}
	}
}
