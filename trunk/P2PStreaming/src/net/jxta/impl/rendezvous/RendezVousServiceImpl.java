/*
 *  $Id: RendezVousServiceImpl.java,v 1.11 2006/06/21 00:07:39 hamada Exp $
 *
 *  Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
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
 *
 *  =========================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 */
package net.jxta.impl.rendezvous;

import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.document.XMLElement;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.endpoint.EndpointListener;
import net.jxta.endpoint.EndpointService;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.TextDocumentMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.id.ID;
import net.jxta.peer.PeerID;
import net.jxta.id.IDFactory;
import net.jxta.impl.id.UUID.UUID;
import net.jxta.impl.id.UUID.UUIDFactory;
import net.jxta.impl.protocol.RdvConfigAdv;
import net.jxta.impl.util.*;
import net.jxta.impl.util.TimeUtils;
import net.jxta.impl.util.TimerThreadNamer;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.ConfigParams;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.RdvAdvertisement;
import net.jxta.protocol.RouteAdvertisement;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.rendezvous.RendezVousStatus;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.endpoint.Messenger;

import net.jxta.service.Service;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *  A JXTA {@link net.jxta.rendezvous.RendezvousService} implementation which
 *  implements the standard JXTA Rendezvous Protocol (RVP).
 *
 *@see    net.jxta.rendezvous.RendezvousService
 *@see    <a href="http://spec.jxta.org/nonav/v1.0/docbook/JXTAProtocols.html#proto-rvp"
 *      target="_blank">JXTA Protocols Specification : Rendezvous Protocol</a>
 */
public class RendezVousServiceImpl implements RendezVousService, EndpointListener, PeerViewListener {
    private final static long MONITOR_INTERVAL = 20 * TimeUtils.ASECOND;
    private final static long ADDEVENT_DELAY = 3 * TimeUtils.ASECOND;
    private final static long CHALLENGE_TIMEOUT = 90 * TimeUtils.ASECOND;
    /**
     *  Description of the Field
     */
    public final static String ConnectRequest = "Connect";
    /**
     *  Description of the Field
     */
    public final static String ConnectedLeaseReply = "ConnectedLease";
    /**
     *  Description of the Field
     */
    public final static String ConnectedPeerReply = "ConnectedPeer";
    /**
     *  Description of the Field
     */
    public final static String ConnectedRdvAdvReply = "RdvAdvReply";
    // 5 Minutes

    private final static double DEMOTION_FACTOR = 0.05;
    private final static long DEMOTION_MIN_CLIENT_COUNT = 3;
    private final static long DEMOTION_MIN_PEERVIEW_COUNT = 5;
    /**
     *  Description of the Field
     */
    public final static String DisconnectRequest = "Disconnect";
    /**
     *  Description of the Field
     */
    protected transient String HEADER_NAME;
    private transient long LEASE_MARGIN = 5 * TimeUtils.AMINUTE;

    /**
     *  Log4J Logger
     */
    private final static transient Logger LOG = Logger.getLogger(RendezVousServiceImpl.class.getName());
    /**
     *  Description of the Field
     */
    protected final static int MAX_ADHOC_TTL = 2;
    /**
     *  Description of the Field
     */
    protected final static int MAX_INFRA_TTL = 2;
    /**
     *  Description of the Field
     */
    protected final static int MAX_MSGIDS = 100;

    /**
     *  Number of rendezvous we will try to connect to.
     */
    private final static int MAX_RDV_CONNECTIONS = 1;

    /**
     *  Description of the Field
     */
    protected final static String MESSAGE_NAMESPACE_NAME = "jxta";

    /**
     *  Description of the Field
     */
    protected String PropPName;
    /**
     *  Description of the Field
     */
    protected final static String PropSName = "JxtaPropagate";
    /**
     *  Description of the Field
     */
    public final static String RdvAdvReply = "RdvAdv";

    private PeerGroup advGroup = null;
    private ID assignedID = null;
    private boolean autoRendezvous = false;
    private PeerAdvertisement cachedPeerAdv = null;
    private XMLDocument cachedPeerAdvDoc = null;
    private int cachedPeerAdvModCount = -1;
    private EdgeProtocolListener edgeProtocolListener = null;
    private RdvConfigAdv.RendezVousConfiguration config = RdvConfigAdv.RendezVousConfiguration.EDGE;
    private int defaultTTL = MAX_ADHOC_TTL;

    /**
     *  <p/>
     *
     *
     *  <ul>
     *    <li> Keys are {@link net.jxta.peer.PeerID}.</li>
     *    <li> Values are {@link java.lang.Long} containing the time at which
     *    the rendezvous disconnected.</li>
     *  </ul>
     *
     */
    private final Set disconnectedRendezVous = Collections.synchronizedSet(new HashSet());
    /**
     *  Description of the Field
     */
    public EndpointService endpoint = null;
    // 5 Minutes

    private transient final Set eventListeners = Collections.synchronizedSet(new HashSet());

    private transient PeerGroup group = null;
    private transient ModuleImplAdvertisement implAdvertisement = null;
    private transient long maxChoiceDelay = ADDEVENT_DELAY;
    private transient int messagesReceived;

    /**
     *  Once choice delay has reached zero, any ADD event could trigger a
     *  attempt at connecting to one of the rdvs. If these events come in bursts
     *  while we're not yet connected, we might end-up doing many parallel
     *  attempts, which is a waste of bandwidth. Instead we refrain from doing
     *  more than one attempt every ADDEVENT_DELAY
     */
    private transient long monitorNotBefore = -1;

    /**
     *  This the time in absolute milliseconds at which the monitor is scheduled
     *  to start.The monitor will not be scheduled at all until there is at
     *  least one item in the peerview. The more items in the peerview, the
     *  earlier we start. Once there are at least rdvConfig.minHappyPeerView
     *  items it guaranteed that we start immediately because the start date is
     *  in the past.
     */
    private transient long monitorStartAt = -1;

    private transient final List msgIds = new ArrayList(MAX_MSGIDS);

    /**
     *  Description of the Field
     */
    protected transient String pName;
    /**
     *  Description of the Field
     */
    protected transient String pParam;
    private transient final Map propListeners = new HashMap();
    private final static Random random = new Random();

    /**
     *  <p/>
     *
     *
     *  <ul>
     *    <li> Keys are {@link net.jxta.peer.ID}.</li>
     *    <li> Values are {@link net.jxta.impl.rendezvous.RdvConnection}.</li>
     *
     *  </ul>
     *
     */
    private transient final Map rendezVous = Collections.synchronizedMap(new HashMap());

    /**
     *  The peer view for this peer group.
     */
    public transient volatile PeerView rpv = null;
    private transient final Timer timer = new Timer(true);

    /**
     *  {@inheritDoc}
     *
     *@param  listener  The feature to be added to the Listener attribute
     */
    public final void addListener(RendezvousListener listener) {
        eventListeners.add(listener);
    }


