/*
 *  $Id: Outgoing.java,v 1.1 2005/08/23 00:14:06 hamada Exp $
 *
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
import net.jxta.endpoint.Message;

/**
 *  This interface specifies the methods that must be implemented by a
 *  connection in order to be able to reliably deliver messages over the
 *  connection.
 */
public interface Outgoing {

    /**
     *  Send a message to the remote peer.
     *
     *@param  msg              message to send
     *@return                  true upon sucess
     *@exception  IOException  if an io error occurs
     */
    boolean send(Message msg) throws IOException;

    /**
     *  Close the connection.
     *
     *@exception  IOException  if an io error occurs
     */
    void close() throws IOException;

    /**
     *  Amount of a connection must be idle before a reconnection attempt will
     *  be considered.
     *
     *@return    The minIdleReconnectTime value
     */
    long getMinIdleReconnectTime();

    /**
     *  Amount of time after which a connection is considered idle and may be
     *  scavenged.
     *
     *@return    The idleTimeout value
     */
    long getIdleTimeout();

    /**
     *  Amount if time during which retries may remain queued for
     *  retransmission. If still unACKed after this amount of time then the
     *  connection is considered dead.
     *
     *@return    The maxRetryAge value
     */
    long getMaxRetryAge();

    /**
     *  Get the time when this connection was last used.
     *
     *@return    The lastAccessed value
     */
    long getLastAccessed();

    /**
     *  Set the time when this connection was last used.
     *
     *@param  time  The new lastAccessed value
     */
    void setLastAccessed(long time);

    /**
     *  Return a human-friendly description of this connection.
     *
     *@return    Description of the Return Value
     */
    String toString();
}

