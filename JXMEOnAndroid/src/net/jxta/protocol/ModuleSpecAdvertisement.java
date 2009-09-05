/*
 *  Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 *  reserved.
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
 *  $Id: ModuleSpecAdvertisement.java,v 1.2 2005/05/24 02:39:57 hamada Exp $
 */
package net.jxta.protocol;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.jxta.document.Element;
import net.jxta.document.ExtendableAdvertisement;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentUtils;
import net.jxta.id.ID;
import net.jxta.platform.ModuleSpecID;

/**
 *  A ModuleSpecAdvertisement describes a module specification. Its main purpose
 *  is to provide references to the documentation needed in order to create
 *  conforming implementations of that specification. A secondary use is,
 *  optionally, to make running instances usable remotely, by publishing any or
 *  all of the following:<br>
 *
 *  <ul>
 *    <li> PipeAdvertisement
 *    <li> ModuleSpecID of a proxy module
 *    <li> ModuleSpecID of an authenticator module
 *  </ul>
 *  Not all modules are usable remotely, it is up to the specification creator
 *  to make that choice. However, if the specification dictates it, all
 *  implementations can be expected to support it. <p>
 *
 *  Note that the Standard PeerGroup implementation of the java reference
 *  implementation does <em>not</em> support replacing a group service with a
 *  pipe to a remote instance. However, nothing prevents a particular
 *  implementation of a group from using a proxy module in place of the fully
 *  version; provided that the API (and therefore the ClassIDs) of the proxy and
 *  local versions are identical. <p>
 *
 *  Note also that in the case of the local+proxy style, it is up to the
 *  implementation of both sides to figure-out which pipe to listen to or
 *  connect to. The safest method is probably for the full version to seek its
 *  own ModuleSpecAdvertisement, and for the proxy version to accept the full
 *  version's ModuleSpecAdvertisement as a parameter. Alternatively if the proxy
 *  version is completely dedicated to the specification that it proxies, both
 *  sides may have the PipeID and type hard-coded.
 *
 *@see    net.jxta.platform.ModuleSpecID
 *@see    net.jxta.protocol.PipeAdvertisement
 *@see    net.jxta.protocol.ModuleImplAdvertisement
 *@see    net.jxta.document.Advertisement
 */