    /**
     *  Checks if a message id has been recorded
     *
     *@param  id  message to record.
     *@return     Description of the Return Value
     *@result     true if message was added otherwise (duplicate) false.
     */
    public boolean addMsgId(UUID id) {

        synchronized (msgIds) {
            if (isMsgIdRecorded(id)) {
                // Already there. Nothing to do
                return false;
            }

            if (msgIds.size() < MAX_MSGIDS) {
                msgIds.add(id);
            } else {
                msgIds.set((messagesReceived % MAX_MSGIDS), id);
            }

            messagesReceived++;
        }

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Added Message ID : " + id);
        }

        return true;
    }


    /**
     *  {@inheritDoc}
     *
     *@param  name      The feature to be added to the PropagateListener
     *      attribute
     *@param  listener  The feature to be added to the PropagateListener
     *      attribute
     *@return           Description of the Return Value
     */
    public synchronized boolean addPropagateListener(String name, EndpointListener listener) {

        // FIXME: jice@jxta.org - 20040726 - The naming of PropagateListener is inconsistent with that of EndpointListener. It is
        // not a major issue but is ugly since messages are always addressed with the EndpointListener convention. The only way to
        // fix it is to deprecate addPropagateListener in favor of a two argument version and wait for applications to adapt. Only
        // once that transition is over, will we be able to know where the separator has to be.

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Adding listener (" + listener + ") for name= " + name);
        }
        // Take the optimistic stance. Since we're synchronized, replace the current one, and if we find there was one and it's
        // not the same, put things back as they were.

        EndpointListener current = (EndpointListener) propListeners.put(name, listener);
        if ((current != null) && (current != listener)) {
            propListeners.put(name, current);
            return false;
        }
        return true;
    }


    /**
     *  {@inheritDoc}
     *
     *@param  serviceName   The feature to be added to the PropagateListener
     *      attribute
     *@param  serviceParam  The feature to be added to the PropagateListener
     *      attribute
     *@param  listener      The feature to be added to the PropagateListener
     *      attribute
     *@return               Description of the Return Value
     */
    public boolean addPropagateListener(String serviceName, String serviceParam, EndpointListener listener) {
        // Until the old API is killed, the new API behaves like the old one (so that name
        // collisions are still detected if both APIs are in use).
        return addPropagateListener(serviceName + serviceParam, listener);
    }


    /**
     *  Add a rendezvous to our collection of rendezvous peers.
     *
     *@param  padv   PeerAdvertisement for the rendezvous peer.
     *@param  lease  The duration of the lease in relative milliseconds.
     */
    private void addRdv(PeerAdvertisement padv, long lease) {

        int eventType;

        RdvConnection rdvConnection;

        synchronized (rendezVous) {
            rdvConnection = (RdvConnection) rendezVous.get(padv.getPeerID());
            if (null == rdvConnection) {
                rdvConnection = new RdvConnection(group, this, padv.getPeerID());
                rendezVous.put(padv.getPeerID(), rdvConnection);
                disconnectedRendezVous.remove(padv.getPeerID());
                eventType = RendezvousEvent.RDVCONNECT;
            } else {
                eventType = RendezvousEvent.RDVRECONNECT;
            }
        }
        rdvConnection.connect(padv, lease, Math.min(LEASE_MARGIN, (lease / 2)));
        generateEvent(eventType, padv.getPeerID());
    }


    /**
     *  {@inheritDoc}
     *
     *@param  peer   Description of the Parameter
     *@param  delay  Description of the Parameter
     */
    public void challengeRendezVous(ID peer, long delay) {
        challengeRendezVous(peer, delay);
    }


    /**
     *  {@inheritDoc}
     *
     *@param  msg  Description of the Parameter
     *@return      Description of the Return Value
     */
    protected RendezVousPropagateMessage checkPropHeader(Message msg) {

        RendezVousPropagateMessage propHdr;

        try {
            propHdr = getPropHeader(msg);

            if (null == propHdr) {
                // No header. Discard the message
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("Discarding " + msg + " -- missing propagate header.");
                }
                return null;
            }
        } catch (Exception failure) {
            // Bad header. Discard the message
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Discarding " + msg + " -- bad propagate header.", failure);
            }
            return null;
        }

        // Look at the Propagate header if any and check for loops.
        // Do not remove it; we do not have to change it yet, and we have
        // do look at it at different places and looking costs less on
        // incoming elements than on outgoing.

        // TTL detection. A message arriving with TTL <= 0 should not even
        // have been sent. Kill it.

        if (propHdr.getTTL() <= 0) {
            // This message is dead on arrival. Drop it.
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Discarding " + msg + "(" + propHdr.getMsgId() + ") -- dead on arrival (TTl=" + propHdr.getTTL() + ").");
            }
            return null;
        }

        if (!addMsgId(propHdr.getMsgId())) {
            // We already received this message - discard
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Discarding " + msg + "(" + propHdr.getMsgId() + ") -- feedback.");
            }
            return null;
        }

        // Loop detection
        if (propHdr.isVisited(group.getPeerID().toURI())) {
            // Loop is detected - discard.
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Discarding " + msg + "(" + propHdr.getMsgId() + ") -- loopback.");
            }
            return null;
        }

        // Message is valid
        return propHdr;
    }


    /**
     *  Connects to a random rendezvous from the peer view.
     */
    private void connectToRandomRdv() {

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Periodic rendezvous connect attempt for " + group.getPeerGroupID());
        }

        List currentView = new ArrayList(Arrays.asList(rpv.getView().toArray()));

        Collections.shuffle(currentView);

        while (!currentView.isEmpty()) {
            PeerViewElement pve = (PeerViewElement) currentView.remove(0);

            RdvAdvertisement radv = pve.getRdvAdvertisement();

            if (null == radv) {
                continue;
            }

            if (null != getPeerConnection(radv.getPeerID())) {
                continue;
            }

            try {
                newLeaseRequest(radv);
                break;
            } catch (IOException ez) {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("rdv connection failed.", ez);
                }
            }
        }
    }


    /**
     *  Force the peerview to use the given peer as a seed peer and force the
     *  edge rendezvous provider (if we're edge) to chose a rendezvous as soon
     *  as there is one (likely but not necessarily the one given).
     *
     *@param  addr          The addres of the seed peer (raw or peer id based)
     *@param  routeHint     Description of the Parameter
     *@throws  IOException  if it failed immediately.
     */
    private void connectToRendezVous(EndpointAddress addr, RouteAdvertisement routeHint) throws IOException {

        PeerView currView = rpv;
        if (null == currView) {
            throw new IOException("No PeerView");
        }

        // In order to mimic the past behaviour as closely as possible we add that peer to the seed list automatically and we
        // change the provider choice delay (edge peer only), so that it choses a rendezvous as soon as the suggested one is added
        // to the peerview. However, another seed rendezvous might beat it to the finish line (assuming there are other seeds).

        currView.addSeed(addr.toURI());
        setChoiceDelay(0);
        if (!currView.probeAddress(addr, routeHint)) {
            throw new IOException("Could not probe:" + addr);
        }
    }


    /**
     *  {@inheritDoc}
     */
    public void connectToRendezVous(PeerAdvertisement adv) throws IOException {

        EndpointAddress addr = new EndpointAddress("jxta", adv.getPeerID().getUniqueValue().toString(), null, null);
        connectToRendezVous(addr, extractRouteAdv(adv));
    }


    /**
     *  {@inheritDoc}
     */
    public void connectToRendezVous(EndpointAddress addr) throws IOException {
        connectToRendezVous(addr, null);
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public UUID createMsgId() {
        return UUIDFactory.newSeqUUID();
    }

    /**
     *  {@inheritDoc}
     *
     *@param  peerId  Description of the Parameter
     */
    public void disconnectFromRendezVous(ID peerId) {
        removeRdv((PeerID) peerId, false);
    }


    /**
     *  Description of the Method
     *
     *@param  adv  Description of the Parameter
     *@return      Description of the Return Value
     */
    public final static RouteAdvertisement extractRouteAdv(PeerAdvertisement adv) {

        try {
            // Get its EndpointService advertisement
            XMLElement endpParam = (XMLElement) adv.getServiceParam(PeerGroup.endpointClassID);

            if (endpParam == null) {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("No Endpoint Params");
                }
                return null;
            }

            // get the Route Advertisement element
            Enumeration paramChilds = endpParam.getChildren(RouteAdvertisement.getAdvertisementType());
            XMLElement param;

            if (paramChilds.hasMoreElements()) {
                param = (XMLElement) paramChilds.nextElement();
            } else {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("No Route Adv in Peer Adv");
                }
                return null;
            }

            // build the new route
            RouteAdvertisement route = (RouteAdvertisement) AdvertisementFactory.newAdvertisement((XMLElement) param);
            route.setDestPeerID(adv.getPeerID());
            return route;
        } catch (Exception e) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("failed to extract radv", e);
            }
        }
        return null;
    }


    /**
     *  {@inheritDoc}
     *
     *@exception  Throwable  Description of the Exception
     */
    protected void finalize() throws Throwable {
        stopApp();
        super.finalize();
    }


    /**
     *  Creates a rendezvous event and sends it to all registered listeners.
     *
     *@param  type       Description of the Parameter
     *@param  regarding  Description of the Parameter
     */
    public final void generateEvent(int type, ID regarding) {

        Iterator eachListener = Arrays.asList(eventListeners.toArray()).iterator();

        RendezvousEvent event = new RendezvousEvent(getInterface(), type, regarding);

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Calling listeners for " + event);
        }

        while (eachListener.hasNext()) {
            RendezvousListener aListener = (RendezvousListener) eachListener.next();

            try {
                aListener.rendezvousEvent(event);
            } catch (Throwable ignored) {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("Uncaught Throwable in listener (" + aListener + ")", ignored);
                }
            }
        }
    }


    /**
     *  Gets the assignedID attribute of the RendezVousServiceImpl object
     *
     *@return    The assignedID value
     */
    public ID getAssignedID() {
        return assignedID;
    }


    /**
     *  {@inheritDoc}
     *
     *@return    The connectedPeerIDs value
     */
    public Vector getConnectedPeerIDs() {
        return new Vector();
    }


    /**
     *  {@inheritDoc}
     */
    public Enumeration getConnectedPeers() {
        return getEmptyEnum();
    }


    /**
     *  {@inheritDoc}
     */
    public Enumeration getConnectedRendezVous() {
        return Collections.enumeration(Arrays.asList(rendezVous.keySet().toArray()));
    }


    /**
     *  {@inheritDoc}
     */
    public Enumeration getDisconnectedRendezVous() {
        List result = Arrays.asList(disconnectedRendezVous.toArray());
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug(result.size() + " rendezvous disconnections.");
        }
        
        return Collections.enumeration(result);
    }


    /**
     *  Gets the emptyEnum attribute of the RendezVousServiceImpl object
     *
     *@return    The emptyEnum value
     */
    private static Enumeration getEmptyEnum() {
        return Collections.enumeration(Collections.EMPTY_LIST);
    }


    /**
     *  {@inheritDoc}
     */
    public Advertisement getImplAdvertisement() {
        return implAdvertisement;
    }


    /**
     *  {@inheritDoc}
     */
    public Service getInterface() {
        return this;
    }

    /**
     *  Gets the listener attribute of the RendezVousServiceImpl object
     *
     *@param  str  Description of the Parameter
     *@return      The listener value
     */
    public synchronized EndpointListener getListener(String str) {
        return (EndpointListener) propListeners.get(str);
    }


    /**
     *  {@inheritDoc}
     */
    public Vector getLocalWalkView() {

        Vector tmp = new Vector();

        PeerView currView = rpv;
        if (null == currView) {
            return tmp;
        }
        Iterator eachPVE = Arrays.asList(currView.getView().toArray()).iterator();
        while (eachPVE.hasNext()) {
            PeerViewElement peer = (PeerViewElement) eachPVE.next();
            RdvAdvertisement adv = peer.getRdvAdvertisement();
            tmp.add(adv);
        }
        return tmp;
    }


    /**
     *  Gets the peerAdvertisementDoc attribute of the RendezVousServiceImpl
     *  object
     *
     *@return    The peerAdvertisementDoc value
     */
    protected XMLDocument getPeerAdvertisementDoc() {
        PeerAdvertisement newPadv = null;

        synchronized (this) {
            newPadv = group.getPeerAdvertisement();
            int newModCount = newPadv.getModCount();

            if ((cachedPeerAdv != newPadv) || (cachedPeerAdvModCount != newModCount)) {
                cachedPeerAdv = newPadv;
                cachedPeerAdvModCount = newModCount;
            } else {
                newPadv = null;
            }

            if (null != newPadv) {
                cachedPeerAdvDoc = (XMLDocument) cachedPeerAdv.getDocument(MimeMediaType.XMLUTF8);
            }
        }
        return cachedPeerAdvDoc;
    }


    /**
     *@inheritDoc
     */
    public PeerConnection getPeerConnection(ID peer) {
        return (PeerConnection) rendezVous.get(peer);
    }


    /**
     *@inheritDoc
     */
    protected PeerConnection[] getPeerConnections() {
        return (PeerConnection[]) rendezVous.values().toArray(new PeerConnection[0]);
    }


    /**
     *  Get propagate header from the message.
     *
     *@param  msg  The source message.
     *@return      The message's propagate header if any, otherwise null.
     */
    protected RendezVousPropagateMessage getPropHeader(Message msg) {

        MessageElement elem = msg.getMessageElement(MESSAGE_NAMESPACE_NAME, HEADER_NAME);

        if (elem == null) {
            return null;
        }

        try {
            StructuredDocument asDoc = StructuredDocumentFactory.newStructuredDocument(elem.getMimeType(), elem.getStream());

            return new RendezVousPropagateMessage(asDoc);
        } catch (IOException failed) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Could not get prop header of " + msg, failed);
            }

            IllegalArgumentException failure = new IllegalArgumentException("Could not get prop header of " + msg);

            failure.initCause(failed);
            throw failure;
        }
    }


    /**
     *@inheritDoc
     */
    public RendezVousStatus getRendezVousStatus() {
        return RendezVousStatus.EDGE;
    }


    /**
     *  {@inheritDoc} <p/>
     *
     *  <b>Note</b> : it is permissible to pass null as the impl parameter when
     *  this instance is not being loaded via the module framework.
     */
    public synchronized void init(PeerGroup group, ID assignedID, Advertisement impl) {

        this.group = group;
        this.assignedID = assignedID;
        this.implAdvertisement = (ModuleImplAdvertisement) impl;
        PropPName = group.getPeerGroupID().getUniqueValue().toString();
        HEADER_NAME = RendezVousPropagateMessage.Name + PropPName;
        pParam = group.getPeerGroupID().getUniqueValue().toString();
        pName = assignedID.toString();
        timer.schedule(new TimerThreadNamer("RendezVousServiceImpl Timer for " + group.getPeerGroupID()), 0);

        advGroup = group.getParentGroup();
        if ((null == advGroup) || PeerGroupID.worldPeerGroupID.equals(advGroup.getPeerGroupID())) {
            // For historical reasons, we publish in our own group rather than
            // the parent if our parent is the world group.
            advGroup = group;
        }

        ConfigParams confAdv = (ConfigParams) group.getConfigAdvertisement();

        // Get the config. If we do not have a config, we're done; we just keep
        // the defaults (edge peer/no auto-rdv)
        if (confAdv != null) {
            Advertisement adv = null;

            try {
                XMLDocument configDoc = (XMLDocument) confAdv.getServiceParam(getAssignedID());
                if (null != configDoc) {
                    // XXX 20041027 backwards compatibility
                    configDoc.addAttribute("type", RdvConfigAdv.getAdvertisementType());
                    adv = AdvertisementFactory.newAdvertisement(configDoc);
                }
            } catch (NoSuchElementException failed) {

            }

            if (adv instanceof RdvConfigAdv) {
                RdvConfigAdv rdvConfigAdv = (RdvConfigAdv) adv;
                config = rdvConfigAdv.getConfiguration();
                autoRendezvous = rdvConfigAdv.getAutoRendezvousCheckInterval() > 0;
                //rdv_watchdog_interval = rdvConfigAdv.getAutoRendezvousCheckInterval();
            }
        }

        if (PeerGroupID.worldPeerGroupID.equals(group.getPeerGroupID())) {
            config = RdvConfigAdv.RendezVousConfiguration.AD_HOC;
        }

        if (LOG.isEnabledFor(Level.INFO)) {
            StringBuffer configInfo = new StringBuffer("Configuring RendezVous Service : " + assignedID);

            if (implAdvertisement != null) {
                configInfo.append("\n\tImplementation :");
                configInfo.append("\n\t\tModule Spec ID: " + implAdvertisement.getModuleSpecID());
                configInfo.append("\n\t\tImpl Description : " + implAdvertisement.getDescription());
                configInfo.append("\n\t\tImpl URI : " + implAdvertisement.getUri());
                configInfo.append("\n\t\tImpl Code : " + implAdvertisement.getCode());
            }

            configInfo.append("\n\tGroup Params :");
            configInfo.append("\n\t\tGroup : " + group.getPeerGroupName());
            configInfo.append("\n\t\tGroup ID : " + group.getPeerGroupID());
            configInfo.append("\n\t\tPeer ID : " + group.getPeerID());

            configInfo.append("\n\tConfiguration :");
            if (null != advGroup) {
                configInfo.append("\n\t\tAdvertising group : " + advGroup.getPeerGroupName() + " [" + advGroup.getPeerGroupID() + "]");
            } else {
                configInfo.append("\n\t\tAdvertising group : (none)");
            }
            configInfo.append("\n\t\tRendezVous : " + config);
            configInfo.append("\n\t\tAuto RendezVous : " + autoRendezvous);
            //configInfo.append("\n\t\tAuto-RendezVous Reconfig Interval : " + rdv_watchdog_interval);

            LOG.info(configInfo);
        }
    }


    /**
     *  Gets the rendezvousConnected attribute of the RendezVousServiceImpl
     *  object
     *
     *@return    true if connected to a rendezvous, false otherwise
     */
    public boolean isConnectedToRendezVous() {
        return !rendezVous.isEmpty();
    }


    /**
     *  Gets the msgIdRecorded attribute of the RendezVousServiceImpl object
     *
     *@param  id  Description of the Parameter
     *@return     The msgIdRecorded value
     */
    public boolean isMsgIdRecorded(UUID id) {

        boolean found;

        synchronized (msgIds) {
            found = msgIds.contains(id);
        }

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug(id + " = " + found);
        }

        return found;
    }


    /**
     *  {@inheritDoc}
     */
    public boolean isRendezVous() {
        RendezVousStatus currentStatus = getRendezVousStatus();
        return (RendezVousStatus.AUTO_RENDEZVOUS == currentStatus) || (RendezVousStatus.RENDEZVOUS == currentStatus);
    }


    /**
     *  Convenience method for constructing an endpoint address from an id
     *
     *@param  destPeer  peer id
     *@param  serv      the service name (if any)
     *@param  parm      the service param (if any)
     *@return           Description of the Return Value
     */
    protected final static EndpointAddress mkAddress(String destPeer, String serv, String parm) {

        ID asID = null;

        try {
            asID = IDFactory.fromURI(new URI(destPeer));
        } catch (URISyntaxException caught) {
            throw new IllegalArgumentException(caught.getMessage());
        }

        return mkAddress(asID, serv, parm);
    }


    /**
     *  Convenience method for constructing an endpoint address from an id
     *
     *@param  destPeer  peer id
     *@param  serv      the service name (if any)
     *@param  parm      the service param (if any)
     *@return           Description of the Return Value
     */
    protected final static EndpointAddress mkAddress(ID destPeer, String serv, String parm) {

        EndpointAddress addr = new EndpointAddress(MESSAGE_NAMESPACE_NAME, destPeer.getUniqueValue().toString(), serv, parm);

        return addr;
    }


    /**
     *  Attempt to connect to a rendezvous we have not previously connected to.
     *
     *@param  radv             Rendezvous advertisement for the Rdv we want to
     *      connect to.
     *@exception  IOException  Description of the Exception
     */
    private void newLeaseRequest(RdvAdvertisement radv) throws IOException {

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Sending new lease request to " + radv.getPeerID());
        }
        EndpointAddress addr = mkAddress(radv.getPeerID(), null, null);
        RouteAdvertisement hint = radv.getRouteAdv();
        Messenger messenger = endpoint.getMessenger(addr, hint);

        if (null == messenger) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Could not get messenger for " + addr);
            }
            throw new IOException("Could not connect to " + addr);
        }

        Message msg = new Message();
        // The request simply includes the local peer advertisement.
        msg.replaceMessageElement("jxta", new TextDocumentMessageElement(ConnectRequest, getPeerAdvertisementDoc(), null));
        messenger.sendMessage(msg, pName, pParam);
    }


    /**
     *  Adds a propagation header to the given message with the given default
     *  TTL. Also adds this peer to the path recorded in the message.
     *
     *@param  serviceName   Description of the Parameter
     *@param  serviceParam  Description of the Parameter
     *@param  ttl           Description of the Parameter
     *@return               Description of the Returned Value
     */
    private RendezVousPropagateMessage newPropHeader(String serviceName, String serviceParam, int ttl) {

        RendezVousPropagateMessage propHdr = new RendezVousPropagateMessage();
        propHdr.setTTL(ttl);
        propHdr.setDestSName(serviceName);
        propHdr.setDestSParam(serviceParam);
        UUID msgID = createMsgId();
        propHdr.setMsgId(msgID);
        addMsgId(msgID);
        // Add this peer to the path.
        propHdr.addVisited(group.getPeerID().toURI());
        return propHdr;                               
    }


    /**
     *  {@inheritDoc}
     */
    public void peerViewEvent(PeerViewEvent event) {

        int theEventType = event.getType();

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("[" + group.getPeerGroupName() + "] Processing " + event);
        }

        switch (theEventType) {
            case PeerViewEvent.ADD:
                synchronized (this) {
                    try {
                        // There is a new rdv in the peerview. If we are not
                        // connected, it is worth a try, right away.

                        // Use the single timer thread rather than doing it
                        // from this thread which belongs to the invoker.
                        // This removes risks of dealocks and other calamities.
                        // All we have to do is to change the schedule.

                        if (!rendezVous.isEmpty()) {
                            break;
                        }

                        // We do not act upon every single add event. If they
                        // come in storms as they do during boot, it would
                        // make us launch many immediate attempts in parallel,
                        // which causes useless traffic.  As long as
                        // choiceDelay is not exhausted we just reschedule
                        // accordingly. Once choiceDelay is exhausted, we
                        // schedule for immediate execution, but only if we
                        // haven't done so in the last ADDEVENT_DELAY.

                        long choiceDelay;

                        if (monitorStartAt == -1) {
                            // The startDate had never been decided. Initialize it now.
                            choiceDelay = maxChoiceDelay;
                            monitorStartAt = TimeUtils.toAbsoluteTimeMillis(choiceDelay);
                        } else {
                            choiceDelay = TimeUtils.toRelativeTimeMillis(monitorStartAt);
                        }

                        if (choiceDelay <= 0) {
                            if (TimeUtils.toRelativeTimeMillis(monitorNotBefore) > 0) {
                                break;
                            }
                            monitorNotBefore = TimeUtils.toAbsoluteTimeMillis(ADDEVENT_DELAY);
                            choiceDelay = 0;
                        } else {
                            monitorStartAt -= ADDEVENT_DELAY;
                        }

                        // Either way, we're allowed to (re) schedule; possibly immediately.

                        if (LOG.isEnabledFor(Level.DEBUG)) {
                            LOG.debug("Scheduling rdv monitor in " + choiceDelay + "ms.");
                        }

                        timer.schedule(new MonitorTask(), choiceDelay, MONITOR_INTERVAL);
                    } catch (Exception anything) {
                        if (LOG.isEnabledFor(Level.WARN)) {
                            LOG.warn("Event could not be processed", anything);
                        }
                        // Don't do it, then. The likely cause is that this
                        // monitor is being closed.
                    }
                }
                break;
            case PeerViewEvent.REMOVE:
            case PeerViewEvent.FAIL:
                PeerViewElement pve = event.getPeerViewElement();
                ID failedPVE = pve.getRdvAdvertisement().getPeerID();
                RdvConnection pConn = (RdvConnection) rendezVous.get(failedPVE);
                if (null != pConn) {
                    pConn.setConnected(false);
                    removeRdv(pConn.getPeerID(), false);
                }
                break;
            default:
                break;
        }
    }


    /**
     *  Process a connected replay
     *
     *@param  msg  Description of Parameter
     */
    protected void processConnectedReply(Message msg) {
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Processing a connect reply");
        }
        // get the Peer Advertisement of the RDV.
        MessageElement elem = msg.getMessageElement("jxta", ConnectedRdvAdvReply);
        if (null == elem) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("missing rendezvous peer advertisement");
            }
            return;
        }

        InputStream is = null;
        PeerAdvertisement padv = null;
        try {
            is = elem.getStream();
            padv = (PeerAdvertisement) AdvertisementFactory.newAdvertisement(elem.getMimeType(), is);
            // This is not our own peer adv so we must not keep it
            // longer than its expiration time.
            DiscoveryService discovery = group.getDiscoveryService();
            if (null != discovery) {
                discovery.publish(padv, DiscoveryService.DEFAULT_EXPIRATION, DiscoveryService.DEFAULT_EXPIRATION);
            }
        } catch (Exception e) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("failed to publish Rendezvous Advertisement", e);
            }
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException ignored) {
                    ;
                }
            }
            is = null;
        }

        if (null == padv) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("missing rendezvous peer advertisement");
            }
            return;
        }

        long lease;
        try {
            MessageElement el = msg.getMessageElement("jxta", ConnectedLeaseReply);
            if (el == null) {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("missing lease");
                }
                return;
            }
            lease = Long.parseLong(el.toString());
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Got a lease for :"+lease);
            }
        } catch (Exception e) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Parse lease failed with ", e);
            }
            return;
        }

        ID pId;
        MessageElement el = msg.getMessageElement("jxta", ConnectedPeerReply);
        if (el == null) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("missing rdv peer");
            }
            return;
        }
        try {
            pId = IDFactory.fromURI(new URI(el.toString()));
        } catch (URISyntaxException badID) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Bad RDV peer ID");
            }
            return;
        }

        String rdvName = padv.getName();
        if (null == padv.getName()) {
            rdvName = pId.toString();
        }
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("RDV Connect Response : peer=" + rdvName + " lease=" + lease + "ms");
        }

        if (lease <= 0) {
            removeRdv(pId, false);
        } else {
            if (rendezVous.containsKey(pId)
                     || ((rendezVous.size() < MAX_RDV_CONNECTIONS) && (rpv.getPeerViewElement(pId) != null))) {
                addRdv(padv, lease);
            } else {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    // XXX bondolo 20040423 perhaps we should send a disconnect here.
                    LOG.debug("Ignoring lease offer from " + rdvName);
                }
            }
        }
    }


    /**
     *  Handle a disconnection request from a remote peer.
     *
     *@param  msg  Description of Parameter
     */
    protected void processDisconnectRequest(Message msg) {

        try {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Processing a disconnect request");
            }
            MessageElement elem = msg.getMessageElement("jxta", DisconnectRequest);
            if (null != elem) {
                InputStream is = elem.getStream();
                PeerAdvertisement adv = (PeerAdvertisement) AdvertisementFactory.newAdvertisement(elem.getMimeType(), is);

                RdvConnection rdvConnection = (RdvConnection) rendezVous.get(adv.getPeerID());

                if (null != rdvConnection) {
                    rdvConnection.setConnected(false);
                    removeRdv(adv.getPeerID(), true);
                } else {
                    if (LOG.isEnabledFor(Level.DEBUG)) {
                        LOG.debug("Ignoring disconnect request from " + adv.getPeerID());
                    }
                }
            }
        } catch (Exception failure) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Failure processing disconnect request", failure);
            }
        }
    }

    /**
     *  {@inheritDoc}
     */
    public void processIncomingMessage(Message msg, EndpointAddress srcAddr, EndpointAddress dstAddr) {
        //MessageUtil.printMessageStats(msg, true);
        RendezVousPropagateMessage propHdr = checkPropHeader(msg);
        if (null != propHdr) {
            // Get the destination real destination of the message
            String sName = propHdr.getDestSName();
            String sParam = propHdr.getDestSParam();
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Processing " + msg + "(" + propHdr.getMsgId() + ") for " + sName + "/" + sParam + " from " + srcAddr);
            }
            // Check if we have a local listener for this message
            processReceivedMessage(msg, propHdr, srcAddr, new EndpointAddress(dstAddr, sName, sParam));
        }
    }
    
    protected void processReceivedMessage(Message msg, RendezVousPropagateMessage propHdr, EndpointAddress srcAddr, EndpointAddress dstAddr) {
        EndpointListener listener = getListener(dstAddr.getServiceName() + dstAddr.getServiceParameter());
        if (listener != null) {
            // We have a local listener for this message.
            // Deliver it.
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Calling local listener for [" +
                          dstAddr.getServiceName() + dstAddr.getServiceParameter() +
                          "] with " + msg + " ("+ propHdr.getMsgId() + ")");
                          
            }
            try {
                listener.processIncomingMessage(msg, srcAddr, dstAddr);
            } catch (Throwable ignored) {
                if (LOG.isEnabledFor(Level.ERROR)) {
                    LOG.error("Uncaught Throwable during callback of (" + listener + ") to " + dstAddr, ignored);
                }
            }
        } else if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("No listener for [" +
                      dstAddr.getServiceName() + dstAddr.getServiceParameter() +
                      "] with " + msg + " ("+ propHdr.getMsgId() + ")");
        }
    }


    /**
     *  Receive and publish a Rendezvous Peer Advertisement.
     *
     *@param  msg  Message containing the Rendezvous Peer Advertisement
     */
    protected void processRdvAdvReply(Message msg) {
        try {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Processing a rendezvous reply");
            }
            MessageElement elem = msg.getMessageElement("jxta", RdvAdvReply);
            if (null != elem) {
                PeerAdvertisement adv = (PeerAdvertisement) AdvertisementFactory.newAdvertisement(elem.getMimeType(), elem.getStream());
                DiscoveryService discovery = group.getDiscoveryService();
                if (null != discovery) {
                    discovery.publish(adv, DiscoveryService.DEFAULT_EXPIRATION, DiscoveryService.DEFAULT_EXPIRATION);
                }
            }
        } catch (Exception failed) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Publish Rdv Adv failed", failed);
            }
        }
    }


    /**
     *  {@inheritDoc}
     */
    public void propagate(Message msg,
                          String serviceName,
                          String serviceParam,
                          int ttl) throws IOException {

        ttl = Math.min(ttl, defaultTTL);
        RendezVousPropagateMessage propHdr = updatePropHeader(msg, getPropHeader(msg), serviceName, serviceParam, ttl);
        if (null != propHdr) {
            sendToEachConnection(msg, propHdr);
            sendToNetwork(msg, propHdr);
        } else {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Declining to propagate " + msg + " (No prop header)");
            }
        }
    }


    /**
     *  {@inheritDoc}
     */
    public void propagate(Enumeration destPeerIDs,
            Message msg,
            String serviceName,
            String serviceParam,
            int ttl) throws IOException {

        propagate(destPeerIDs,
                msg,
                serviceName,
                serviceParam,
                ttl);
    }

    /**
     *  {@inheritDoc}
     */
    public void propagateInGroup(Message msg,
            String serviceName,
            String serviceParam,
            int ttl) throws IOException {

        ttl = Math.min(ttl, defaultTTL);
        RendezVousPropagateMessage propHdr = updatePropHeader(msg, getPropHeader(msg), serviceName, serviceParam, ttl);
        if (null != propHdr) {
            sendToEachConnection(msg, propHdr);
        } else {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Declining to propagate " + msg + " (No prop header)");
            }
        }
    }

    /**
     *  {@inheritDoc}
     */
    public void propagateToNeighbors(Message msg,
                                     String serviceName,
                                     String serviceParam,
                                     int ttl) throws IOException {

        ttl = Math.min(defaultTTL, ttl);
        
        RendezVousPropagateMessage propHdr = updatePropHeader(msg, getPropHeader(msg), serviceName, serviceParam, ttl);
        
        if (null != propHdr) {
            try {
                sendToNetwork(msg, propHdr);
            } catch (IOException failed) {
                throw failed;
            }
        }
    }


    /**
     *  {@inheritDoc}
     */
    public final boolean removeListener(RendezvousListener listener) {

        return eventListeners.remove(listener);
    }


    /**
     *  {@inheritDoc}
     */
    public synchronized EndpointListener removePropagateListener(String name, EndpointListener listener) {

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Removing listener (" + listener + ") for name= " + name);
        }

        // Take the optimistic stance. Since we're synchronized, remove it, and if we find the invoker is cheating. Put it back.

        EndpointListener current = (EndpointListener) propListeners.remove(name);
        if ((current != null) && (current != listener)) {
            propListeners.put(name, current);
            return null;
        }

        return current;
    }


    /**
     *  {@inheritDoc}
     */
    public EndpointListener removePropagateListener(String serviceName, String serviceParam,
            EndpointListener listener) {

        // Until the old API is killed, the new API behaves like the old one (so that name
        // collisions are still detected if both APIs are in use).
        return removePropagateListener(serviceName + serviceParam, listener);
    }


    /**
     *  Remove the specified rendezvous from our collection of rendezvous.
     *
     *@param  rdvid      the id of the rendezvous to remove.
     *@param  requested  Description of the Parameter
     */
    private void removeRdv(ID rdvid, boolean requested) {

        if (LOG.isEnabledFor(Level.INFO)) {
            LOG.info("Disconnect from RDV " + rdvid);
        }

        PeerConnection rdvConnection;

        synchronized (this) {
            rdvConnection = (PeerConnection) rendezVous.remove(rdvid);

            // let's add it to the list of disconnected rendezvous
            if (null != rdvConnection) {
                disconnectedRendezVous.add(rdvid);
            }
        }

        if (null != rdvConnection) {
            if (rdvConnection.isConnected()) {
                rdvConnection.setConnected(false);
                sendDisconnect(rdvConnection);
            }
        }

        /*
         *  Remove the rendezvous we are disconnecting from the peerview as well.
         *  This prevents us from immediately reconnecting to it.
         */
        rpv.notifyFailure((PeerID) rdvid, false);
        generateEvent(requested ? RendezvousEvent.RDVDISCONNECT : RendezvousEvent.RDVFAILED, rdvid);
    }


    /**
     *  {@inheritDoc}
     */
    protected void repropagate(Message msg, RendezVousPropagateMessage propHdr, String serviceName, String serviceParam) {

        try {
            propHdr = updatePropHeader(msg, propHdr, serviceName, serviceParam, defaultTTL);

            if (null != propHdr) {
                sendToEachConnection(msg, propHdr);
                sendToNetwork(msg, propHdr);
            } else {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("Declining to repropagate " + msg + " (No prop header)");
                }
            }
        } catch (Exception ez1) {
            // Not much we can do
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Could not repropagate " + msg, ez1);
            }
        }
    }


    /**
     *  Sends a disconnect message to the specified peer.
     *
     *@param  peerid  Description of the Parameter
     *@param  padv    Description of the Parameter
     */
    protected void sendDisconnect(PeerID peerid, PeerAdvertisement padv) {

        Message msg = new Message();
        // The request simply includes the local peer advertisement.
        try {
            msg.replaceMessageElement("jxta", new TextDocumentMessageElement(DisconnectRequest, getPeerAdvertisementDoc(), null));
            EndpointAddress addr = mkAddress(peerid, null, null);
            RouteAdvertisement hint = null;
            if (null != padv) {
                hint = RendezVousServiceImpl.extractRouteAdv(padv);
            }

            Messenger messenger = endpoint.getMessenger(addr, null);
            if (null == messenger) {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("Could not get messenger for " + peerid);
                }
                return;
            }
            messenger.sendMessage(msg, pName, pParam);
        } catch (Exception e) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("sendDisconnect failed", e);
            }
        }
    }


    /**
     *  Sends a disconnect message to the specified peer.
     *
     *@param  pConn  Description of the Parameter
     */
    protected void sendDisconnect(PeerConnection pConn) {

        Message msg = new Message();
        // The request simply includes the local peer advertisement.
        try {
            msg.replaceMessageElement("jxta", new TextDocumentMessageElement(DisconnectRequest, getPeerAdvertisementDoc(), null));
            pConn.sendMessage(msg, pName, pParam);
        } catch (Exception e) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("sendDisconnect failed", e);
            }
        }
    }


    /**
     *  Description of the Method
     *
     *@param  pConn            Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    private void sendLeaseRequest(RdvConnection pConn) throws IOException {
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Sending Lease request to " + pConn);
        }
        Message msg = new Message();
        // The request simply includes the local peer advertisement.
        msg.replaceMessageElement("jxta", new TextDocumentMessageElement(ConnectRequest, getPeerAdvertisementDoc(), null));
        pConn.sendMessage(msg, pName, pParam);
    }


    /**
     *  Sends to all connected peers. <p/>
     *
     *  Note: The original msg is not modified and may be reused upon return.
     *
     *@param  msg      is the message to propagate.
     *@param  propHdr  Description of the Parameter
     *@return          Description of the Return Value
     */
    protected int sendToEachConnection(Message msg, RendezVousPropagateMessage propHdr) {

        int sentToPeers = 0;

        List peers = Arrays.asList(getPeerConnections());
        Iterator eachClient = peers.iterator();
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Sending to rendezvous connection :"+peers.size());
        }
        while (eachClient.hasNext()) {
            PeerConnection pConn = (PeerConnection) eachClient.next();

            // Check if this rendezvous has already processed this propagated message.
            if (!pConn.isConnected()) {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("Skipping " + pConn + " for " + msg + "(" + propHdr.getMsgId() + ") -- disconnected.");
                }
                // next!
                continue;
            }
            if (propHdr.isVisited(pConn.getPeerID().toURI())) {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("Skipping " + pConn + " for " + msg + "(" + propHdr.getMsgId() + ") -- already visited.");
                }
                // next!
                continue;
            }
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Sending " + msg + "(" + propHdr.getMsgId() + ") to " + pConn);
            }
            if (pConn.sendMessage((Message) msg.clone(), PropSName, PropPName)) {
                sentToPeers++;
            }
        }
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Sent " + msg + "(" + propHdr.getMsgId() + ") to " + sentToPeers + " of " + peers.size() + " peers.");
        }
        return sentToPeers;
    }


    /**
     *  Propagates on all endpoint protocols. <p/>
     *
     *  Note: The original msg is not modified and may be reused upon return.
     *
     *@param  msg              is the message to propagate.
     *@param  propHdr          Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    protected void sendToNetwork(Message msg, RendezVousPropagateMessage propHdr) throws IOException {

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Endpoint propagating " + msg + " (" + propHdr.getMsgId() + ")");
        }
        endpoint.propagate((Message) msg.clone(), PropSName, PropPName);
    }


    /**
     *  (@inheritDoc}
     */
    public boolean setAutoStart(boolean auto) {
        return false;
    }


    /**
     *  (@inheritDoc}
     */
    public synchronized boolean setAutoStart(boolean auto, long period) {
        return false;
    }


    /**
     *  {@inheritDoc}
     */
    public void setChoiceDelay(long delay) {
        monitorStartAt = TimeUtils.toAbsoluteTimeMillis(delay);
    }


    /**
     *  {@inheritDoc}
     */
    public int startApp(String[] arg) {
        endpoint = group.getEndpointService();

        if (null == endpoint) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Stalled until there is an endpoint service");
            }
            return START_AGAIN_STALLED;
        }

        Service needed = group.getMembershipService();
        try {

            // This must stay despite the call to addPropagateListener below.
            // The somewhat equivalent call done inside addPropagateListener
            // may be removed in the future and this here would remain the only
            // case were both a propagate listener and an endpoint listener are connected.

            if (!endpoint.addIncomingMessageListener(this, PropSName, PropPName)) {
                if (LOG.isEnabledFor(Level.ERROR)) {
                    LOG.error("Cannot register the propagation listener (already registered)");
                }
            }
            addPropagateListener(PropSName + PropPName, this);
        } catch (Exception ez1) {
            // Not much we can do here.
            if (LOG.isEnabledFor(Level.ERROR)) {
                LOG.error("Failed registering the propagation listener", ez1);
            }
        }

