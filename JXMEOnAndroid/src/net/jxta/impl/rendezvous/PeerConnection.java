/*
 *  $Id: PeerConnection.java,v 1.2 2005/08/31 06:32:57 hamada Exp $
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
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 */
package net.jxta.impl.rendezvous;

import java.util.Enumeration;

import net.jxta.discovery.DiscoveryService;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.endpoint.EndpointService;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.Messenger;
import net.jxta.endpoint.OutgoingMessageEvent;
import net.jxta.endpoint.OutgoingMessageEventListener;
import net.jxta.id.ID;
import net.jxta.impl.util.TimeUtils;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.RouteAdvertisement;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *  Manages a connection with a remote client or a rendezvous peer.
 */
public abstract class PeerConnection implements OutgoingMessageEventListener {

    /**
     *  Log4J Logger
     */
    private final static transient Logger LOG = Logger.getLogger(PeerConnection.class.getName());

    /**
     *  A cached messenger to be used for sending messages to the remote peer.
     */
    protected transient Messenger cachedMessenger = null;

    /**
     *  If true then we believe we are still connected to the remote peer.
     */
    protected volatile boolean connected = true;
    /**
     *  Description of the Field
     */
    protected transient final EndpointService endpoint;

    /**
     *  Description of the Field
     */
    protected transient final PeerGroup group;

    /**
     *  The absolute time in milliseconds at which we expect this connection to
     *  expire unless renewed.
     */
    protected transient long leasedTil = -1;

    /**
     *  Cached name of the peer for display purposes.
     */
    protected String peerName = null;

    /**
     *  ID of the remote peer.
     */
    protected transient final ID peerid;


    /**
     *  Constructor for the PeerConnection object
     *
     *@param  group     group context
     *@param  endpoint  the endpoint service to use for sending messages.
     *@param  peerid    destination peerid
     */
    public PeerConnection(PeerGroup group, EndpointService endpoint, ID peerid) {
        this.group = group;
        this.endpoint = endpoint;
        this.peerid = peerid;

        this.peerName = peerid.toString();
    }


    /**
     *  Declare that we are connected for the specified amount of time.
     *
     *@param  leaseDuration  The duration of the lease in relative milliseconds.
     */
    protected void connect(long leaseDuration) {
        setLease(leaseDuration);

        setConnected(true);
    }


    /**
     *  {@inheritDoc} <p/>
     *
     *  performs PeerID comparison
     *
     *@param  obj  Description of the Parameter
     *@return      Description of the Return Value
     */
    public boolean equals(Object obj) {
        if (obj instanceof PeerConnection) {
            return peerid.equals(((PeerConnection) obj).peerid);
        } else {
            return false;
        }
    }


    /**
     *  {@inheritDoc} <p/>
     *
     *  Just in case the code that allocated an instance of this object forgot
     *  to do "close".
     *
     *@exception  Throwable  Description of the Exception
     */
    protected void finalize() throws Throwable {
        setConnected(false);
    }


    /**
     *  Return a messenger suitable for communicating to this peer.
     *
     *@return        The cachedMessenger value
     *@deprecated    Prefered style is to pass the connection object around and
     *      use the sendMessage method rather than getting the messenger.
     *@result        a messenger for sending to this peer or <code>null</code>
     *      if none is available.
     */
    protected Messenger getCachedMessenger() {

        // We don't do the check on existing messenger under synchronization
        // hence the temporary variable.
        Messenger result = cachedMessenger;

        if ((null == result) || result.isClosed()) {
            // We need a new messenger.
            PeerAdvertisement padv = null;

            DiscoveryService discovery = group.getDiscoveryService();

            // Try to see if we have a peer advertisement for this peer.
            // This is very likely.
            if (null != discovery) {
                try {
                    Enumeration each = discovery.getLocalAdvertisements(DiscoveryService.PEER, "PID", peerid.toString());

                    if (each.hasMoreElements()) {
                        padv = (PeerAdvertisement) each.nextElement();
                    }
                } catch (Exception ignored) {

                }
            }

            result = getCachedMessenger(padv);
        }

        return result;
    }


