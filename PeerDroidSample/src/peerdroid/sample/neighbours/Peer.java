package peerdroid.sample.neighbours;

import net.jxta.protocol.PipeAdvertisement;


/**
 * 
 * Describes a Peer in the network
 * 
 * @author Marco Picone
 * @created 28/08/09
 */
public class Peer {

	private String name = null;
	private PipeAdvertisement pipeAdvertisement = null;
	private long lastUpdate = 0;
	
	public Peer(String name, PipeAdvertisement pipeAdvertisement, long lastUpdate) {
		super();
		this.name = name;
		this.pipeAdvertisement = pipeAdvertisement;
		this.lastUpdate = lastUpdate;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		Peer objPeer = (Peer)obj;
		
		if(objPeer.name.equals(this.name))
			return true;
		
		return false;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public PipeAdvertisement getPipeAdvertisement() {
		return pipeAdvertisement;
	}
	public void setPipeAdvertisement(PipeAdvertisement pipeAdvertisement) {
		this.pipeAdvertisement = pipeAdvertisement;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	
	
	
}
