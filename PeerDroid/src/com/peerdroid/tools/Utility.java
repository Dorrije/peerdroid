/**
 * 
 */
package com.peerdroid.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * @author Ling
 *
 */
public class Utility {
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * 
	 * @param b
	 * @return
	 */
	
	public static final int byteArrayToInt(byte [] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
	}
	/**
	 * 
	 * @param value
	 * @return
	 */
	public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
	}
	/**
	 * convert any object into byte array
	 * @param obj
	 * @return
	 * @throws java.io.IOException
	 */
	public static byte[] getBytes(Object obj) throws java.io.IOException{
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      ObjectOutputStream oos = new ObjectOutputStream(bos);
	      oos.writeObject(obj);
	      oos.flush();
	      oos.close();
	      bos.close();
	      byte [] data = bos.toByteArray();
	      return data;
	  }
	
	/**
	 * restore a byte array to an object.
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static Object toObject(byte[] bytes) throws Exception{
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object object = ois.readObject();
		return object;
		}
	/**
	 * write the byte array to a file
	 * @param bytes
	 * @param fileName
	 * @throws IOException
	 */
	public static void saveBytes(byte[] bytes, String fileName) throws IOException {
		FileOutputStream outStream = new FileOutputStream(fileName);
		outStream.write(bytes); 
		outStream.flush();
		outStream.close(); 
	}
	
	public static String getCurrentTime() {
		return ("[" + dateFormat.format(new Date(System.currentTimeMillis())) + "] ");
	}
	public static String getTime(long millis) {
		return ("[" + dateFormat.format(new Date(millis)) + "] ");
	}
	/**
     * Checks whether the device is able to connect to the network
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w("tag", "couldn't get connectivity manager");

        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null) {

                for (int i = 0; i < info.length; i++) {

                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {

                        return true;

                    }

                }
            }
        }
        return false;
    } 
}