    /**
     *  Return a messenger suitable for communicating to this peer.
     *
     *@param  padv  A peer advertisement which will be used for route hints if a
     *      new messenger is needed.
     *@return       The cachedMessenger value
     *@result       a messenger for sending to this peer or <code>null</code> if
     *      none is available.
     */
    protected synchronized Messenger getCachedMessenger(PeerAdvertisement padv) {

        if ((null != padv) && !peerid.equals(padv.getPeerID())) {
            throw new IllegalArgumentException("Peer Advertisement does not match connection");
        }

        if ((null != padv) && (null != padv.getName())) {
            setPeerName(padv.getName());
        }

        // if we have a good messenger then re-use it.
        if ((null != cachedMessenger) && !cachedMessenger.isClosed()) {
            return cachedMessenger;
        }

        cachedMessenger = null;

        if (isConnected()) {
            // we only get new messengers while we are connected. It is not
            // worth the effort for a disconnected peer. We WILL use an existing
            // open messenger if we have one though.
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Getting new cached Messenger for " + peerName);
            }

            RouteAdvertisement hint = null;

            if (null != padv) {
                hint = RendezVousServiceImpl.extractRouteAdv(padv);
            }

            EndpointAddress destAddress = mkAddress(peerid, null, null);

            cachedMessenger = endpoint.getMessenger(destAddress, hint);

            if (null == cachedMessenger) {
                // no messenger? avoid doing more work.
                setConnected(false);
            }
        } else {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("connection closed : NOT getting new cached Messenger for " + peerName);
            }
        }

        return cachedMessenger;
    }


    /**
     *  Time at which the lease will expire in absolute milliseconds.
     *
     *@return    The lease value
     */
    public long getLeaseEnd() {
        return leasedTil;
    }


    /**
     *  Get the peer id of the peer associated with this connection.
     *
     *@return    The peer id of the connected peer.
     */
    public ID getPeerID() {
        return peerid;
    }


    /**
     *  Get the peer name. If the symobolic name is available, use it, otherwise
     *  returns the peer id.
     *
     *@return    The name of the connected peer.
     */
    public String getPeerName() {

        return peerName;
    }


    /**
     *  {@inheritDoc}
     *
     *@return    Description of the Return Value
     */
    public int hashCode() {
        return peerid.hashCode();
    }


    /**
     *  Test if the connection is still active.
     *
     *@return    The connected value
     */
    public boolean isConnected() {
        connected &= (TimeUtils.toRelativeTimeMillis(leasedTil) >= 0);

        return connected;
    }


    /**
     *  {@inheritDoc}
     *
     *@param  event  Description of the Parameter
     */
    public void messageSendFailed(OutgoingMessageEvent event) {
        // If it's just a case of queue overflow, ignore it.
        if (event.getFailure() == null) {
            return;
        }

        setConnected(false);
    }


    /**
     *  {@inheritDoc}
     *
     *@param  event  Description of the Parameter
     */
    public void messageSendSucceeded(OutgoingMessageEvent event) {
        // hurray!
    }


    /**
     *  Convenience method for constructing an endpoint address from an id
     *
     *@param  destPeer  peer id
     *@param  serv      the service name (if any)
     *@param  parm      the service param (if any)
     *@return           endpointAddress for the destination peer.
     */
    private static EndpointAddress mkAddress(ID destPeer, String serv, String parm) {
        EndpointAddress addr = new EndpointAddress("jxta", destPeer.getUniqueValue().toString(), serv, parm);

        return addr;
    }


    /**
     *  Send a message to the remote peer.
     *
     *@param  msg      the message to send.
     *@param  service  The destination service.
     *@param  param    Parameters for the destination service.
     *@return
     *      <true>
     *        true
     *      </true>
     *      if the message was queued to be sent, otherwise <code>false</code>.
     *      A <code>true</code> result does not mean that the destination peer
     *      will receive the message.
     */
    public boolean sendMessage(Message msg, String service, String param) {

        Messenger messenger = getCachedMessenger();

        if (null != messenger) {
            messenger.sendMessage(msg, service, param, this);
            return true;
        } else {
            return false;
        }
    }


    /**
     *  Set the connection state. This operation must be idempotent.
     *
     *@param  isConnected  The new connected state. Be very careful when setting
     *      <code>true</code> state without setting a new lease.
     */
    public void setConnected(boolean isConnected) {
        connected = isConnected;
    }


    /**
     *  Set the lease duration in relative milliseconds.
     *
     *@param  leaseDuration  the lease duration in relative milliseconds.
     */
    protected void setLease(long leaseDuration) {
        leasedTil = TimeUtils.toAbsoluteTimeMillis(leaseDuration);
    }


    /**
     *  set the peer name.
     *
     *@param  name  The new peerName value
     */
    protected void setPeerName(String name) {

        peerName = name;
    }


    /**
     *  {@inheritDoc}
     *
     *@return    Description of the Return Value
     */
    public String toString() {
        return getPeerName() + (connected ? " C" : " c") + " : " + Long.toString(TimeUtils.toRelativeTimeMillis(leasedTil));
    }
}

