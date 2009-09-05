/*
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
 *  $Id: PeerAdvertisement.java,v 1.2 2005/05/24 02:39:57 hamada Exp $
 */
package net.jxta.protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import net.jxta.document.Element;
import net.jxta.document.ExtendableAdvertisement;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredDocumentUtils;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *  This type of advertisement is generated when instantiating a group on a peer
 *  and contains all the parameters that services need to publish. It is then
 *  published within the the group.
 */
public abstract class PeerAdvertisement extends ExtendableAdvertisement implements Cloneable {
    /**
     *  Log4J Logger
     */
    protected final static Logger LOG = Logger.getLogger(PeerAdvertisement.class.getName());
    public final static String descTag = "Desc";
    public final static String gidTag = "GID";
    public final static String mcidTag = "MCID";
    public final static String nameTag = "Name";
    public final static String paramTag = "Parm";
    public final static String pidTag = "PID";
    public final static String svcTag = "Svc";
    protected final static String[] fields = {nameTag, pidTag};
    protected Map indexMap = new HashMap();

    /**
     *  Descriptive meta-data about this peer.
     */
    private Element description = null;

    /**
     *  The group in which this peer is located.
     */
    private PeerGroupID gid = null;

    /**
     *  Counts the changes made to this object. The API increments it every time
     *  some change is not proven to be idempotent. We rely on implementations
     *  to increment modCount every time something is changed without going
     *  through the API.
     */
    protected volatile int modCount = 0;

    /**
     *  The name of this peer. Not guaranteed to be unique in any way. May be
     *  empty or null.
     */
    private String name = null;

    /*
     *  FIXME: [20011001 jice@jxta.org]
     *  ideally Advertisements should be immutable, but then they become too
     *  cumbersome to construct. Therefore we would need an immutable class
     *  and a mutable subclass, and provide one or the other depending on some
     *  yet to defined privilege or something like that...later.
     *
     *  Currently what we do is to make it properly clonable, so that
     *  copies can be safely modified independently. The cost is still small
     *  because most members are actually immutable and thus implement a trivial
     *  clone() method. To be safe with future modifications of the code we do not
     *  assume that classes other than String are indeed immutable. We just rely
     *  on their clone() method being as efficient as can be.
     */
    /**
     *  The id of this peer.
     */
    private PeerID pid = null;

    // A table of structured documents to be interpreted by each service.
    // For cost and convenience reasons the elements in this table are
    // not cloned when the table is set or returned. For safe operation these
    // elements should be immutable, but we're helpless if they are not.
    /**
     *  Parameters for services associated with this peer. May be needed for
     *  invocation of those services.
     */
    private Hashtable serviceParams = new Hashtable();

    /**
     *  Make a safe clone of this PeerAdvertisement.
     *
     *@return    Object an object of class PeerAdvertisement that is a
     *      deep-enough copy of this one.
     */
    public Object clone() {
        // IDs are know to be immutable but that could change. clone() them
        // for safety; their clone method costs nothing.

        // Shallow clone the params table.
        // The individual elements do not need cloning because we never allow
        // them to be altered. Instead accessor methods always return clones.
        // However, there are mutators to add/remove elements to/from the
        // table, which is far less expensive than cloning the whole table
        // contents out and in. Since this table is never very big,
        // it is cheaper to just clone it whole now than playing games.

        // We let the modCount be reset to zero. The fact that the object are
        // different can be detected by comparing the references. modCount
        // only serves to detect changes in one particular object.

        try {
            PeerAdvertisement result = (PeerAdvertisement) super.clone();
            result.serviceParams = (Hashtable) serviceParams.clone();
            return result;
        } catch (CloneNotSupportedException impossible) {
            return null;
        }
    }

    /**
     *  Returns the identifying type of this Advertisement.
     *
     *@return    String the type of advertisement
     */
    public static String getAdvertisementType() {
        return "jxta:PA";
    }

    /**
     *  {@inheritDoc}
     *
     *@return    The baseAdvType value
     */
    public final String getBaseAdvType() {
        return getAdvertisementType();
    }

    /**
     *  returns the description
     *
     *@return    the description
     */
    public StructuredDocument getDesc() {
        if (null != description) {
            StructuredDocument newDoc =
                    StructuredDocumentUtils.copyAsDocument(description);

            return newDoc;
        } else {
            return null;
        }
    }

