/*
 * $Id: OutgoingPipeAdaptor.java,v 1.1 2005/08/23 00:14:06 hamada Exp $
 *
 * Copyright (c) 2003 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 */

package net.jxta.impl.util.pipe.reliable;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.jxta.pipe.OutputPipe;
import net.jxta.endpoint.Message;
import net.jxta.impl.util.TimeUtils;

public class OutgoingPipeAdaptor implements Outgoing, Runnable {

    private static final Logger LOG = 
        Logger.getLogger(OutgoingPipeAdaptor.class.getName());

    private OutputPipe pipe = null;
    private long lastAccessed = 0;
    private List outQueue = null;
    private Thread pusher = null;

    public OutgoingPipeAdaptor(OutputPipe pipe) {
        if (pipe == null) {
            throw new IllegalArgumentException("pipe cannot be null");
        }
        this.pipe = pipe;

        // initialize to some reasonable value
        lastAccessed = TimeUtils.timeNow();

        outQueue = new ArrayList();

        pusher = new Thread(this, "Reliable OutgoingPipe Message Push Thread");
        pusher.setDaemon(true);
        pusher.start();
    }

    public boolean send(Message msg) throws IOException {
        synchronized (outQueue) {
            while (outQueue.size() >= Defs.MAXQUEUESIZE) {
                try {
                    outQueue.wait(TimeUtils.ASECOND);
                } catch (InterruptedException ignore) {
                }
            }
            outQueue.add(msg);
            outQueue.notifyAll();
        }
        return true;
    }

    public void close() throws IOException {
        pipe.close();
    }

    // Obsolete.
    public long getMinIdleReconnectTime() {
        return 10 * TimeUtils.AMINUTE;
    }

    // Default should be "never", otherwise, connection closes while not
    // in active use and ReliableOutputStream does NOT reconnect automatically.
    public long getIdleTimeout() {
        return Long.MAX_VALUE;
    }

    // This is the important tunable: how long to wait on a stale connection.
    public long getMaxRetryAge() {
        return 2 * TimeUtils.AMINUTE;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long time) {
        lastAccessed = time;
    }

    public String toString() {
        return pipe.toString() + 
            " lastAccessed=" + Long.toString(lastAccessed);
    }

    public void run() {
        try {
            while (true) {
                Message msg = null;
                synchronized (outQueue) {
                    while (outQueue.isEmpty()) {
                        try {
                            outQueue.wait(TimeUtils.ASECOND);
                        } catch (InterruptedException ignore) {
                        }
                    }

                    msg = (Message) outQueue.remove(0);
                    outQueue.notifyAll();
                }

                try {
                    pipe.send(msg);
                } catch (IOException ex) {
                    if (LOG.isEnabledFor(Level.WARN)) {
                        LOG.warn("Failed to send message " + msg);
                    }
                }
            }
        } catch (Throwable all) {
            if (LOG.isEnabledFor(Level.WARN)) {
            LOG.warn("Uncaught Throwable in thread :" + 
                     Thread.currentThread().getName(), all);
            }
        }
    }
}
