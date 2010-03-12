/**
 * 
 */
package peerdroid.sample.res;

import java.io.IOException;

import peerdroid.sample.tools.Utility;

/**
 * @author linghu
 *
 */
public class Message extends Resource {
	private String msgContent = "";
	private int mSize = -1;
	
	public Message(String msgContent) {
		super();
		this.msgContent = msgContent;
	}

	public int getMessageSize() {
		return msgContent.length();
	}
	
	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	/* (non-Javadoc)
	 * @see peerdroid.sample.res.Resource#getBytes()
	 */
	@Override
	public byte[] getBytes() {
		try {
			byte[] ret = Utility.getBytes(this);
			mSize = ret.length;
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
	public Message getResource(byte[] bytes) {
		try {
			Message obj = (Message)Utility.toObject(bytes);
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
	public int getResourceSize() {
		return mSize;
	}

	/* (non-Javadoc)
	 * @see peerdroid.sample.res.Resource#getResourceType()
	 */
	@Override
	public int getResourceType() {
		return Resource.MessageResource;
	}

}