    /**
     *  returns the description
     *
     *@return    String the description
     */
    public String getDescription() {
        if (null != description) {
            return (String) description.getValue();
        } else {
            return null;
        }
    }

    /**
     *  Returns a unique ID for that peer X group intersection. This is for
     *  indexing purposes only. <p/>
     *
     *  We return a composite ID that represents this peer is this group rather
     *  than in the platform, which is what the regular peerId shows. <p/>
     *
     *  May-be one day we'll want to name a peer differently in each group,
     *  exactly in this way. In the meantime we still need it to uniquely
     *  identify this adv. <p/>
     *
     *  FIXME 20020604 bondolo@jxta.org This is a total hack as it assumes the
     *  format of a group id. It's supposed to be opaque. The real answer is to
     *  use a unique value within each group.
     *
     *@return    ID the composite ID
     */

    public ID getID() {

        // If it is incomplete, there's no meaninfull ID that we can return.
        if (gid == null || pid == null) {
            return null;
        }

        String peer;
        // That's tricky; we're not realy supposed to do that...

        // Get the grp unique string of hex. Clip the two type bytes
        // at the end.
        if (gid.equals(PeerGroupID.defaultNetPeerGroupID)
                 || gid.equals(PeerGroupID.worldPeerGroupID)) {

            peer = pid.getUniqueValue().toString();

        } else {
            String grp = gid.getUniqueValue().toString();
            grp = grp.substring(0, grp.length() - 2);

            // Get the peer unique string whih starts with the platform's unique
            // string.
            peer = pid.getUniqueValue().toString();
            // Replace the platform's unique portion with this group's id.
            peer = grp + peer.substring(grp.length());
        }

        // Forge a URI form for this chimaera and build a new PeerID out of it.
        try {
            return IDFactory.fromURI(new URI(ID.URIEncodingName + ":" + ID.URNNamespace + ":" + peer));
        } catch (URISyntaxException iDontMakeMistakes) {
            // Fall through,  iDontMakeMistakes sometimes makes mistakes :)
            //iDontMakeMistakes.printStackTrace();
            ;
        }
        // May be if we had an "internal error exception" we should throw it.
        return null;
    }

    /**
     *  Returns the number of times this object has been modified since it was
     *  created. This permits to detect local changes that require refreshing
     *  some other data.
     *
     *@return    int the current modification count.
     */

    public int getModCount() {
        return modCount;
    }

    /**
     *  returns the name of the peer.
     *
     *@return    String name of the peer.
     *@since     JXTA 1.0
     */

    public String getName() {
        return name;
    }

    /**
     *  Returns the id of the peergroup this peer advertisement is for.
     *
     *@return    PeerGroupID the peergroup id
     */

    public PeerGroupID getPeerGroupID() {
        return gid;
    }

    /**
     *  Returns the id of the peer.
     *
     *@return    PeerID the peer id
     */

    public PeerID getPeerID() {
        return pid;
    }

    /**
     *  Returns the parameter element that matches the given key from the
     *  service parameters table. The key is of a subclass of ID; usually a
     *  ModuleClassID.
     *
     *@param  key  The key.
     *@return      StructuredDocument The matching parameter document or null if
     *      none matched. The document type id "Param".
     */
    public StructuredDocument getServiceParam(ID key) {
        Element param = (Element) serviceParams.get(key);
        if (param == null) {
            return null;
        }

        StructuredDocument newDoc =
                StructuredDocumentUtils.copyAsDocument(param);
        return newDoc;
    }

