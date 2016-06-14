package transformers;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageUtils {

	public static String rotate180(StringBuilder image, String imageSubtype, String imageEncoding) {
		if (image.toString().isEmpty()) {
			return image.toString();
		}



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
			//double rotationRequired = Math.toRadians (180);
			BufferedImage ans = rotate(img, 180);
//			double locationX = img.getWidth() / 2;
//			double locationY = img.getHeight() / 2;
//			AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
//			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
//			img = op.filter(img, null);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			OutputStream b64 = new Base64OutputStream(os);
			ImageIO.write(ans, imageSubtype , b64);
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
	
	public static BufferedImage rotate(BufferedImage image, double angle) {
	    int w = image.getWidth(), h = image.getHeight();
	    GraphicsConfiguration gc = getDefaultConfiguration();
	    BufferedImage result = gc.createCompatibleImage(w, h);
	    Graphics2D g = result.createGraphics();
	    g.rotate(Math.toRadians(angle), w / 2, h / 2);
	    g.drawRenderedImage(image, null);
	    g.dispose();
	    return result;
	}
	
	public static GraphicsConfiguration getDefaultConfiguration() {
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsDevice gd = ge.getDefaultScreenDevice();
	    return gd.getDefaultConfiguration();
	}
}
