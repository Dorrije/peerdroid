/*
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
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
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *    not be used to endorse or promote products derived from this
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 * $Id: IPUtils.java,v 1.1 2005/05/11 02:26:58 hamada Exp $
 */

package net.jxta.impl.endpoint;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Utility methods for use by IP based transports.
 */
public final class IPUtils {

    /**
     * Log4j Logger
     */
    private final static Logger LOG = Logger.getLogger(IPUtils.class.getName());

    final static Random random = new Random();

    final static String IPV4ANYADDRESS = "0.0.0.0";
    final static String IPV6ANYADDRESS = "::";

    final static String IPV4LOOPBACK = "127.0.0.1";
    final static String IPV6LOOPBACK = "::1";

    /**
     *  Constant which works as the IP "Any Address" value
     */
    public final static InetAddress ANYADDRESS;
    public static InetAddress ANYADDRESSV4;
    public static InetAddress ANYADDRESSV6;

    /**
     *  Constant which works as the IP "Local Loopback" value;
     */
    public final static InetAddress LOOPBACK;
    public static InetAddress LOOPBACKV4;
    public static InetAddress LOOPBACKV6;

    static {
        try {
            ANYADDRESSV4 = InetAddress.getByName(IPV4ANYADDRESS);
        } catch (Exception ignored) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("failed to intialize ANYADDRESSV4. Not fatal");
            }
        }

        try {
            ANYADDRESSV6 = InetAddress.getByName(IPV6ANYADDRESS);
        } catch (Exception ignored) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("failed to intialize IPV6ANYADDRESS. Not fatal");
            }
        }

        ANYADDRESS = (ANYADDRESSV4 == null) ? ANYADDRESSV6 : ANYADDRESSV4;

        try {
            LOOPBACKV4 = InetAddress.getByName(IPV4LOOPBACK);
        } catch (Exception ignored) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("failed to intialize IPV4LOOPBACK. Not fatal");
            }
        }

        try {
            LOOPBACKV6 = InetAddress.getByName(IPV6LOOPBACK);
        } catch (Exception ignored) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("failed to intialize ANYADDRESSV4. Not fatal");
            }
        }

        LOOPBACK = (LOOPBACKV4 == null) ? LOOPBACKV6 : LOOPBACKV4;

        if (LOOPBACK == null || ANYADDRESS == null) {
            if (LOG.isEnabledFor(Level.FATAL)) {
                LOG.fatal("failure initializing statics. Neither IPV4 nor IPV6 seem to work.");
            }

            throw new IllegalStateException("failure initializing statics. Neither IPV4 nor IPV6 seem to work.");
        }
    };

    /**
     *  This is a static utility class, you don't make instaces.
     */
    private IPUtils() {}

    /**
     *  Provide an iterator which returns all of the local InetAddresses for this
     *  host.
     *
     *  @return iterator of InetAddress which is all of the InetAddress for all
     *  local interfaces.
     */
    public static Iterator getAllLocalAddresses() {
        List allAddr = new ArrayList();

        Enumeration allInterfaces;
        try {
            allInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException caught) {
            if (LOG.isEnabledFor(Level.ERROR)) {
                LOG.error("Could not get local interfaces list", caught);
            }

            allInterfaces = Collections.enumeration(Collections.EMPTY_LIST);
        }

        while(allInterfaces.hasMoreElements()) {
            NetworkInterface anInterface = (NetworkInterface) allInterfaces.nextElement();

            try {
                Enumeration allIntfAddr = anInterface.getInetAddresses();

                while(allIntfAddr.hasMoreElements()) {
                    InetAddress anAddr = (InetAddress) allIntfAddr.nextElement();

                    if(anAddr.isLoopbackAddress())
                        continue;

                    if(!allAddr.contains(anAddr))
                        allAddr.add(anAddr);
                }
            } catch (Throwable caught) {
                if (LOG.isEnabledFor(Level.ERROR)) {
                    LOG.error("Could not get addresses for " + anInterface, caught);
                }
            }
        }

        // if nothing suitable was found then return loopback address.
        if(allAddr.isEmpty() || Boolean.getBoolean("net.jxta.impl.IPUtils.localOnly")) {
            if(null != LOOPBACKV4) {
                allAddr.add(LOOPBACKV4);
            }

            if(null != LOOPBACKV6) {
                allAddr.add(LOOPBACKV6);
            }

            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Adding loopback interfaces");
            }
        }

        return allAddr.iterator();
    }
}