    /**
     *  Returns the sets of parameters for all services. This method returns a
     *  deep copy, in order to protect the real information from uncontrolled
     *  sharing while keeping it shared as long as it is safe. This quite an
     *  expensive operation. If only a few parameters need to be accessed, it is
     *  wise to use getServiceParam() instead.
     *
     *@return    Returns the sets of parameters for all services.
     */
    public Hashtable getServiceParams() {
        Hashtable copy = new Hashtable();

        Enumeration keys = serviceParams.keys();
        while (keys.hasMoreElements()) {
            ID key = (ID) keys.nextElement();
            Element e = (Element) serviceParams.get(key);
            Element newDoc = StructuredDocumentUtils.copyAsDocument(e);
            copy.put(key, newDoc);
        }
        return copy;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    protected int incModCount() {
        if (LOG.isEnabledFor(Level.DEBUG)) {
            Throwable trace = new Throwable("Stack Trace");
            StackTraceElement elements[] = trace.getStackTrace();

            LOG.debug("Modification #" + (modCount + 1) + " to PeerAdv@" + Integer.toHexString(System.identityHashCode(this)) + " caused by : " +
                    "\n\t" + elements[1] +
                    "\n\t" + elements[2]);
        }

        return modCount++;
    }

    /**
     *  Puts a service parameter in the service parameters table under the given
     *  key. The key is of a subclass of ID; usually a ModuleClassID. This
     *  method makes a deep copy of the given element into an independent
     *  document.
     *
     *@param  key    The key.
     *@param  param  The parameter, as an element. What is stored is a copy as a
     *      stand alone StructuredDocument which type is the element's name.
     */
    public void putServiceParam(ID key, Element param) {
        incModCount();

        if (param == null) {
            serviceParams.remove(key);
            return;
        }
        Element newDoc = StructuredDocumentUtils.copyAsDocument(param);
        serviceParams.put(key, newDoc);
    }

    /**
     *  Removes and returns the parameter element that matches the given key
     *  from the service parameters table. The key is of a subclass of ID;
     *  usually a ModuleClassID.
     *
     *@param  key  The key.
     *@return      Element the removed parameter element or null if not found.
     *      This is actually a StructureDocument of type "Param".
     */
    public StructuredDocument removeServiceParam(ID key) {

        Element param = (Element) serviceParams.remove(key);
        if (param == null) {
            return null;
        }

        incModCount();

        // It sound silly to clone it, but remember that we could be sharing
        // this element with a clone of ours, so we have the duty to still
        // protect it.

        StructuredDocument newDoc =
                StructuredDocumentUtils.copyAsDocument(param);

        return newDoc;
    }

    /**
     *  sets the description
     *
     *@param  desc  the description
     */
    public void setDesc(Element desc) {

        if (null != desc) {
            this.description = StructuredDocumentUtils.copyAsDocument(desc);
        } else {
            this.description = null;
        }

        incModCount();
    }

    /**
     *  sets the description
     *
     *@param  description  the description
     */
    public void setDescription(String description) {

        if (null != description) {
            StructuredDocument newdoc =
                    StructuredDocumentFactory.newStructuredDocument(
                    MimeMediaType.XMLUTF8, "Desc", description);

            setDesc(newdoc);
        } else {
            this.description = null;
        }

        incModCount();
    }

    /**
     *  sets the name of the peer.
     *
     *@param  name  name of the peer.
     */

    public void setName(String name) {
        this.name = name;
        if (name != null) {
            indexMap.put(nameTag, name);
        } else {
            indexMap.remove(nameTag);
        }
        incModCount();
    }

    /**
     *  Returns the id of the peergroup this peer advertisement is for.
     *
     *@param  gid  The id of the peer.
     */

    public void setPeerGroupID(PeerGroupID gid) {
        this.gid = gid;
        incModCount();
    }

    /**
     *  Sets the id of the peer.
     *
     *@param  pid  the id of this peer.
     */

    public void setPeerID(PeerID pid) {
        this.pid = pid;
        if (pid != null) {
            indexMap.put(pidTag, pid.toString());
        } else {
            indexMap.remove(pidTag);
        }
        incModCount();
    }

    /**
     *  sets the sets of parameters for all services. This method first makes a
     *  deep copy, in order to protect the active information from uncontrolled
     *  sharing. This quite an expensive operation. If only a few of the
     *  parameters need to be added, it is wise to use putServiceParam()
     *  instead.
     *
     *@param  params  The whole set of parameters.
     */
    public void setServiceParams(Hashtable params) {

        incModCount();

        if (params == null) {
            serviceParams = new Hashtable();
            return;
        }

        Hashtable copy = new Hashtable();

        Enumeration keys = params.keys();
        while (keys.hasMoreElements()) {
            ID key = (ID) keys.nextElement();
            Element e = (Element) params.get(key);
            Element newDoc = StructuredDocumentUtils.copyAsDocument(e);
            copy.put(key, newDoc);
        }

        serviceParams = copy;
    }
    /**
     *  {@inheritDoc}
     */
    public final Map getIndexMap() {
        return  Collections.unmodifiableMap(indexMap);
    }
}

