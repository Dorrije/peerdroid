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

package net.jxta.impl;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;

/**
 * @author sting
 *
 */
public class ConfigProperties {
	private static final ResourceBundle RESOURCE_BUNDLE ;
    //= ResourceBundle.getBundle( BUNDLE_NAME );
    //Hack for android problem with resource bundles.
    
	public static ResourceBundle getBundle()    {
		return RESOURCE_BUNDLE;
	}
	
	static {
    	ResourceBundle bundle = new ListResourceBundle(){
			protected Object[][] getContents() {
				return resources;
			}
    	};

		RESOURCE_BUNDLE = bundle;
    }
			
    private static Object[][] resources = new Object[][]    {
    	{"PlatformPeerGroupClassName", "net.jxta.impl.peergroup.Platform"},
	    {"StdPeerGroupClassName", "net.jxta.impl.peergroup.StdPeerGroup"},
    	{"NetPeerGroupID", "jxta-NetGroup"},
    	{"NetPeerGroupName", "NetPeerGroup"},
    	{"NetPeerGroupDesc", "default Net Peer Group"},
    	{"ConfiguratorClassName", "net.jxta.impl.peergroup.DefaultConfigurator"},
    	{"AdvertisementInstanceTypes",
    	     "net.jxta.impl.protocol.PeerAdv" + " " +
		     "net.jxta.impl.protocol.PlatformConfig" + " " +
			 "net.jxta.impl.protocol.PeerGroupAdv" + " " +
			 "net.jxta.impl.protocol.TCPAdv" + " " +
			 "net.jxta.impl.protocol.HTTPAdv" + " " +
			 "net.jxta.impl.protocol.RdvConfigAdv" + " " +
			 "net.jxta.impl.protocol.DiscoveryConfigAdv" + " " +
			 "net.jxta.impl.protocol.PipeAdv" + " " +
			 "net.jxta.impl.protocol.RelayConfigAdv" + " " +
			 "net.jxta.impl.protocol.RdvAdv" + " " +
			 "net.jxta.impl.protocol.ModuleImplAdv" + " " +
			 "net.jxta.impl.protocol.ModuleSpecAdv" + " " +
			 "net.jxta.impl.protocol.ModuleClassAdv" + " " +
			 "net.jxta.impl.protocol.RouteAdv" + " " +
			 "net.jxta.impl.protocol.AccessPointAdv"},
		{"StructuredDocumentInstanceTypes",
		     "net.jxta.impl.document.PlainTextDocument" + " " +
			 "net.jxta.impl.document.LiteXMLDocument"},
		{"MsgWireFmtsInstanceTypes", "net.jxta.impl.endpoint.WireFormatMessageBinary"},
		{"IDInstanceTypes", "net.jxta.impl.id.UUID.IDFormat" + " " + 
			"net.jxta.impl.id.CBID.IDFormat"},
		{"IDNewInstances", "uuid"}
	};
}