/*
        if (null == needed) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Stalled until there is a membership service");
            }
            return START_AGAIN_STALLED;
        }
*/
        // Create the PeerView instance
        if (!PeerGroupID.worldPeerGroupID.equals(group.getPeerGroupID())) {
            rpv = new PeerView(group, advGroup, this, getAssignedID().toString() + group.getPeerGroupID().getUniqueValue().toString());
            rpv.start();
            rpv.addListener(this);
        }

        edgeProtocolListener = new EdgeProtocolListener(this);
        endpoint.addIncomingMessageListener(edgeProtocolListener, pName, null);
        
        generateEvent(RendezvousEvent.BECAMEEDGE, group.getPeerID());

        if (LOG.isEnabledFor(Level.INFO)) {
            LOG.info("Rendezvous Serivce started");
        }
        return 0;
    }


    /**
     *  {@inheritDoc}
     *
     *@throws  will  always throw a runtime exception as it is not a supported
     *      operation
     */
    public void startRendezVous() {
        throw new RuntimeException("Operation not supported");
    }

    /**
     *  {@inheritDoc}
     */
    public synchronized void stopApp() {

        EndpointListener shouldbehandler = endpoint.removeIncomingMessageListener(pName, null);
        if (shouldbehandler != edgeProtocolListener) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Unregistered listener was not as expected." + shouldbehandler + " != " + this);
            }
        }
        shouldbehandler = endpoint.removeIncomingMessageListener(PropSName, PropPName);
        if (shouldbehandler != this) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Unregistered listener was not as expected." + shouldbehandler + " != " + this);
            }
        }

        Iterator eachListener = propListeners.keySet().iterator();
        while (eachListener.hasNext()) {
            String aListener = (String) eachListener.next();
            try {
                endpoint.removeIncomingMessageListener(aListener, null);
            } catch (Exception ignored) {

            }
            eachListener.remove();
        }
        propListeners.clear();
        timer.cancel();
        msgIds.clear();
        eventListeners.clear();
        if (LOG.isEnabledFor(Level.INFO)) {
            LOG.info("Rendezvous Serivce stopped");
        }
    }


    /**
     *  {@inheritDoc}
     */
    public void stopRendezVous() {
    }


    /**
     *  Description of the Method
     *
     *@param  msg           propagated message
     *@param  propHdr       header element
     *@param  serviceName   service name
     *@param  serviceParam  service param
     *@param  ttl           time to live
     *@return               RendezVous Propagate Message
     */
    protected RendezVousPropagateMessage updatePropHeader(Message msg, RendezVousPropagateMessage propHdr, String serviceName, String serviceParam, int ttl) {

        boolean newHeader = false;

        if (null == propHdr) {
            propHdr = newPropHeader(serviceName, serviceParam, ttl);
            newHeader = true;
        } else {
            if (null == updatePropHeader(propHdr, ttl)) {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("TTL expired for " + msg + " (" + propHdr.getMsgId() + ") ttl=" + propHdr.getTTL());
                }
                return null;
            }
        }

        XMLDocument propHdrDoc = (XMLDocument) propHdr.getDocument(MimeMediaType.XMLUTF8);
        MessageElement elem = new TextDocumentMessageElement(HEADER_NAME, propHdrDoc, null);

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug((newHeader ? "Added" : "Updated") + " prop header for " + msg + " (" + propHdr.getMsgId() + ") ttl=" + propHdr.getTTL());
        }
        msg.replaceMessageElement(MESSAGE_NAMESPACE_NAME, elem);
        return propHdr;
    }


    /**
     *  Updates the propagation header of the message. Also adds this peer to
     *  the path recorded in the message. Returns true if the message should be
     *  repropagated, false otherwise.
     *
     *@param  propHdr  The propHdr for the message.
     *@param  maxTTL   The maximum TTL which will be allowed.
     *@return          The updated propagate header if the message should be
     *      repropagated otherwise null.
     */
    private RendezVousPropagateMessage updatePropHeader(RendezVousPropagateMessage propHdr, int maxTTL) {
        int msgTTL = propHdr.getTTL();
        URI me = group.getPeerID().toURI();
        int useTTL = msgTTL;
        if (!propHdr.isVisited(me)) {
            // only reduce TTL if message has not previously visited us.
            useTTL--;
        }
        // ensure TTL does not exceed maxTTL
        useTTL = Math.min(useTTL, maxTTL);
        propHdr.setTTL(useTTL);
        // Add this peer to the path.
        propHdr.addVisited(me);
        // If message came in with TTL one or less, it was last trip. It can not go any further.
        return (useTTL <= 0) ? null : propHdr;
    }


    /**
     *  {@inheritDoc}
     */
    public void walk(Message msg,
            String serviceName,
            String serviceParam,
            int ttl) throws IOException {

        propagate(msg,
                serviceName,
                serviceParam,
                defaultTTL);
    }


    /**
     *  {@inheritDoc}
     */
    public void walk(Vector destPeerIDs,
            Message msg,
            String serviceName,
            String serviceParam,
            int defaultTTL) throws IOException {

        walk(destPeerIDs,
                msg,
                serviceName,
                serviceParam,
                defaultTTL);
    }


    /**
     *  A timer task for monitoring our active rendezvous connections <p/>
     *
     *  Checks leases, challenges when peer adv has changed, initiates lease
     *  renewals, starts new lease requests.
     */
    private class MonitorTask extends TimerTask {

        /**
         *@inheritDoc
         */
        public void run() {
            try {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("[" + group.getPeerGroupID() + "] Periodic rendezvous check");
                }

                Iterator eachRendezvous = Arrays.asList(rendezVous.values().toArray()).iterator();

                while (eachRendezvous.hasNext()) {
                    RdvConnection pConn = (RdvConnection) eachRendezvous.next();

                    try {
                        if (!pConn.isConnected()) {
                            if (LOG.isEnabledFor(Level.INFO)) {
                                LOG.debug("[" + group.getPeerGroupID() + "] Lease expired. Disconnected from " + pConn);
                            }
                            removeRdv(pConn.getPeerID(), false);
                            continue;
                        }

                        if (pConn.peerAdvertisementHasChanged()) {
                            // Pretend that our lease is expiring, so that we do not rest
                            // until we have proven that we still have an rdv.
                            if (LOG.isEnabledFor(Level.DEBUG)) {
                                LOG.debug("[" + group.getPeerGroupID() + "] Local PeerAdvertisement changed. Challenging " + pConn);
                            }
                            challengeRendezVous(pConn.getPeerID(), CHALLENGE_TIMEOUT);
                            continue;
                        }
                        if (TimeUtils.toRelativeTimeMillis(pConn.getRenewal()) <= 0) {
                            if (LOG.isEnabledFor(Level.DEBUG)) {
                                LOG.debug("[" + group.getPeerGroupID() + "] Attempting lease renewal for " + pConn);
                            }
                            sendLeaseRequest(pConn);
                        }
                    } catch (Exception e) {
                        if (LOG.isEnabledFor(Level.WARN)) {
                            LOG.warn("[" + group.getPeerGroupID() + "] Failure while checking " + pConn, e);
                        }
                    }
                }

                // Not enough Rdvs? Try finding more.
                if (rendezVous.size() < MAX_RDV_CONNECTIONS) {
                    connectToRandomRdv();
                }
            } catch (Throwable t) {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("Uncaught throwable in thread :" + Thread.currentThread().getName(), t);
                }
            }
        }
    }



    /**
     *  Listener for
     *
     *  <assignedID>/<group-unique>
     */
    private class EdgeProtocolListener implements EndpointListener {
        RendezVousServiceImpl rdvService;
        EdgeProtocolListener(RendezVousServiceImpl rdvService) {
            this.rdvService = rdvService;
        }
        /**
         *  {@inheritDoc}
         */
        public void processIncomingMessage(Message msg, EndpointAddress srcAddr, EndpointAddress dstAddr) {
            
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("[" + group.getPeerGroupID() + "] processing " + msg);
            }
            
            if (msg.getMessageElement("jxta", RdvAdvReply) != null) {
                rdvService.processRdvAdvReply(msg);
            }
            
            if ((msg.getMessageElement("jxta", ConnectedPeerReply) != null) || (msg.getMessageElement("jxta", ConnectedRdvAdvReply) != null)) {
                rdvService.processConnectedReply(msg);
            }
            
            if (msg.getMessageElement("jxta", DisconnectRequest) != null) {
                rdvService.processDisconnectRequest(msg);
            }
        }
    }
}

