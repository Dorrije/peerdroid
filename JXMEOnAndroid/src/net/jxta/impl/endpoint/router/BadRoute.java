/*
 *
 * $Id: BadRoute.java,v 1.1 2005/05/11 02:27:02 hamada Exp $
 *
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

package net.jxta.impl.endpoint.router;


import java.util.Enumeration;
import java.util.Vector;

import net.jxta.peer.PeerID;
import net.jxta.protocol.RouteAdvertisement;


/**
 * This class is used to cache negative route information. Bad routes
 * are represented by three fields:
 *  - route advertisement
 *  - expiration time of the negative cache
 *  - vectors or hops that are known bad for that route
 */

public class BadRoute {
  
    private Long expiration;
    private RouteAdvertisement badRoute = null;
    private Vector badHops = new Vector();

    BadRoute(RouteAdvertisement route, Long exp, Vector hops) {
        this.badRoute = route;
        this.expiration = exp;

        if (hops != null) {
            for (Enumeration e = hops.elements(); e.hasMoreElements();) {
                this.badHops.add((PeerID) e.nextElement());
            }
        }
    }

    /**
     * return the bad route info
     * 
     * @return bad route advertisement
     */
    public RouteAdvertisement getRoute() {
        if (badRoute != null) {
            return (RouteAdvertisement) badRoute.clone();
        } else {
            return null;
        }
    }

    /**
     * set the bad route info
     * 
     * @param route bad route advertisement
     */
    public void setRoute(RouteAdvertisement route) {
        this.badRoute = route;
    }

    /**
     * return the bad route expiration time
     * 
     * @return bad route  expiration time
     */
    public Long getExpiration() {
        return expiration;
    }

    /**
     * set the bad route expiration time
     * 
     * @param exp bad route expiration time
     */
    public void setExpiration(Long exp) {
        this.expiration = exp;
    }

    /**
     * return the known bad hops in the route
     * 
     * @return bad route hops
     */
    public Vector getHops() {
        Vector hops = new Vector();

        if (badHops != null) {
            for (Enumeration e = badHops.elements(); e.hasMoreElements();) {
                hops.add((PeerID) e.nextElement());
            }
        }
        return hops;
    }

    /**
     * set bad hops into the bad route
     * 
     * @param hops bad route hops
     */
    public void setHops(Vector hops) {

        badHops = new Vector();
        if (hops != null) {
            for (Enumeration e = hops.elements(); e.hasMoreElements();) {
                this.badHops.add((PeerID) e.nextElement());
            }
        }
    }
  
    /**
     * add bad hops into the bad route
     * 
     * @param hops bad route hops
     */
    public void addHops(Vector hops) {
        if (hops != null) {
            for (Enumeration e = hops.elements(); e.hasMoreElements();) {
                this.badHops.add((PeerID) e.nextElement());
            }
        }
    }

    /**
     *
     *
     */
    public String display() {
        StringBuffer routeBuf = new StringBuffer();
        
        routeBuf.append("Bad " + getRoute().display());
        routeBuf.append("   Exp:" + getExpiration());
        routeBuf.append("   Hops:");
        for (Enumeration e = getHops().elements(); e.hasMoreElements();) {
            routeBuf.append(((PeerID) e.nextElement()).toString());
        }
        return routeBuf.toString();
    }
}

