/*
 *  Copyright (c) 2006 Sun Microsystems, Inc.  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Sun Microsystems, Inc. for Project JXTA."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *  must not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact Project JXTA at http://www.jxta.org.
 *
 *  5. Products derived from this software may not be called "JXTA",
 *  nor may "JXTA" appear in their name, without prior written
 *  permission of Sun.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 *
 *  $Id: ConfigurationFactory.java,v 1.16 2006/03/13 22:46:16 bondolo Exp $
 */
package net.jxta.platform;

import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredDocumentUtils;
import net.jxta.document.XMLDocument;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.protocol.PlatformConfig;
import net.jxta.impl.protocol.RdvConfigAdv;
import net.jxta.impl.protocol.RdvConfigAdv.RendezVousConfiguration;
import net.jxta.impl.protocol.RelayConfigAdv;
import net.jxta.impl.protocol.TCPAdv;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *  By design this configuration factory provides a preset edge configuration
 *  which addresses the majority of edge peer configuration. The application can
 *  override some of these defaults by calling the setter methods prior to
 *  calling newPlatformConfig. For advanced settings, the application is
 *  expected to manipulate each service defined with the PlaformConfig through
 *  the associated binding.
 */
public final class ConfigurationFactory  {
    private static final Logger LOG = Logger.getLogger(ConfigurationFactory.class.getName());

