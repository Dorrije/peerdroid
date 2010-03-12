/**
 * 
 */
package peerdroid.sample.res;

/**
 * @author linghu
 *
 */
public abstract class Resource {

	public static  int MessageResource = 0;
	public static  int ImageResource = 1;
	public static  int VideoResource = 2;
	
	abstract byte[] getBytes();
	abstract int getResourceType();
	abstract int getResourceSize();
	abstract Resource getResource(byte[] bytes); 
}
