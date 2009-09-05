/*
 *  Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 *  reserved.
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
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *  not be used to endorse or promote products derived from this
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
 *  $Id: Srdi.java,v 1.3 2005/06/01 16:53:59 hamada Exp $
 */
package net.jxta.impl.cm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import net.jxta.credential.Credential;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.protocol.ResolverSrdiMsgImpl;
import net.jxta.impl.protocol.SrdiMessageImpl;
import net.jxta.impl.util.JxtaHash;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.RdvAdvertisement;
import net.jxta.protocol.ResolverQueryMsg;
import net.jxta.protocol.ResolverSrdiMsg;
import net.jxta.protocol.SrdiMessage;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.resolver.ResolverService;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *  Srdi is a service which provides Srdi functionalities such as :
 *  <ul>
 *    <li> pushing of Srdi messages to a another peer/propagate</li>
 *    <li> replication of an Srdi Message to other peers in a given peerview
 *    </li>
 *    <li> given an expression Srdi provides a independently calculated starting
 *    point</li>
 *    <li> Forwarding a ResolverQuery, and taking care of hopCount, random
 *    selection</li>
 *    <li> registers with the RendezvousService to determine when to share Srdi
 *    Entries</li> and whether to push deltas, or full a index</li>
 *    <li> provides a SrdiInterface giving to provide a generic srdi message
 *    definition</li>
 *  </ul>
 *  <p/>
 *
 *  If Srdi is started as a thread it performs periodic srdi pushes of indices
 *  and also has the ability to respond to rendezvous events. <p/>
 *
 *  ResolverSrdiMessages define a ttl, to indicate to the receiving service
 *  whether to replicate such message or not. <p/>
 *
 *  In addition A ResolverQuery defines a hopCount to indicate how many hops a
 *  query has been forwarded. This element could be used to detect/stop a query
 *  forward loopback hopCount is checked to make ensure a query is not forwarded
 *  more than twice.
 *
 *@see    <a href="http://spec.jxta.org/nonav/v1.0/docbook/JXTAProtocols.html#proto-prp"
 *      target="_blank">JXTA Protocols Specification : Peer Resolver Protocol
 *      </a>
 */
public class Srdi implements Runnable, RendezvousListener {

    /**
     *  Log4J Logger
     */
    private final static Logger LOG = Logger.getLogger(Srdi.class.getName());

    // This ought be to configurable/based on a function applied to the rpv size
    /**
     *  Description of the Field
     */
    public final static int RPV_REPLICATION_THRESHOLD = 3;
    private long connectPollInterval = 0;
    private Credential credential = null;
    private StructuredDocument credentialDoc = null;

    private PeerGroup group = null;
    private String handlername = null;
    private JxtaHash jxtaHash = new JxtaHash();
    private MembershipService membership;
    private long pushInterval = 0;

    /**
     *  Random number generator used for random result selection
     */
    private static Random random = new Random();
    private volatile boolean republish = true;

    private ResolverService resolver;
    private SrdiInterface srdiService = null;

    private volatile boolean stop = false;

    /**
     *  Starts the Srdi Service. wait for connectPollInterval prior to pushing
     *  the index if connected to a rdv, otherwise index is as soon as the
     *  Rendezvous connect occurs
     *
     *@param  group                group context to operate in
     *@param  handlername          the SRDI handlername
     *@param  srdiService          the service utilizing this Srdi, for purposes
     *      of callback push entries on events such as rdv connect/disconnect,
     *      etc.
     *@param  srdiIndex            The index instance associated with this
     *      service
     *@param  connectPollInterval  initial timeout before the very first push of
     *      entries in milliseconds
     *@param  pushInterval         the Interval at which the deltas are pushed
     *      in milliseconds
     */
    public Srdi(PeerGroup group,
                String handlername,
                SrdiInterface srdiService,
                long connectPollInterval,
                long pushInterval) {

        this.group = group;
        this.handlername = handlername;
        this.srdiService = srdiService;
        this.connectPollInterval = connectPollInterval;
        this.pushInterval = pushInterval;

        membership = group.getMembershipService();
        resolver = group.getResolverService();
        group.getRendezVousService().addListener(this);
        /*
        synchronized (this) {
            try {
                credential = membership.getDefaultCredential();

                if (null != credential) {
                    credentialDoc = credential.getDocument(MimeMediaType.XMLUTF8);
                } else {
                    credentialDoc = null;
                }
            } catch (Exception all) {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("could not get credential", all);
                }
            }
        }
        */
    }