    /**
     *  Default multicast datagram buffer size set 16K
     */
    protected int bufferSize = 16384;
    private static ConfigurationFactory factory = new ConfigurationFactory();
    /**
     *  Default JXTA_HOME set ".jxta"
     */
    protected File home = new File(".jxta");
    /**
     *  Default tcp incoming on
     */
    protected boolean tcp_incoming = true;
    /**
     *  Default multicast port set 1234
     */
    protected int mcastPort = 1234;
    /**
     *  Default Multicast state
     */
    protected boolean mutlicastOn = true;
    /**
     *  Default tcp outgoing true
     */
    protected boolean tcp_outgoing = true;
    /**
     *  Default PeerID set null
     */
    protected PeerID peerid = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID);
    /**
     *  Default peer uses a relay
     */
    protected boolean relayed = true;
    /**
     *  Default tcp listening port set to 9901
     */
    protected int tcpPort = 9901;
    /**
     *  Default tcp start listening port range set to 9901
     */
    protected int tcpStartPort = 9901;
    /**
     *  Default tcp end listening port range set to 9990
     */
    protected int tcpEndPort = 9990;
    /**
     *  Default peer name
     */
    protected String name = "unknown";
    /**
     *  Default Rendezvous Seeding URI
     */
    protected URI rdvSeedingURI = null;
    /**
     *  Default Relay Seeding URI
     */
    protected URI relaySeedingURI = null;
    /**
     * Rendevous seeds table
     */
    protected Set seedRendezvous = new HashSet();
    /**
     * Use only those defined as rendezvous seeds
     */
    protected boolean useOnlyRendezvouSeeds = false;
    /**
     * Relay seed table
     */
    protected Set seedRelay = new HashSet();
    /**
     * Use only those defined as relay seeds
     */
    protected boolean useOnlyRelaySeeds = false;
    /**
     * No default range set for TCP ports
     */
    protected boolean rangeSet = true;
    /**
     * Default PlatformConfig Peer Description
     */
    protected String description = "Platform Config Advertisement created by : " + ConfigurationFactory.class.getName();
    /**
     * Default NetPeerGroup ID
     */
    protected static PeerGroupID netPGID = PeerGroupID.defaultNetPeerGroupID;
    /**
     *  Constructor for the ConfigurationFactory object
     */
    private ConfigurationFactory() {
    	
        try {
            rdvSeedingURI = new URI("http://rdv.jxtahosts.net/cgi-bin/rendezvous.cgi?2");
            relaySeedingURI = new URI("http://rdv.jxtahosts.net/cgi-bin/relays.cgi?2");
        } catch (URISyntaxException ue) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Failed to initialize seeding URIs", ue);
            }
        }
    }
    public static ConfigurationFactory newInstance() {
        return factory;
    }

    /**
     *  Creates an http transport advertisement
     *
     *@param  port    listening port
     *@param  server  if incoming is enabled
     *@param  client  if outgoing is enabled
     *@return         an http transport advertisement
     *
    private static HTTPAdv createHttpAdv(int port, boolean server, boolean client) {
        HTTPAdv httpAdv = (HTTPAdv) AdvertisementFactory.newAdvertisement(HTTPAdv.getAdvertisementType());
        httpAdv.setProtocol("http");
        httpAdv.setPort(port);
        httpAdv.setClientEnabled(client);
        httpAdv.setServerEnabled(server);
        return httpAdv;
}
    */
    /**
     *  returns a Rendezvous Config Advertisement seeded with default public
     *  seeding uri "http://rdv.jxtahosts.net/cgi-bin/relays.cgi?2"
     *
     *@return    a RdvConfigAdv
     */
    private static RdvConfigAdv createRdvConfigAdv() {
        return createRdvConfigAdv(factory.rdvSeedingURI);
    }

    /**
     *  returns a Rendezvous Config Advertisement seeded with seedingURI
     *
     *@param  seedingURI  seeding uri
     *@return             a RdvConfigAdv
     */
    private static RdvConfigAdv createRdvConfigAdv(URI seedingURI) {
        RdvConfigAdv rdvAdv = (RdvConfigAdv) AdvertisementFactory.newAdvertisement(RdvConfigAdv.getAdvertisementType());
        if (seedingURI != null) {
            rdvAdv.addSeedingURI(seedingURI);
        }

        Iterator eachSeed = factory.seedRendezvous.iterator();
        while (eachSeed.hasNext()) {
            rdvAdv.addSeedRendezvous((URI) eachSeed.next());
        }
        rdvAdv.setUseOnlySeeds(factory.useOnlyRendezvouSeeds);
        rdvAdv.setConfiguration(RendezVousConfiguration.EDGE);
        return rdvAdv;
    }

    /**
     *  returns a Relay Config Advertisement seeded with default public seeding
     *  uri "http://rdv.jxtahosts.net/cgi-bin/relays.cgi?2"
     *
     *@return    a RelayConfigAdv
     */
    private static RelayConfigAdv createRelayConfigAdv() {
        return createRelayConfigAdv(factory.relaySeedingURI);
    }

    /**
     *  returns a Relay Config Advertisement seeded with seedingURI
     *
     *@param  seedingURI  seeding uri
     *@return             a RelayConfigAdv
     */
    private static RelayConfigAdv createRelayConfigAdv(URI seedingURI) {
        RelayConfigAdv relayConfig = (RelayConfigAdv) AdvertisementFactory.newAdvertisement(
                                         RelayConfigAdv.getAdvertisementType());
        if (seedingURI != null) {
            relayConfig.addSeedingURI(seedingURI);
        }
        Iterator eachSeed = factory.seedRelay.iterator();
        while (eachSeed.hasNext()) {
            //FIXME RelayConfigAdv should support addSeedRelay(uri)
            relayConfig.addSeedRelay(eachSeed.next().toString());
        }
        relayConfig.setUseOnlySeeds(factory.useOnlyRelaySeeds);
        relayConfig.setClientEnabled(true);
        relayConfig.setServerEnabled(false);
        return relayConfig;
    }

    /**
     *  Creates an tcp transport advertisement with the platform default values
     *  multicast on, 224.0.1.85:1234, with a max packet size of 16K
     *
     *@return    an tcp transport advertisement
     */
    private static TCPAdv createTcpAdv() {
        TCPAdv tcpAdv = (TCPAdv) AdvertisementFactory.newAdvertisement(TCPAdv.getAdvertisementType());
        tcpAdv.setProtocol("tcp");
        tcpAdv.setInterfaceAddress(null);
        if (!factory.rangeSet) {
            tcpAdv.setPort(factory.tcpPort);
        } else {
            tcpAdv.setPort(factory.tcpStartPort);
            tcpAdv.setStartPort(factory.tcpStartPort);
            tcpAdv.setEndPort(factory.tcpEndPort);
        }
        tcpAdv.setMulticastAddr("224.0.1.85");
        tcpAdv.setMulticastPort(factory.mcastPort);
        tcpAdv.setMulticastSize(factory.bufferSize);
        tcpAdv.setMulticastState(factory.mutlicastOn);
        tcpAdv.setServer(null);
        tcpAdv.setClientEnabled(factory.tcp_outgoing);
        tcpAdv.setServerEnabled(factory.tcp_incoming);
        return tcpAdv;
    }

    /**
     *  Gets the parmDoc attribute of the ConfigurationFactory class
     *
     *@param  enabled  whether the param doc is enabled or not, adds a "isOff"
     *      element if disabled
     *@param  adv      the Advertisement to retrive the param doc from
     *@return          The parmDoc value
     */
    private static StructuredDocument getParmDoc(boolean enabled, Advertisement adv) {
        StructuredDocument parmDoc = StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, "Parm");
        StructuredDocument doc = (StructuredDocument) adv.getDocument(MimeMediaType.XMLUTF8);
        StructuredDocumentUtils.copyElements(parmDoc,
                                             parmDoc,
                                             doc);
        if (!enabled) {
            parmDoc.appendChild(parmDoc.createElement("isOff"));
        }
        return parmDoc;
    }

    /**
     *  Returns a PlatformConfig which represents configuration Fine tuning is
     *  achieved through accessing each configured advertisement and modifying
     *  each object directly. The resulting can then be used as the platform
     *  configuration
     *
     *@return    The edgePeerPlatformConfig Advertisement
     */
    public static Advertisement newPlatformConfig() {
        PlatformConfig advertisement = (PlatformConfig) AdvertisementFactory.newAdvertisement(PlatformConfig.getAdvertisementType());
        advertisement.setName(factory.name);
        advertisement.setDescription(factory.description);
        if (factory.peerid != null) {
            advertisement.setPeerID(factory.peerid);
        }

        TCPAdv tcpAdv = createTcpAdv();
        advertisement.putServiceParam(PeerGroup.tcpProtoClassID, getParmDoc(true, tcpAdv));

        if (factory.relayed) {
            RelayConfigAdv relayConfig = createRelayConfigAdv();
            XMLDocument relayDoc = (XMLDocument) relayConfig.getDocument(MimeMediaType.XMLUTF8);
            advertisement.putServiceParam(PeerGroup.relayProtoClassID, relayDoc);
        }

        RdvConfigAdv rdvConfig = createRdvConfigAdv();
        XMLDocument rdvDoc = (XMLDocument) rdvConfig.getDocument(MimeMediaType.XMLUTF8);
        advertisement.putServiceParam(PeerGroup.rendezvousClassID, rdvDoc);

        return advertisement;
    }

    /**
     *  Saves a PlatformConfig advertisement in $JXTA_HOME/PlaformConfig
     *
     *@param  advertisement              PlatformConfig advertisement to save
     *@param  overwrite                  if true, overrides any existing Platformconfig
     *@exception  IOException if an io error occurs
     */
    public static void save(Advertisement advertisement, boolean overwrite) throws IOException {
        if (!(advertisement instanceof PlatformConfig)) {
            throw new IllegalArgumentException("Invalid PlatformConfig Advertisement");
        }
        FileOutputStream out = null;
        factory.home.mkdirs();
        File saveFile = new File(factory.home, "PlatformConfig");
        if (!overwrite && saveFile.exists()) {
            return;
        }
        out = new FileOutputStream(saveFile);
        XMLDocument aDoc = (XMLDocument) advertisement.getDocument(MimeMediaType.XMLUTF8);
        OutputStreamWriter os = new OutputStreamWriter(out, "UTF-8");
        aDoc.sendToWriter(os);
        os.flush();
        out.close();
    }

    /**
     * Returns the stream of bytes which represents the content of this
     * <code>PlatformConf</code>.
     *
     * @return An {@link java.io.InputStream} containing the bytes
     * of this <code>PlatformConf</code>.
     * @exception  IOException if an I/O error occurs.
     **/
     InputStream getStream() throws IOException {
         Advertisement advertisement = newPlatformConfig();
         XMLDocument document = (XMLDocument) advertisement.getDocument(MimeMediaType.XMLUTF8);
         return document.getStream();
     }

    /**
     *  Set the JXTA_HOME (default is $CWD/.jxta)
     *
     *@param  home    The new home value
     */
    public static void setHome(File home) {
        factory.home = home;
        System.setProperty("JXTA_HOME", home.getAbsolutePath());
    }

    /**
     *  Sets the multicastPort (default 1234)
     *
     *@param  port  The new multicastPort value
     */
    public static void setMulticastPort(int port) {
        factory.mcastPort = port;
    }
    /**
     *  Sets the multicastPort (default 1234)
     *
     *@param  name  The new multicastPort value
     */
    public static void setName(String name) {
        factory.name = name;
    }

    /**
     *  Set the PeerID (A new PeerID will be generated if none set)
     *
     *@param  peerid  The new peerid value
     */
    public static void setPeerID(PeerID peerid) {
        factory.peerid = peerid;
    }

    /**
     *  Toggles whether a peer is relayed or not (default is relayed)
     *
     *@param  relayed  The new relayed value
     */
    public static void setRelayed(boolean relayed) {
        factory.relayed = relayed;
    }

    /**
     *  Toggles whether incoming is on or not (default is on)
     *
     *@param  incoming  The new tcpIncoming value
     */
    public static void setTcpIncoming(boolean incoming) {
        factory.tcp_incoming = incoming;
    }

    /**
     *  Toggles whether outgoing is on or not (default is on)
     *
     *@param  outgoing  The new tcpOutgoing value
     */
    public static void setTcpOutgoing(boolean outgoing) {
        factory.tcp_outgoing = outgoing;
    }

    /**
     *  Sets the listening port (default 9901)
     *
     *@param  port  The new tcpPort value
     */
    public static void setTcpPort(int port) {
        factory.tcpPort = port;
    }

    /**
     *  Toggles whether to use multicast (default use multicast)
     *
     *@param  mutlicastOn  The new useMulticast value
     */
    public static void setUseMulticast(boolean mutlicastOn) {
        factory.mutlicastOn = mutlicastOn;
    }
    /**
     *  Sets Rendezvous Seeding URI
     *
     *@param  seedURI  Rendezvous service seeding uri
     */
    public static void setRdvSeedingURI(URI seedURI) {
        factory.rdvSeedingURI = seedURI;
    }    
    /**
     *  Sets Relay Seeding URI
     *
     *@param  seedURI  Relay service seeding uri
     */
    public static void setRelaySeedingURI(URI seedURI) {
        factory.relaySeedingURI = seedURI;
    }

    /**
     *  Adds Redezvous peer Seed
     *
     *@param  uri  Redezvous peer seed uri
     */
    public static void addSeedRendezvous(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri may not be null");
        }
        factory.seedRendezvous.add(uri);
    }

    /**
     *  Adds Relay peer Seed
     *
     *@param  uri  Relay peer seed uri
     */
    public static void addSeedRelay(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri may not be null");
        }
        factory.seedRelay.add(uri);
    }
    
    /**
     * Sets the lowest port on which the TCP Transport will listen if configured
     * to do so. Valid values are <code>-1</code>, <code>0</code> and
     * <code>1-65535</code>. The <code>-1</code> value is used to signify that
     * the port range feature should be disabled. The <code>0</code> specifies
     * that the Socket API dynamic port allocation should be used. For values
     * <code>1-65535</code> the value must be equal to or less than the value
     * used for end port.
     *
     * @param start the lowest port on which to listen.
     * @param end the highest port on which to listen.
     */
    public static void setTCPPortRange(int start, int end) {
        factory.tcpStartPort = start;
        factory.tcpEndPort = end;
        factory.rangeSet = true;
    }

    /**
     * Sets PlaformConfig Peer Description
     *
     * @param description The description
     */
    public static void setDescription(String description) {
        factory.description = description;
    }

    /**
     *  Sets whether to only use defined relay seeds
     *
     *@param  useOnlyRelaySeeds  whether to only use relay seeds
     */
    public void setUseOnlyRelaySeeds(boolean useOnlyRelaySeeds) {
        factory.useOnlyRelaySeeds = useOnlyRelaySeeds;
    }
    /**
     *  Sets whether to only use defined rendezvous seeds
     *
     *@param  useOnlyRendezvouSeeds  whether to only use rendezvous seeds
     */
    public void setUseOnlyRendezvousSeeds(boolean useOnlyRendezvouSeeds) {
        factory.useOnlyRendezvouSeeds = useOnlyRendezvouSeeds;
    }
    
    /**
     *  Sets Infrastructure PeerGroup ID
     *
     *@param  peerGroupID    the Infrastructure PeerGroup ID
     */
    public static void setInfrastructureID(PeerGroupID peerGroupID) {
        if (peerGroupID == null || peerGroupID.equals(ID.nullID)) {
            throw new IllegalArgumentException("PeerGroup ID may not be null");
        }
        netPGID = peerGroupID;
    }
    /**
     *  Sets Infrastructure PeerGroup ID
     *
     *@return the Infrastructure PeerGroup ID
     */
    public static PeerGroupID getInfrastructureID() {
        return netPGID;
    }
}

