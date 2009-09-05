/*
 *  $Id: JxtaServerSocket.java,v 1.4 2006/03/10 22:06:08 hamada Exp $
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
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
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
 */
package net.jxta.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.InputStreamMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.Messenger;
import net.jxta.endpoint.TextDocumentMessageElement;
import net.jxta.id.IDFactory;
import net.jxta.impl.util.UnbiasedQueue;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *  JxtaServerSocket is a bi-directional Pipe that behaves very much like
 *  ServerSocket.  It creates an inputpipe and listens for pipe connection requests.
 *  JxtaServerSocket also defines it own protocol. Requests arrive as a JXTA Message
 *  with the following elements:
 *
 * <p>
 *  &lt;Cred> Credentials which can be used to determine trust &lt;/Cred>
 * <p>
 *  &lt;reqPipe> requestor's pipe advertisement &lt;/reqPipe>
 * <p>
 *  &lt;remPipe> Remote pipe advertisement &lt;/remPipe>
 * <p>
 *  &lt;reqPeer> Remote peer advertisement  &lt;/reqPeer>
 * <p>
 *  &lt;stream> determine whether the connection is reliable, or not &lt;/stream>
 * <p>
 *  &lt;close> close request &lt;/close>
 * <p>
 *  &lt;data> Data &lt;/data>
 * <p>
 *  JxtaServerSocket then creates a new private pipe, listens for messages on that pipe,
 *  resolves the requestor's pipe, and sends a &lt;remPipe> private pipe created &lt;/remotePipe>
 *  advertisement back, where the remote side is resolved.
 */
public class JxtaServerSocket extends ServerSocket implements PipeMsgListener {

    private static final Logger LOG = Logger.getLogger(JxtaServerSocket.class.getName());
    public static final String  nameSpace = "JXTASOC";
    public static final String    credTag = "Cred";
    public static final String reqPipeTag = "reqPipe";
    public static final String remPeerTag = "remPeer";
    public static final String remPipeTag = "remPipe";
    public static final String    dataTag = "data";
    public static final String   closeTag = "close";
    public static final String   streamTag = "stream";
    protected PeerGroup group;
    protected InputPipe serverPipe;
    protected PipeAdvertisement pipeadv;
    protected int backlog = 50;
    protected int timeout = 60000;
    protected String closeLock = new String("closeLock");
    protected UnbiasedQueue queue = UnbiasedQueue.synchronizedQueue(new UnbiasedQueue(backlog, false));
    protected boolean created = false;
    protected boolean bound = false;
    protected boolean closed = false;
    protected StructuredDocument myCredentialDoc = null;

    /**
     * Default Constructor
     * <p>
     * backlog default of 50
     * <p>
     * timeout defaults to 60 seconds, i.e. blocking.
     * <p>
     * A call to bind() is needed to finish initializing this object.
     */
    public JxtaServerSocket() throws IOException {}

    /**
     * Constructs and binds a JxtaServerSocket using a JxtaSocketAddress as  
     * the address. The default backlog will be 50, and the default timeout
     * 60 seconds.
     * 
     * @param address an instance of JxtaSocketAddress
     * @throws IOException
     * @see net.jxta.socket.JxtaSocketAddress
     */
    public JxtaServerSocket(SocketAddress address) throws IOException {
        this(address, 50);
    }

    /**
     *Constructor for the JxtaServerSocket
     * <p>
     * The backlog defaults to 50.
     * <p>
     * The timeout default to 60 seconds, i.e. blocking.
     *
     * @param  group                JXTA PeerGroup
     * @param  pipeadv              PipeAdvertisement on which pipe requests are accepted
     * @exception  IOException  if an I/O error occurs
     */
    public JxtaServerSocket(PeerGroup group, PipeAdvertisement pipeadv) throws IOException {
        this(group, pipeadv, 50);
    }

    /**
     * Constructs and binds a JxtaServerSocket using a JxtaSocketAddress as  
     * the address. The default timeout will be 60 seconds.
     * 
     * @param address an instance of JxtaSocketAddress
     * @param backlog the size of the backlog queue
     * @throws IOException
     * @see net.jxta.socket.JxtaSocketAddress
     */
    public JxtaServerSocket(SocketAddress address, int backlog) throws IOException {
        this(address, backlog, 60000);
    }