public abstract class ModuleSpecAdvertisement extends ExtendableAdvertisement
         implements Cloneable {
    /**
     *  Description of the Field
     */
    public final static String authIdTag = "Auth";
    private ModuleSpecID authSpecID = null;
    private String creator = null;
    public final static String creatorTag = "Crtr";
    public final static String descTag = "Desc";
    private String description = null;
    private ModuleSpecID id = null;
    public final static String idTag = "MSID";
    protected Map indexMap = new HashMap();
    private String name = null;
    public final static String nameTag = "Name";

    // The module interprets this. It is not necessarily final and immutable
    // so it may need cloning for making a fully correct clone adv.
    private StructuredDocument param = null;
    public final static String paramTag = "Parm";
    private PipeAdvertisement pipeAdv = null;
    public final static String proxyIdTag = "Proxy";
    private ModuleSpecID proxySpecID = null;
    private String uri = null;
    public final static String uriTag = "SURI";
    public final static String versTag = "Vers";
    protected final static String[] fields = {nameTag, idTag};
    private String version = null;

    /**
     *  Clone this ModuleSpecAdvertisement
     *
     *@return    Object an object of class ModuleSpecAdvertisement that is a
     *      deep-enough copy of this one.
     */
    public Object clone() {

        // All members are either immutable or never modified nor allowed to
        // be modified: all accessors return clones. IDs are know to be
        // immutable but that could change. clone() them for safety; their
        // clone method costs nothing.
        try {
            return super.clone();
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
        return "jxta:MSA";
    }

    /**
     *  returns the specID of an authenticator module.
     *
     *@return    ModuleSpecID the spec id
     */

    public ModuleSpecID getAuthSpecID() {
        return authSpecID;
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
     *  Returns the creator of the module spec, in case someone cares.
     *
     *@return    String the creator.
     */

    public String getCreator() {
        return creator;
    }

    /**
     *  returns the keywords/description associated with this class
     *
     *@return    String keywords/description associated with the class
     */

    public String getDescription() {
        return description;
    }

    /**
     *  returns a unique id for that adv for the purpose of indexing. The spec
     *  id uniquely identifies this advertisement.
     *
     *@return    ID the spec id as a basic ID.
     */

    public ID getID() {
        return id;
    }

    /**
     *  {@inheritDoc}
     *
     *@return    The indexMap value
     */
    public final Map getIndexMap() {
        return Collections.unmodifiableMap(indexMap);
    }

    /**
     *  returns the id of the spec
     *
     *@return    ModuleSpecID the spec id
     */

    public ModuleSpecID getModuleSpecID() {
        return id;
    }

    /**
     *  returns the name of the module spec
     *
     *@return    String name of the module spec
     */

    public String getName() {
        return name;
    }

    /**
     *  returns the param element.
     *
     *@return    Element parameters as an Element of unspecified content.
     */

    public StructuredDocument getParam() {
        return (param == null ? null : StructuredDocumentUtils.copyAsDocument(param));
    }

    /**
     *  Privileged version of {@link #getParam()} that does not clone the
     *  elements.
     *
     *@return    StructuredDocument A stand-alone structured document of
     *      unspecified content.
     */
    protected StructuredDocument getParamPriv() {
        return param;
    }

    /**
     *  returns the embedded pipe advertisement if any.
     *
     *@return    PipeAdvertisement the Pipe Advertisement. null if none exists.
     */

    public PipeAdvertisement getPipeAdvertisement() {
        return (pipeAdv == null ? null : (PipeAdvertisement) pipeAdv.clone());
    }

    /**
     *  returns the specID of a proxy module.
     *
     *@return    ModuleSpecID the spec id
     */

    public ModuleSpecID getProxySpecID() {
        return proxySpecID;
    }

    /**
     *  returns the uri. This uri normally points at the actual specification
     *  that this advertises.
     *
     *@return    String uri
     */

    public String getSpecURI() {
        return uri;
    }

    /**
     *  returns the specification version number
     *
     *@return    String version number
     */

    public String getVersion() {
        return version;
    }

    /**
     *  sets an authenticator module specID
     *
     *@param  authSpecID  The spec id
     */

    public void setAuthSpecID(ModuleSpecID authSpecID) {
        this.authSpecID = authSpecID;
    }

    /**
     *  Sets the creator of this module spec. Note: the usefulness of this is
     *  unclear.
     *
     *@param  creator  name of the creator of the module
     */

    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     *  sets the description associated with this class
     *
     *@param  description
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *  sets the id of the spec
     *
     *@param  id  The id of the spec
     */

    public void setModuleSpecID(ModuleSpecID id) {
        this.id = id;
        if (id != null) {
            indexMap.put(idTag, id.toString());
        } else {
            indexMap.remove(idTag);
        }
    }

    /**
     *  sets the name of the module spec
     *
     *@param  name  name of the module spec to be set
     */

    public void setName(String name) {
        this.name = name;
        if (name != null) {
            indexMap.put(nameTag, name);
        } else {
            indexMap.remove(nameTag);
        }
    }

    /**
     *  sets the param element.
     *
     *@param  param  Element of an unspecified content.
     */

    public void setParam(Element param) {
        this.param = (param == null ? null : StructuredDocumentUtils.copyAsDocument(param));
    }

    /**
     *  sets an embedded pipe advertisement.
     *
     *@param  pipeAdv  the Pipe Advertisement. null is authorized.
     */

    public void setPipeAdvertisement(PipeAdvertisement pipeAdv) {
        this.pipeAdv = (pipeAdv == null ? null : (PipeAdvertisement) pipeAdv.clone());
    }

    /**
     *  sets a proxy module specID
     *
     *@param  proxySpecID  The spec id
     */

    public void setProxySpecID(ModuleSpecID proxySpecID) {
        this.proxySpecID = proxySpecID;
    }

    /**
     *  sets the uri
     *
     *@param  uri  string uri
     */

    public void setSpecURI(String uri) {
        this.uri = uri;
    }

    /**
     *  sets the version of the module
     *
     *@param  version  version number
     */

    public void setVersion(String version) {
        this.version = version;
    }
}