    /**
     *  Forwards a Query to a specific peer hopCount is incremented to indicate
     *  this query is forwarded
     *
     *@param  peer   peerid to forward query to
     *@param  query  The query
     */
    public void forwardQuery(Object peer, ResolverQueryMsg query) {

        query.incrementHopCount();
        if (query.getHopCount() > 2) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("hopCount exceeded. Not forwarding query " + query.getHopCount());
            }
            // query has been forwarded too many times
            return;
        }
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("[" + group.getPeerGroupName() + " / " + handlername + "] Forwarding Query to " + peer);
        }
        resolver.sendQuery(peer.toString(), query);
    }

    /**
     *  Forwards a Query to a list of peers hopCount is incremented to indicate
     *  this query is forwarded
     *
     *@param  peers  The peerids to forward query to
     *@param  query  The query
     */
    public void forwardQuery(Vector peers, ResolverQueryMsg query) {

        query.incrementHopCount();
        if (query.getHopCount() > 2) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("hopCount exceeded not forwarding query " + query.getHopCount());
            }
            // query has been forwarded too many times
            return;
        }
        for (int i = 0; i < peers.size(); i++) {
            PeerID peer = (PeerID) peers.elementAt(i);
            String destPeer = peer.toString();

            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("[" + group.getPeerGroupName() + " / " + handlername + "] Forwarding Query to " + destPeer);
            }
            resolver.sendQuery(destPeer, query);
        }
    }

    /**
     *  forward srdi message to another peer
     *
     *@param  peerid        PeerID to forward query to
     *@param  srcPid        The source originator
     *@param  primaryKey    primary key
     *@param  secondarykey  secondary key
     *@param  value         value of the entry
     *@param  expiration    expiration in ms
     */
    public void forwardSrdiMessage(PeerID peerid,
                                   PeerID srcPid,
                                   String primaryKey,
                                   String secondarykey,
                                   String value,
                                   long expiration) {

        try {
            SrdiMessageImpl srdi = new SrdiMessageImpl(srcPid,
                                      // ttl of 0, avoids additional replication
                                      0,
                                      primaryKey,
                                      secondarykey,
                                      value,
                                      expiration);

            ResolverSrdiMsgImpl resSrdi = new ResolverSrdiMsgImpl(handlername,
                                                                  credential,
                                                                  srdi.toString());
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("[" + group.getPeerGroupName() +
                          " / " + handlername +
                          "] Forwarding a SRDI messsage of type " + primaryKey + 
                          " to " + peerid);
            }
            resolver.sendSrdi(peerid.toString(), (ResolverSrdiMsg) resSrdi);
        } catch (Exception e) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Failed forwarding SRDI Message", e);
            }
        }
    }

    /**
     *  get the global peerview as the rendezvous service only returns the
     *  peerview without the local RDV peer. We need this consistent view for
     *  the SRDI index if not each RDV will have a different peerview, off
     *  setting the index even when the peerview is stable
     *
     *@return    the sorted list
     */
    public Vector getGlobalPeerView() {

        Vector global = new Vector();
        SortedSet set = new TreeSet();

        try {
            // get the local peerview
            Vector rpv = group.getRendezVousService().getLocalWalkView();
            Iterator eachPVE = rpv.iterator();
            while (eachPVE.hasNext()) {
                RdvAdvertisement padv = (RdvAdvertisement) eachPVE.next();
                set.add(padv.getPeerID().toString());
            }

            // add myself
            set.add(group.getPeerID().toString());
            // produce a vector of Peer IDs
            Iterator eachPeerID = set.iterator();

            while (eachPeerID.hasNext()) {
                try {
                    PeerID id = (PeerID) IDFactory.fromURI(new URI((String) eachPeerID.next()));
                    global.add(id);
                } catch (URISyntaxException badID) {
                    throw new IllegalArgumentException("Bad PeerID ID in advertisement");
                } catch (ClassCastException badID) {
                    throw new IllegalArgumentException("ID was not a peerID");
                }
            }
        } catch (Exception ex) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Failure generating the global view", ex);
            }
        }
        return global;
    }


    /**
     *  Push an SRDI message to a peer ttl is 1, and therefore services
     *  receiving this message could choose to replicate this message
     *
     *@param  peer  peer to push message to, if peer is null it is the message
     *      is propagated
     *@param  srdi  SRDI message to send
     */
    public void pushSrdi(ID peer, SrdiMessage srdi) {
        try {
            ResolverSrdiMsg resSrdi = new ResolverSrdiMsgImpl(handlername, credential, srdi.toString());
            if (peer == null) {
                resolver.sendSrdi(null, resSrdi);
            } else {
                resolver.sendSrdi(peer.toString(), resSrdi);
            }
        } catch (Exception e) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Failed to send srdi message", e);
            }
        }
    }

    /**
     *  {@inheritDoc}
     *
     *@param  event  Description of the Parameter
     */
    public synchronized void rendezvousEvent(RendezvousEvent event) {

        int theEventType = event.getType();

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("[" + group.getPeerGroupName() + " / " + handlername + "] Processing " + event);
        }

        switch (theEventType) {

            case RendezvousEvent.RDVCONNECT:
                // This is an initial connection, we need to upload the
                // complete index.
                republish = true;

            case RendezvousEvent.RDVRECONNECT:
                // This is just a renewal of the rdv lease. Nothing special to do.
                notify();
                // wake up the thread now.
                break;
            case RendezvousEvent.CLIENTCONNECT:
            case RendezvousEvent.CLIENTRECONNECT:
            case RendezvousEvent.BECAMERDV:
            case RendezvousEvent.BECAMEEDGE:
                break;
            case RendezvousEvent.RDVFAILED:
            case RendezvousEvent.RDVDISCONNECT:
                republish = true;
                break;
            case RendezvousEvent.CLIENTFAILED:
            case RendezvousEvent.CLIENTDISCONNECT:
                break;
            default:
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("[" + group.getPeerGroupName() + " / " + handlername + "] Unexpected RDV event " + event);
                }
                break;
        }
    }

    /**
     *  {@inheritDoc} <p/>
     *
     *  Main processing method for the SRDI Worker thread Send all entries, wait
     *  for pushInterval, then send deltas
     */
    public void run() {

        boolean waitingForRdv;
        try {
            while (!stop) {
                waitingForRdv = group.isRendezvous() ||
                               !group.getRendezVousService().isConnectedToRendezVous();
                // upon connection we will have to republish
                republish |= waitingForRdv;
                synchronized (this) {
                    // wait until we stop being a rendezvous or connect to a rendezvous
                    if (waitingForRdv) {
                        try {
                            wait(connectPollInterval);
                        } catch (InterruptedException e) {
                            Thread.interrupted();
                        }
                        continue;
                    }

                    if (!republish) {
                        if (LOG.isEnabledFor(Level.DEBUG)) {
                            LOG.debug("[" + group.getPeerGroupName() +
                                      " / " + handlername + 
                                      "] Sleeping for " + pushInterval+ 
                                      "ms before sending deltas.");
                        }
                        try {
                            wait(pushInterval);
                        } catch (InterruptedException e) {
                            Thread.interrupted();
                            continue;
                        }

                        if (stop) {
                            break;
                        }
                    }
                }

                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("[" + group.getPeerGroupName() +
                              " / " + handlername + "] Pushing " + (republish ?
                              "all entries" : "deltas"));
                }

                srdiService.pushEntries(republish);
                republish = false;
            }
        } catch (Throwable all) {
            if (LOG.isEnabledFor(Level.ERROR)) {
                LOG.error("Uncaught Throwable in " +
                          Thread.currentThread().getName() + 
                          "[" + group.getPeerGroupName() +
                          " / " + handlername + "]", all);
            }
        }
    }

    /**
     *  stop the current running thread
     */
    public synchronized void stop() {

        if (stop) {
            return;
        }
        stop = true;
        group.getRendezVousService().removeListener(this);
        // wakeup and die
        notify();
    }

    /**
     *  Interface for pushing entries.
     */
    public interface SrdiInterface {

        /**
         *  Pushe SRDI entries.
         *
         *@param  all  if true then push all entries otherwise just push those
         *      which have changed since the last push.
         */
        void pushEntries(boolean all);
    }
}

