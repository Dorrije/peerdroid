/**
 * 
 */
package peerdroid.sample.res;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import peerdroid.sample.tools.Utility;

/**
 * @author linghu
 *
 */
public class Image extends Resource {

	private String imgName = "";
	private String imgDesc = "";
	private GeoLocation imgLoc ;
	private List<String> imgTags = new ArrayList<String>();
	/**
	 * size of the image
	 */
	private int imgSize = -1; 
	private byte[] imgbytes ;
	/**
	 * size of the Image object
	 */
	private int iSize = -1; 
	
	public Image(String imgPath) {
		File file = new File(imgPath);
		if (!file.exists()) {
			throw new Error("File " + imgPath + " doesn't exist!");
		} else {
			fileProcessing(file);
		}
	}

	public Image(File file) {
		fileProcessing(file);
	}
	
	private void fileProcessing(File file) {
		imgName = file.getName();
		try{
			FileInputStream fis = new FileInputStream(file);
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);
			imgbytes = new byte[bytes.length];
			System.arraycopy(bytes, 0, imgbytes, 0, bytes.length);
			imgSize = bytes.length; 
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getImgDesc() {
		return imgDesc;
	}

	public void setImgDesc(String imgDesc) {
		this.imgDesc = imgDesc;
	}

	public GeoLocation getImgLoc() {
		return imgLoc;
	}

	public void setImgLoc(GeoLocation imgLoc) {
		this.imgLoc = imgLoc;
	}

	public String getImgName() {
		return imgName;
	}

	public List<String> getImgTags() {
		return imgTags;
	}
	
	public void tagImg(String tag) {
		if (!imgTags.contains(tag)) {
			imgTags.add(tag);
		} 
	}

	public void tagsImg(List<String> tags) {
		for (String s : tags)
			tagImg(s);
	}
	
	public int getImgSize() {
		return imgSize;
	}

	/**
	 * Get the byte array of the image file
	 * @return
	 */
	public byte[] getImgbytes() {
		return imgbytes;
	}

	/* (non-Javadoc)
	 * @see peerdroid.sample.res.Resource#getBytes()
	 * Get the byte array of the Image Object
	 */
	@Override
	byte[] getBytes() {
		try {
			byte[] ret = Utility.getBytes(this);
			iSize = ret.length;
			return ret;
		} catch(IOException ioe) {
			ioe.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see peerdroid.sample.res.Resource#getResource(byte[])
	 */
	@Override
	Resource getResource(byte[] bytes) {
		try {
			Image obj = (Image)Utility.toObject(bytes);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see peerdroid.sample.res.Resource#getResourceSize()
	 */
	@Override
	int getResourceSize() {
		// TODO Auto-generated method stub
		return iSize;
	}

	/* (non-Javadoc)
	 * @see peerdroid.sample.res.Resource#getResourceType()
	 */
	@Override
	int getResourceType() {
		// TODO Auto-generated method stub
		return Resource.ImageResource;
	}

}
