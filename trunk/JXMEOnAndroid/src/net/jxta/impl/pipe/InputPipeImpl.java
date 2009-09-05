/*
 *  $Id: InputPipeImpl.java,v 1.3 2005/06/01 17:00:09 hamada Exp $
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
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 *
 *  $Id: InputPipeImpl.java,v 1.3 2005/06/01 17:00:09 hamada Exp $
 */
package net.jxta.impl.pipe;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.jxta.endpoint.EndpointAddress;
import net.jxta.endpoint.EndpointListener;
import net.jxta.endpoint.Message;
import net.jxta.id.ID;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PipeAdvertisement;

import net.jxta.impl.util.TimeUtils;
import net.jxta.impl.util.UnbiasedQueue;

/**
 *  Implements the {@link net.jxta.pipe.InputPipe} interface by listening on the
 *  endpoint for messages to service "PipeService" and a param of the Pipe ID.
 */
class InputPipeImpl implements EndpointListener, InputPipe {

    /**
     *  log4J logger
     */
    private final static Logger LOG = Logger.getLogger(InputPipeImpl.class.getName());

    /**
     *  Description of the Field
     */
    protected final static int QUEUESIZE = 100;

    /**
     *  Description of the Field
     */
    protected volatile boolean closed = false;
    /**
     *  Description of the Field
     */
    protected PipeMsgListener listener = null;

    /**
     *  Description of the Field
     */
    protected PipeAdvertisement pipeAdv = null;
    /**
     *  Description of the Field
     */
    protected PipeID pipeID = null;
    /**
     *  Description of the Field
     */
    protected UnbiasedQueue queue = null;

    /**
     *  Description of the Field
     */
    protected PipeRegistrar registrar = null;

    /**
     *  Constructor for the InputPipeImpl object
     *
     *@param  r                pipe resolver
     *@param  adv              pipe advertisement
     *@param  listener         listener to receive messages
     *@exception  IOException  Description of the Exception
     */
    protected InputPipeImpl(PipeRegistrar resolver, PipeAdvertisement adv, PipeMsgListener listener) throws IOException {

        registrar = resolver;
        this.pipeAdv = adv;
        this.listener = listener;

        pipeID = (PipeID) adv.getPipeID();

        if (LOG.isEnabledFor(Level.INFO)) {
            LOG.info("Creating InputPipe for " + pipeID + " of type " + adv.getType() + " with " + ((null != listener) ? "listener" : "queue"));
        }
        if (!registrar.register(this)) {
            throw new IOException("Could not register input pipe (already registered) for " + pipeID);
        }
        // queue based inputpipe?
        if (listener == null) {
            queue = UnbiasedQueue.synchronizedQueue(new UnbiasedQueue(QUEUESIZE, true));
        }
    }

    /**
     *  {@inheritDoc}
     */
    public synchronized void close() {

        if (closed) {
            return;
        }

        // Close the queue
        if (null == listener) {
            queue.close();
        }

        // Remove myself from the pipe resolver.
        if (!registrar.forget(this)) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("close() : pipe was not registered with registrar.");
            }
        }

        listener = null;

        registrar = null;

        if (LOG.isEnabledFor(Level.INFO)) {
            LOG.info("close(): Closed " + pipeID);
        }

        closed = true;
    }

    /**
     *  {@inheritDoc} <p/>
     *
     *  Closes the pipe.
     *
     *@exception  Throwable  Description of the Exception
     */
    protected synchronized void finalize() throws Throwable {
        super.finalize();

        if (!closed) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Pipe is being finalized without being previously closed. This is likely a bug.");
            }
        }

        close();
    }


    /**
     *  Gets the pipe advertisement
     *
     *@return    The advertisement
     */
    public PipeAdvertisement getAdvertisement() {
        return pipeAdv;
    }


    /**
     *  Gets the pipe name
     *
     *@return    The name
     */
    public String getName() {
        return pipeAdv.getName();
    }


    /**
     *  Gets the pipe id
     *
     *@return    The type
     */
    public ID getPipeID() {
        return pipeID;
    }


    /**
     *  Gets the pipe type
     *
     *@return    The type
     */
    public String getType() {
        return pipeAdv.getType();
    }

    /**
     *  {@inheritDoc}
     *
     *@param  timeout                   Description of the Parameter
     *@return                           Description of the Return Value
     *@exception  InterruptedException  Description of the Exception
     */
    public Message poll(int timeout) throws InterruptedException {

        if (listener == null) {
            return (Message) queue.pop(timeout);
        } else {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("poll() has no effect in listener mode.");
            }
            return null;
        }
    }

    /**
     *  {@inheritDoc}
     *
     *@param  msg      Description of the Parameter
     *@param  srcAddr  Description of the Parameter
     *@param  dstAddr  Description of the Parameter
     */
    public void processIncomingMessage(Message msg, EndpointAddress srcAddr, EndpointAddress dstAddr) {

        // if we are closed, ignore any additional messages
        if (closed) {
            return;
        }

        // XXX: header check, security and such should be done here
        // before pushing the message onto the queue.

        // determine where demux the msg, to listener, or onto the queue

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("processIncomingMessage: received " + msg + " on pipe : " + pipeID);
        }

        if (null != listener) {
            PipeMsgEvent event = new PipeMsgEvent(this, msg, pipeID);
            try {
                listener.pipeMsgEvent(event);
            } catch (Throwable ignored) {
                if (LOG.isEnabledFor(Level.ERROR)) {
                    LOG.error("Uncaught Throwable in listener for : " + pipeID + "(" + listener.getClass().getName() + ")", ignored);
                }
            }
        } else {
            boolean pushed = false;

            while (!pushed && !queue.isClosed()) {
                try {
                    pushed = queue.push(msg, TimeUtils.ASECOND);
                } catch (InterruptedException woken) {
                    Thread.interrupted();
                }
            }

            if (LOG.isEnabledFor(Level.DEBUG)) {
                synchronized (this) {
                    LOG.debug("Queued " + msg + " for " + pipeID +
                              "\n\tqueue closed : " + queue.isClosed() +
                              "\tnumber in queue : " + queue.getCurrentInQueue() +
                              "\tnumber queued : " + queue.getNumEnqueued() +
                              "\tnumber dequeued : " + queue.getNumDequeued());
                }
            }
        }
    }

    /**
     *  {@inheritDoc}
     *
     *@return                           Description of the Return Value
     *@exception  InterruptedException  Description of the Exception
     */
    public Message waitForMessage() throws InterruptedException {
        return poll(0);
    }
}

