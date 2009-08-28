/*
 * Copyright (C) 2007 The Information and Communication Research Labs of ITRI.
 * All Rights Reserved.
 *
 * This SOURCE CODE FILE,  which has been provided by Itri Computer
 * and Communications Labs as part of an Computer and Communication
 * Labs product for use ONLY by licensed users of the product,
 * include CONFIDENTIAL and PROPRIETARY information of Itri Computer
 * and Communications Labs.
 *
 * USE OF THIS SOFTWARE IS GOVERNED BY THE TERMS AND CONDITIONS OF
 * THE LICENSE STATEMENT AND LIMITED WARRANTY FURNISHED WITH THE
 * PRODUCT.
 *
 * IN PARTICULAR, YOU WILL INDEMNIFY AND HOLD ITRI COMPUTER AND
 * COMMUNICATIONS LABS SOFTWARE, ITS RELATED COMPANIES AND ITS
 * SUPPLIERS, HARMLESS FROM AND AGAINST ANY CLAIMS OR LIABILITIES
 * ARISING OUT OF THE USE, REPRODUCTION, OR DISTRIBUTION OF YOUR
 * PROGRAMS, INCLUDING ANY CLAIMS OR LIABILITIES ARISING OUT OF
 * OR RESULTING FROM THE USE, MODIFICATION OR DISTRIBUTION OF
 * PROGRAMS OR FILES CREATED FROM, BASED ON, AND/OR DERIVED FROM
 * THIS SOURCE CODE FILE.
 *
 *
 * Author: Cheng Po-Wen <sting@itri.org.tw>, (C) 2007
 *
 * Copyright: See COPYING file that comes with this distribution
 *
 */

package peerdroid.sample.service;

import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
//import java.io.IOException;
import java.net.URI;

import peerdroid.sample.PeerDroidSample;
//import java.net.URISyntaxException;

//import org.icl.jxme.utils.GroupUtils;

import android.util.Log;

//import net.jxta.credential.AuthenticationCredential;
//import net.jxta.document.Advertisement;
//import net.jxta.exception.PeerGroupException;
//import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.id.IDFactory;

import net.jxta.impl.id.UUID.UUID;
import net.jxta.impl.id.UUID.UUIDFactory;
//import net.jxta.membership.Authenticator;
//import net.jxta.membership.InteractiveAuthenticator;
//import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
//import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.peergroup.PeerGroupID;
//import net.jxta.platform.ConfigurationFactory;
import net.jxta.rendezvous.RendezVousService;

/**
 * @author sting
 *
 */
public class JXTASystem {
	private static final String TAG = "JXTA JXTASystem";
	private static final PeerGroupID ANDROID_ROOT_PEERGROUP_ID = (PeerGroupID) PeerGroupID.create(URI.create("urn:jxta:uuid-E52BD0F265354B44A9FDC83B32F7A21202"));
	
	private static byte[] preCookedPeerID = new byte[16];

    /**
     * The ID that our custom peer group will use. We use a hardcoded id, so
     * that all instances use the same value. This ID was generated using the
     * <tt>newpgrp -s</tt> JXSE Shell command.
     */
    
    private static PeerID MY_ID;

    //private static PeerGroup netPeerGroup = null;
    private static PeerGroup androidRootPeerGroup = null;
    
    public static void init()    {
    	initPeerID();
    }
    
    public static PeerGroup getRootPeerGroup()    {
    	return androidRootPeerGroup;
    }
    
	public static PeerID getMY_ID() {
		return MY_ID;
	}

	public void setMY_ID(PeerID my_id) {
		MY_ID = my_id;
	}

	private static void initPeerID()    {
		
		Log.d(PeerDroidSample.TAG,"initPeerID CALL!");
		
		File peerID = new File(net.jxta.impl.config.Config.JXTA_HOME + "PeerID");
		
		if(peerID.exists())    {
			try {
				FileInputStream fis = new FileInputStream(peerID);
				fis.read(preCookedPeerID);
				fis.close();
			} catch (Exception e) {
				Log.e(TAG, "PeerID file I/O Error", e);
				System.exit(1);
			}
		} else    {
			UUID uuid = UUIDFactory.newUUID();
			long most = uuid.getMostSignificantBits();
			long lease = uuid.getLeastSignificantBits();
			
			int i;
			for(i = 0; i < 8; i++)    {
				preCookedPeerID[i] = (byte)(lease & 0X00FFL);
				lease >>= 8;
			}
			
			for(i = 8; i < 16; i++)    {
				preCookedPeerID[i] = (byte)(most & 0X00FFL);
				most >>= 8;
			}
			
			try {
				peerID.createNewFile();
				FileOutputStream fos = new FileOutputStream(peerID);
				fos.write(preCookedPeerID);
				fos.close();
			} catch (Exception e) {
				Log.e(TAG, "PeerID file I/O Error", e);
				System.exit(1);
			}
		}

		MY_ID =	IDFactory.newPeerID(ANDROID_ROOT_PEERGROUP_ID, preCookedPeerID);
	}

}