    /**
     * Constructor for the JxtaServerSocket object
     *
     * @param  group                JXTA PeerGroup
     * @param  pipeadv              PipeAdvertisement on which pipe requests are accepted
     * @param  backlog              the maximum length of the queue.
     * @exception  IOException  if an I/O error occurs
     */
    public JxtaServerSocket(PeerGroup group, PipeAdvertisement pipeadv, int backlog) throws IOException {
        this(group, pipeadv, backlog, 60000);
    }

    /**
     * Constructs and binds a JxtaServerSocket using a JxtaSocketAddress as  
     * the address. 
     * 
     * @param address an instance of JxtaSocketAddress
     * @param backlog the size of the backlog queue
     * @param timeout connection timeout in milliseconds
     * @throws IOException
     * @see net.jxta.socket.JxtaSocketAddress
     */
    public JxtaServerSocket(SocketAddress address, int backlog, int timeout) throws IOException {
        if (backlog <= 0) {
            throw new IllegalArgumentException("backlog must be > 0");
        }

        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be >= 0");
        }

        this.timeout = timeout;
        bind(address, backlog);
    }

    /**
     * Constructor for the JxtaServerSocket object
     *
     *
     * @param  group                JXTA PeerGroup
     * @param  pipeadv              PipeAdvertisement on which pipe requests are accepted
     * @param  backlog              the maximum length of the queue.
     * @param  timeout              the specified timeout, in milliseconds
     * @exception  IOException  if an I/O error occurs
     */
    public JxtaServerSocket(PeerGroup group, PipeAdvertisement pipeadv, int backlog, int timeout) throws IOException {
        if (pipeadv.getType() != null && pipeadv.getType().equals(PipeService.PropagateType)) {
            throw new IOException("Propagate pipe advertisements are not supported");
        }
        this.group = group;
        this.pipeadv = pipeadv;
        if (backlog <= 0) {
            throw new IllegalArgumentException("backlog must be > 0");
        }
        this.backlog = backlog;

        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be >= 0");
        }
        this.timeout = timeout;
        queue.setMaxQueueSize(backlog);
        PipeService pipeSvc = group.getPipeService();
        serverPipe = pipeSvc.createInputPipe(pipeadv, this);
        setBound();
    }

    /**
     *  Binds the <code>JxtaServerSocket</code> to a specific pipe advertisement
     *
     * @param  group                JXTA PeerGroup
     * @param  pipeadv              PipeAdvertisement on which pipe requests are accepted
     * @exception  IOException  if an I/O error occurs
     */
    public void bind(PeerGroup group, PipeAdvertisement pipeadv) throws IOException {
        if (isBound()) {
            throw new SocketException("Already bound");
        }
        if (pipeadv.getType() != null && pipeadv.getType().equals(PipeService.PropagateType)) {
            throw new IOException("Propagate pipe advertisements are not supported");
        }
        this.group = group;
        this.pipeadv = pipeadv;
        PipeService pipeSvc = group.getPipeService();
        serverPipe = pipeSvc.createInputPipe(pipeadv, this);
        setBound();
    }

    /**
     *  Binds the <code>JxtaServerSocket</code> to a specific pipe advertisement
     *
     * @param  group                JXTA PeerGroup
     * @param  pipeadv              PipeAdvertisement on which pipe requests are accepted
     * @param  backlog              the maximum length of the queue.
     * @exception  IOException  if an I/O error occurs
     */
    public void bind(PeerGroup group, PipeAdvertisement pipeadv, int backlog) throws IOException {
        this.backlog = backlog;
        bind(group, pipeadv);
        queue.setMaxQueueSize(backlog);
    }

    /**
     *  {@inheritDoc}
     *
     *  <p/>Used to bind a  JxtaServerSocket created with the no-arg constructor.
     */
    public void bind(SocketAddress endpoint) throws IOException {
        bind(endpoint, backlog);
    }

    /**
     *  {@inheritDoc}
     *
     *  <p/>Used to bind a  JxtaServerSocket created with the no-arg constructor.
     */
    public void bind(SocketAddress endpoint, int backlog) throws IOException {
        if (endpoint instanceof JxtaSocketAddress) {
            JxtaSocketAddress socketAddress = (JxtaSocketAddress) endpoint;
            PeerGroup pg = PeerGroup.globalRegistry.lookupInstance(socketAddress.getPeerGroupId());
            if (pg == null) {
                throw new IOException(
                        "Can't connect socket in PeerGroup with id " + socketAddress.getPeerGroupId().toString()
                        + ". No running instance of the group is registered.");
            }
            bind(pg.getWeakInterface(), socketAddress.getPipeAdv(), backlog);
            pg.unref();
        } else {
            throw new IllegalArgumentException("Unsupported subclass of SocketAddress; " + "use JxtaSocketAddress instead.");
        }
    }

    /**
     *  {@inheritDoc}
     *
     *  <p/>The default timeout is 60 seconds (60000ms).
     */
    public Socket accept() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        if (!isBound()) {
            throw new SocketException("Socket is not bound yet");
        }
        try {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Waiting for a connection");
            }
            while (true) {
                Message msg = (Message) queue.pop(timeout);
                if (msg == null) {
                    throw new SocketTimeoutException("Timeout reached");
                }

                JxtaSocket socket = processMessage(msg);
                // make sure we have a socket returning
                if (socket != null) {
                    if (LOG.isEnabledFor(Level.DEBUG)) {
                        LOG.debug("Waiting for a connection");
                    }
                    return socket;
                } else if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("No connection");
                }
            }
        } catch (InterruptedException ie) {
            throw new SocketException("interrupted");
        }
    }

    /**
     *  Gets the group associated with this JxtaServerSocket object
     *
     * @return    The group value
     */
    public PeerGroup getGroup() {
        return group;
    }

    /**
     *  Gets the PipeAdvertisement associated with this JxtaServerSocket object
     *
     * @return    The pipeAdv value
     */
    public PipeAdvertisement getPipeAdv() {
        return pipeadv;
    }

    /**
     *  {@inheritDoc}
     *
     *  <p/>The default timeout is 60 seconds (60000ms).
     */
    public void close() throws IOException {
        synchronized (closeLock) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Closing ServerSocket");
            }
            if (isClosed()) {
                return;
            }
            if (isBound()) {
                // close all the pipe
                serverPipe.close();
            }
            queue.close();
            closed = true;
        }
    }

    /**
     *  Sets the bound attribute of the JxtaServerSocket object
     */
    protected void setBound() {
        bound = true;
    }

    /**
     *  {@inheritDoc}
     *
     *  <p/>The default timeout is 60 seconds (60000ms).
     */
    public synchronized int getSoTimeout() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        return timeout;
    }


    /**
     *  {@inheritDoc}
     *
     *  <p/>The default timeout is 60 seconds (60000ms).
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }
        this.timeout = timeout;
    }

    /**
     *  {@inheritDoc}
     */
    public boolean isClosed() {
        synchronized (closeLock) {
            return closed;
        }
    }

    /**
     *  {@inheritDoc}
     */
    public boolean isBound() {
        return bound;
    }

    /**
     *  {@inheritDoc}
     */
    public void pipeMsgEvent(PipeMsgEvent event) {

        //deal with messages as they come in
        Message message = event.getMessage();
        if (message == null) {
            return;
        }
        boolean pushed = false;
        try {
            pushed = queue.push(message, -1);
        } catch (InterruptedException e) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Interrupted", e);
            }
        }
        if (!pushed && LOG.isEnabledFor(Level.WARN)) {
            LOG.warn("backlog queue full, connect request dropped");
        }
    }

    /**
     * Method processMessage is the heart of this class.
     * <p>
     * This takes new incoming connect messages and constructs the JxtaSocket
     * to talk to the new client.
     * <p>
     * The ResponseMessage is created and sent.
     * 
     * @param msg The client connection request (assumed not null)
     * @return JxtaSocket Which may be null if an error occurs.
     */
    private JxtaSocket processMessage(Message msg) {

        PipeAdvertisement outputPipeAdv = null;
        PeerAdvertisement  peerAdv = null;
        StructuredDocument credDoc = null;

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Processing a connection message");
        }
        try {
            MessageElement el = msg.getMessageElement(nameSpace, credTag);
            if (el != null) {
                InputStream in = el.getStream();
                credDoc = (StructuredDocument)
                          StructuredDocumentFactory.newStructuredDocument(el.getMimeType(), in);
            }
            el = msg.getMessageElement(nameSpace, reqPipeTag);
            if (el != null) {
                InputStream in = el.getStream();
                outputPipeAdv = (PipeAdvertisement)
                                AdvertisementFactory.newAdvertisement(el.getMimeType(), in);
            }
            el = msg.getMessageElement(nameSpace, remPeerTag);
            if (el != null) {
                InputStream in = el.getStream();
                peerAdv = (PeerAdvertisement)
                          AdvertisementFactory.newAdvertisement(el.getMimeType(), in);
            }

            el = msg.getMessageElement(nameSpace, streamTag);
            boolean isStream = false;
            if (el != null) {
                isStream = (el.toString().equals("true"));
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("Connection request [isStream] :" + isStream);
                }
            }
            Messenger msgr = JxtaSocket.lightweightOutputPipe(group, outputPipeAdv, peerAdv);
            if (msgr != null) {
                PipeAdvertisement newpipe = newInputPipe(group, outputPipeAdv);
                JxtaSocket newsoc = new JxtaSocket(group, msgr, newpipe, credDoc, isStream);
                sendResponseMessage(group, msgr, newpipe);
                return newsoc;
            }
        } catch (IOException e) {
            // deal with the error
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("IOException occured", e);
            }
        }
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Connection processing did not result in a connection");
        }
        return null;
    }

    /**
     * Method sendResponseMessage get the createResponseMessage and sends it.
     * 
     * @param group
     * @param msgr
     * @param pipeAd
     */
    protected void sendResponseMessage(PeerGroup group, Messenger msgr, PipeAdvertisement pipeAd) throws IOException {

        Message msg = new Message();
        PeerAdvertisement peerAdv = group.getPeerAdvertisement();
        if (myCredentialDoc == null) {
            myCredentialDoc = JxtaSocket.getCredDoc(group);
        }
        if (myCredentialDoc != null) {
            msg.addMessageElement(JxtaServerSocket.nameSpace,
                                  new InputStreamMessageElement(credTag, MimeMediaType.XMLUTF8, myCredentialDoc.getStream(), null));
        }

        msg.addMessageElement(JxtaServerSocket.nameSpace,
                              new TextDocumentMessageElement(remPipeTag, (XMLDocument) pipeAd.getDocument(MimeMediaType.XMLUTF8), null));

        msg.addMessageElement(nameSpace, new TextDocumentMessageElement(remPeerTag, (XMLDocument) peerAdv.getDocument(MimeMediaType.XMLUTF8), null));
        msgr.sendMessageB(msg, null, null);
    }

    /**
     * Utility method newInputPipe is used to get new pipe advertisement (w/random pipe ID) from old one.
     * <p>
     * Called by JxtaSocket to make pipe (name -> name.remote) for open message
     * <p>
     * Called by JxtaServerSocket to make pipe (name.remote -> name.remote.remote) for response message
     * 
     * @param group
     * @param pipeadv to get the basename and type from
     * @return PipeAdvertisement a new pipe advertisement
     */
    protected static PipeAdvertisement newInputPipe(PeerGroup group, PipeAdvertisement pipeadv) {
        PipeAdvertisement adv = null;
        adv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
        adv.setPipeID(IDFactory.newPipeID((PeerGroupID) group.getPeerGroupID()));
        adv.setName(pipeadv.getName() + ".remote");
        adv.setType(pipeadv.getType());
        return adv;
    }
    /**
     *  Sets the connection credential doc
     *  If no credentials are set, the default group credential will be used
     *  @param doc Credential StructuredDocument 
     */
    public  void setCredentialDoc(StructuredDocument doc) {
        this.myCredentialDoc = doc;
    }

    /**
     * @return the server socket's JxtaSocketAddress
     * @see java.net.ServerSocket#getLocalSocketAddress()
     */
    public SocketAddress getLocalSocketAddress() {
        return new JxtaSocketAddress(getGroup(), getPipeAdv(), getGroup().getPeerID());
    }

    /**
     *  {@inheritDoc}
     *
     *  <p/>Closes the JxtaServerPipe.
     */
    protected synchronized void finalize() throws Throwable {
        super.finalize();
        if (!closed) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("JxtaServerSocket is being finalized without being previously closed. This is likely a users bug.");
            }
        }
        close();
    }

    /**
     *{@inheritDoc}
     */
    public String toString() {
        if (!isBound()) {
            return "JxtaServerSocket[unbound]";
        }
        return "JxtaServerSocket[pipe id=" + pipeadv.getPipeID().toString() + "]";
    }
}

