/*
 *  Copyright (c) 2001-2003 Sun Microsystems, Inc.  All rights reserved.
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
 *  $Id: AccessPointAdvertisement.java,v 1.3 2005/06/13 15:21:32 hamada Exp $
 */
package net.jxta.protocol;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import net.jxta.peer.PeerID;
import net.jxta.document.ExtendableAdvertisement;
import net.jxta.endpoint.EndpointAddress;

/**
 *  A short representation of the physical endpoint addresses available for a
 *  specific peer. A peer can have many physical endpoint addresses. One or more
 *  endpoint addresses for each configured network interfaces or protocol (IP,
 *  HTTP, etc. ). The AccessPoint advertisement is commonly part of the route
 *  advertisement (see RouteAdvertisement) to describe the endpoints available
 *  on the member peer of the peer group.
 *
 *@see    net.jxta.protocol.PeerAdvertisement
 *@see    net.jxta.protocol.PeerGroupAdvertisement
 *@see    net.jxta.protocol.RouteAdvertisement
 */
public abstract class AccessPointAdvertisement extends ExtendableAdvertisement implements Cloneable {
    public final static String EA_TAG = "EA";
    public final static String PID_TAG = "PID";
    protected final static String[] INDEXFIELDS = {PID_TAG};
    protected Map indexMap = new HashMap();

    /**
     *  The EndpointAddresses associated with the specified peer. <p/>
     *
     *  XXX 20041129 bondolo This SHOULD be a {@link java.util.Set}. <p/>
     *
     *
     *  <ul>
     *    <li> Values are, sadly, {@link java.lang.String} of {@link
     *    net.jxta.endpoint.EndpointAddress}.</li>
     *  </ul>
     *
     */
    private Vector endpointAddresses = new Vector();

    /**
     *  The peer id of the peer with these endpoints. May be <code>null</code>
     *  if the APA is used as a sub-element of a structure in which the context
     *  peerid is already known.
     */
    private PeerID pid = null;

    /**
     *  add a new EndpointAddresses to the access point
     *
     *@param  address  An EndpointAddress
     */
    public void addEndpointAddress(EndpointAddress address) {
        endpointAddresses.add(address.toString());
    }

    /**
     *  add a new EndpointAddresses to the access point
     *
     *@param  address  EndpointAddress respresented as {@link java.lang.String}.
     */
    public void addEndpointAddress(String address) {
        endpointAddresses.add(address);
    }

    /**
     *  Add a new list of EndpointAddresses to the access point
     *
     *@param  addresses  List of EndpointAddresses respresented as {@link
     *      java.lang.String}.
     */
    public void addEndpointAddresses(List addresses) {
        endpointAddresses.addAll(addresses);
    }

    /**
     *  {@inheritDoc}
     *
     *@return    Description of the Return Value
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException impossible) {
            throw new Error("Object.clone() threw CloneNotSupportedException", impossible);
        }
    }

    /**
     *  Check if the EndpointAddress is already in the access point
     *
     *@param  addr  endpoint address to check
     *@return       Description of the Return Value
     */
    public boolean contains(EndpointAddress addr) {
        return endpointAddresses.contains(addr.toString());
    }

    /**
     *  Generate a string that displays an access point information for logging
     *  or debugging purpose
     *
     *@return    String return a string containing the access point
     *      advertisement
     */
    public String display() {

        StringBuffer routeBuf = new StringBuffer();

        routeBuf.append("PID=");

        PeerID peerId = getPeerID();
        if (peerId == null) {
            routeBuf.append("Null Destination");
        } else {
            routeBuf.append(peerId.toString());
        }

        Enumeration e = getEndpointAddresses();
        while (e.hasMoreElements()) {
            routeBuf.append("\n Addr=" + e.nextElement());
        }
        return routeBuf.toString();
    }

    /**
     *  Compare if two access points are equals. Equals means the same PID and
     *  the same endpoint addresses.
     *
     *@param  target  AccessPoint advertisement
     *@return         boolean true or false if the access points are equals or
     *      not
     */
    public boolean equals(Object target) {

        if (this == target) {
            return true;
        }

        if (!(target instanceof AccessPointAdvertisement)) {
            return false;
        }

        AccessPointAdvertisement ap = (AccessPointAdvertisement) target;

        if (!getPeerID().equals(ap.getPeerID())) {
            return false;
        }

        Vector ad = ap.getVectorEndpointAddresses();

        //ok if we do not have endpoint addresses
        if (ad == null && endpointAddresses == null) {
            return true;
        }

        if (ad == null || endpointAddresses == null) {
            return false;
        }

        if (ad.size() != size()) {
            return false;
        }

        Iterator eachEndpointAddress = endpointAddresses.iterator();

        while (eachEndpointAddress.hasNext()) {
            if (!ad.contains(eachEndpointAddress.next())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the AdvertisementType
     *@return    The advBaseType value
     */
    public final String getAdvBaseType() {
        return getAdvertisementType();
    }

    /**
     *  Returns the identifying type of this Advertisement.
     *
     *@return    String the type of advertisement
     */
    public static String getAdvertisementType() {
        return "jxta:APA";
    }

    /**
     *  returns the list of endpoint addresses associated with this access
     *  point.
     *
     *@return    Enumeration of EndpointAddresses represented as {@link
     *      java.lang.String}.
     */
    public Enumeration getEndpointAddresses() {
        return endpointAddresses.elements();
    }

    /**
     *  Returns the PeerId of the associated access point.
     *
     *@return    PeerID the peer id
     */
    public PeerID getPeerID() {
        return pid;
    }

    /**
     *  Returns the vector of endpoint addresses associated with this access
     *  point. The result is a vector of endpoint addresses represented as
     *  String objects. <strong>The Vector contains the "live" data of this
     *  advertisement. It should be modified only with great care.</strong>
     *
     *@return    Vector of EndpointAddresses respresented as {@link
     *      java.lang.String}.
     */
    public Vector getVectorEndpointAddresses() {
        return endpointAddresses;
    }

    /**
     *  remove a list of EndpointAddresses from the access point
     *
     *@param  addresses  List of EndpointAddresses respresented as {@link
     *      java.lang.String}.
     */
    public void removeEndpointAddresses(List addresses) {
        endpointAddresses.removeAll(addresses);
    }

    /**
     *  Sets the list of endpoint addresses associated with this access point
     *
     *@param  addresses  Vector of EndpointAddresses respresented as {@link
     *      java.lang.String}.
     */
    public void setEndpointAddresses(Vector addresses) {
        endpointAddresses = addresses;
    }

    /**
     *  Sets the peerId of the associated access point.
     *
     *@param  pid  The id of the peer.
     */
    public void setPeerID(PeerID pid) {
        this.pid = pid;
        if (pid != null) {
            indexMap.put(PID_TAG, pid.toString());
        } else {
            indexMap.remove(PID_TAG);
        }
    }

    /**
     *  return number of endpoint addresses
     *
     *@return    size number of endpointAddress in the hop
     */
    public int size() {
        return endpointAddresses.size();
    }

    /**
     *  {@inheritDoc}
     */
    public final Map getIndexMap() {
        return  Collections.unmodifiableMap(indexMap);
    }
}

