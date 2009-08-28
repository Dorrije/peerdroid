package peerdroid.sample.info;

import java.util.ArrayList;

/**
 * 
 * Contains network informations about download and upload speeds.
 * 
 * @author Marco Picone
 * @created 01/09/2008
 */
public class NetworkInformation {

	private ArrayList<Double> download_speed;
	private ArrayList<Double> upload_speed;
	
	//Max size of array, linked with the draw of the related graph.
	private int MAX_SIZE = 270;
	
	/**
	 * Class constructor
	 */
	public NetworkInformation(){
		download_speed = new ArrayList<Double>();
		upload_speed = new ArrayList<Double>();
	}
	
	/**
	 * Adds download information
	 * @param info
	 */
	public void addDonwloadInfo( Double info){
		
		if(download_speed.size() == MAX_SIZE)
		{
			download_speed.remove(0);
		}
		
		download_speed.add(info);
	}
	
	/**
	 * Adds upload information
	 * @param info
	 */
	public void addUploadInfo( Double info){
		
		if(upload_speed.size() == MAX_SIZE)
		{
			upload_speed.remove(0);
		}

		upload_speed.add(info);
	}
	
	public int getDownloadInfoSize(){
		return download_speed.size();
	}
	
	public int getUploadInfoSize(){
		return upload_speed.size();
	}
	
	public Double getDownloadInfoAt( int pos ){
		return download_speed.get(pos);
	}
}
