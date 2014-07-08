package VoxelSystem.VoxelMaterials;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import jme3tools.converters.ImageToAwt;

import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.util.BufferUtils;

public class MaterialTools {
	public static BufferedImage scaleImage(BufferedImage bi, int tileDim){ // scale image to tileDim x tileDim
		BufferedImage resized = new BufferedImage(tileDim, tileDim, BufferedImage.TYPE_INT_ARGB);
		Graphics2D cg = resized.createGraphics();
		RenderingHints r = cg.getRenderingHints();
		r.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//		r.put(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
//		r.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		cg.setRenderingHints(r);
		
		cg.drawImage(bi, 
				0, 0, tileDim, tileDim, 
				0, 0, bi.getWidth(), bi.getHeight(), null);
		return resized;
	}
	

	
	public static Image scaleImage(Image image, int tileDim,AWTLoader awt){
		if(image.getWidth() == tileDim && image.getHeight()==tileDim){
			return image;
		}
		
		BufferedImage img =  ImageToAwt.convert(image,false,true, 0);
		BufferedImage scaled = scaleImage(img, tileDim);
		return awt.load(scaled,false);
	}

	/***
	 * Utilizes ImageToAwt.convert to convert image types.
	 * @param image
	 * @param f
	 * @return
	 */
	public static Image setImageFormat(Image image, Format f){
		if(image.getFormat()==f){
			return image;
		}
		int pixels = image.getHeight()*image.getWidth();
		ByteBuffer bb =  BufferUtils.createByteBuffer(pixels*(f.getBitsPerPixel()/8));//Format.BGR8
		BufferedImage img =  ImageToAwt.convert(image,false,true, 0);
		ImageToAwt.convert(img, f, bb);
		return new Image(f,image.getWidth(),image.getHeight(),bb);
	}
	
}
