/*
 *  Copyright (c) 2003 Sun Microsystems, Inc.  All rights reserved.
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
package net.jxta.impl.util.pipe.reliable;

import java.io.IOException;
import java.lang.IllegalStateException;
import java.util.ArrayList;
import java.util.List;
import net.jxta.endpoint.Message;
import net.jxta.impl.util.TimeUtils;
import net.jxta.pipe.OutputPipe;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *  Description of the Class
 */
public class OutgoingPipeAdaptorSync implements Outgoing {

    private final static Logger LOG = Logger.getLogger(OutgoingPipeAdaptor.class.getName());
    private boolean closed = false;
    private long lastAccessed = 0;
    private OutputPipe pipe = null;

    /**
     *  Constructor for the OutgoingPipeAdaptorSync object
     *
     *@param  pipe  Description of the Parameter
     */
    public OutgoingPipeAdaptorSync(OutputPipe pipe) {
        // Null permitted. Send will block until setPipe is called.
        this.pipe = pipe;
        // initialize to some reasonable value
        lastAccessed = TimeUtils.timeNow();
    }

    /**
     *  Description of the Method
     *
     *@exception  IOException  Description of the Exception
     */
    public void close() throws IOException {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        if (pipe != null) {
            pipe.close();
            pipe = null;
        }
    }

    // Default should be "never", otherwise, connection closes while not
    // in active use and ReliableOutputStream does NOT reconnect automatically.
    /**
     *  Gets the idleTimeout attribute of the OutgoingPipeAdaptorSync object
     *
     *@return    The idleTimeout value
     */
    public long getIdleTimeout() {
        return Long.MAX_VALUE;
    }

    /**
     *  Gets the lastAccessed attribute of the OutgoingPipeAdaptorSync object
     *
     *@return    The lastAccessed value
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

    // This is the important tunable: how long to wait on a stale connection.
    /**
     *  Gets the maxRetryAge attribute of the OutgoingPipeAdaptorSync object
     *
     *@return    The maxRetryAge value
     */
    public long getMaxRetryAge() {
        return 1 * TimeUtils.AMINUTE;
    }

    // Obsolete.
    /**
     *  Gets the minIdleReconnectTime attribute of the OutgoingPipeAdaptorSync
     *  object
     *
     *@return    The minIdleReconnectTime value
     */
    public long getMinIdleReconnectTime() {
        return 10 * TimeUtils.AMINUTE;
    }

    /**
     *  Description of the Method
     *
     *@param  msg              Description of the Parameter
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    public boolean send(Message msg) throws IOException {

        OutputPipe locPipe = null;

        synchronized (this) {
            while (pipe == null && !closed) {
                try {
                    wait();
                } catch (InterruptedException ignore) {
                }
            }
            if (closed) {
                return false;
            }
            locPipe = pipe;
        }
        locPipe.send(msg);
        return true;
    }

    /**
     *  Asynchronously send a message
     *
     *@param  msg              message to send
     *@return                  true upon sucess
     *@exception  IOException  if an io error occurs
     */
    public boolean sendNb(Message msg) throws IOException {
        OutputPipe locPipe = null;
        synchronized (this) {
            locPipe = pipe;
        }
        if (closed || locPipe == null) {
            return false;
        }
        locPipe.send(msg);
        return true;
    }

    /**
     *  Sets the lastAccessed attribute of the OutgoingPipeAdaptorSync object
     *
     *@param  time  The new lastAccessed value
     */
    public void setLastAccessed(long time) {
        lastAccessed = time;
    }

    /**
     *  Sets the pipe attribute of the OutgoingPipeAdaptorSync object
     *
     *@param  pipe  The new pipe value
     */
    public void setPipe(OutputPipe pipe) {
        synchronized (this) {
            if (closed || this.pipe != null) {
                throw new IllegalStateException("Cannot change pipe nor re-open");
            }
            this.pipe = pipe;
            notifyAll();
        }
    }

    /**
     *  for debugging display purposes
     *
     *@return    object info string
     */
    public String toString() {
        return ((pipe == null) ? "no pipe yet" : pipe.toString()) +
                " lastAccessed=" + Long.toString(lastAccessed);
    }
}

