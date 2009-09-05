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
 * $Id: ModuleSpecAdv.java,v 1.1 2005/05/03 06:48:12 hamada Exp $
 */
package net.jxta.impl.protocol;

import java.net.URI;
import java.util.Enumeration;
import java.net.URISyntaxException;

import java.net.UnknownServiceException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.platform.ModuleSpecID;
import net.jxta.id.IDFactory;
import net.jxta.document.*;

public class ModuleSpecAdv extends ModuleSpecAdvertisement {

    /**
     *  Log4J Logger
     */
    private static final Logger LOG = Logger.getLogger(ModuleSpecAdv.class.getName());

    private static final String idTag = "MSID";
    private static final String nameTag = "Name";
    private static final String creatorTag = "Crtr";
    private static final String uriTag = "SURI";
    private static final String versTag = "Vers";
    private static final String descTag = "Desc";
    private static final String paramTag = "Parm";
    private static final String proxyIdTag = "Proxy";
    private static final String authIdTag = "Auth";
    private static final String [] fields = {nameTag, idTag};

    public static class Instantiator implements AdvertisementFactory.Instantiator {

        /**
         * Returns the identifying type of this Advertisement.
         *
         * @return String the type of advertisement
         *
         * @since JXTA 1.0
         */
        public String getAdvertisementType() {
            return ModuleSpecAdv.getAdvertisementType();
        }

        /**
         * Constructs an instance of <CODE>Advertisement</CODE> matching the type
         * specified by the <CODE>advertisementType</CODE> parameter.
         *
         * @param advertisementType Specifies the mime media type to be associated with the
         * <CODE>StructuredDocument</CODE> to be created.
         * @return The instance of <CODE>Advertisement</CODE> or null if it
         * could not be created.
         *
         * @exception InvocationTargetException error invoking target constructor
         *
         * @since JXTA 1.0
         */
        public Advertisement newInstance() {
            return new ModuleSpecAdv();
        }

        /**
         * Constructs an instance of <CODE>Advertisement</CODE> matching the type
         * specified by the <CODE>advertisementType</CODE> parameter.
         *
         * @param root Specifies a portion of a StructuredDocument which will be
         * converted into an Advertisement.
         * @return The instance of <CODE>Advertisement</CODE> or null if it
         * could not be created.
         *
         * @exception InvocationTargetException error invoking target constructor
         *
         * @since JXTA 1.0
         */
        public Advertisement newInstance(net.jxta.document.Element root) {
            return new ModuleSpecAdv(root);
        }
    };

    public ModuleSpecAdv() {
        setDescription(null);
        setName(null);
        setModuleSpecID(null);
        setCreator(null);
        setSpecURI(null);
        setVersion(null);
        setDescription(null);
        setPipeAdvertisement(null);
        setProxySpecID(null);
        setAuthSpecID(null);
        setParam(null);
    }

    public ModuleSpecAdv(Element root) {
        if(!XMLElement.class.isInstance(root))
            throw new IllegalArgumentException(getClass().getName() + " only supports XLMElement");

        XMLElement doc = (XMLElement) root;

        String doctype = doc.getName();

        String typedoctype = "";
        Attribute itsType = doc.getAttribute("type");
        if(null != itsType)
            typedoctype = itsType.getValue();

        if(!doctype.equals(getAdvertisementType()) && !getAdvertisementType().equals(typedoctype)) {
            throw new IllegalArgumentException("Could not construct : "
                                               + getClass().getName() + "from doc containing a " + doc.getName());
        }

        Enumeration elements = doc.getChildren();

        while (elements.hasMoreElements()) {
            XMLElement elem = (XMLElement) elements.nextElement();

            if(!handleElement(elem)) {
                if (LOG.isEnabledFor(Level.DEBUG))
                    LOG.debug("Unhandled Element: " + elem.toString());
            }
        }

        // Sanity Check!!!

    }

    /**
     *  {@inheritDoc}
     */
    protected boolean handleElement(Element raw) {

        if (super.handleElement(raw))
            return true;

        XMLElement elem = (XMLElement) raw;

        String nm = elem.getName();

        if(nm.equals(nameTag)) {
            setName(elem.getTextValue());
            return true;
        }
        if(nm.equals(descTag)) {
            setDescription(elem.getTextValue());
            return true;
        }

        if (nm.equals(idTag)) {
            try {
                URI spID =  new URI(elem.getTextValue());
                setModuleSpecID((ModuleSpecID) IDFactory.fromURI(spID));
            } catch (URISyntaxException badID) {
                throw new IllegalArgumentException("Bad spec ID in advertisement");
            }
            catch (ClassCastException badID) {
                throw new IllegalArgumentException("Unusable ID in advertisement");
            }
            return true;
        }

        if (nm.equals(creatorTag)) {
            setCreator(elem.getTextValue());
            return true;
        }

        if (nm.equals(uriTag)) {
            setSpecURI(elem.getTextValue());
            return true;
        }

        if (nm.equals(versTag)) {
            setVersion(elem.getTextValue());
            return true;
        }

        if (nm.equals(paramTag)) {
            // Copy the element into a complete new document
            // which type matches the element name. There is no
            // API Advertisement for it, each module implementation
            // may have its own Advertisement subclass for its param.
            setParam(elem);
            return true;
        }

        if (nm.equals(proxyIdTag)) {
            try {
                URI spID = new URI(elem.getTextValue());
                setProxySpecID((ModuleSpecID) IDFactory.fromURI(spID));
            } catch (URISyntaxException badID) {
                throw new IllegalArgumentException("Bad proxy spec ID in advertisement");
            }
            return true;
        }

        if (nm.equals(authIdTag)) {
            try {
                URI spID = new URI(elem.getTextValue());
                setAuthSpecID((ModuleSpecID) IDFactory.fromURI(spID));
            } catch (URISyntaxException badID) {
                throw new IllegalArgumentException("Bad authenticator spec ID in advertisement");
            }
            catch (ClassCastException badID) {
                throw new IllegalArgumentException("Unusable ID in advertisement");
            }
            return true;
        }

        if (nm.equals(PipeAdvertisement.getAdvertisementType())) {
            try {
                PipeAdvertisement pipeAdv = (PipeAdvertisement)
                                            AdvertisementFactory.newAdvertisement(elem);
                setPipeAdvertisement(pipeAdv);
            } catch (ClassCastException wrongAdv) {
                throw new IllegalArgumentException("Bad pipe advertisement in advertisement");
            }
            return true;
        }

        return false;
    }

    /**
     *  {@inheritDoc}
     */
    public Document getDocument(MimeMediaType encodeAs) {
        StructuredDocument adv = (StructuredDocument) super.getDocument(encodeAs);

        Element e;
        e = adv.createElement(idTag, getModuleSpecID().toString());
        adv.appendChild(e);

        e = adv.createElement(nameTag, getName());
        adv.appendChild(e);

        e = adv.createElement(creatorTag, getCreator());
        adv.appendChild(e);

        e = adv.createElement(uriTag, getSpecURI());
        adv.appendChild(e);

        e = adv.createElement(versTag, getVersion());
        adv.appendChild(e);

        String description = getDescription();
        if(null != description) {
            e = adv.createElement(descTag, description);
            adv.appendChild(e);
        }
        e = getParamPriv();
        // Copy the param document as an element of adv.
        if (e != null) {
            // Force the element to be named "Parm" even if that is not
            // the name of paramDoc.
            StructuredDocumentUtils.copyElements(adv, adv, e, paramTag);
        }
        PipeAdvertisement pipeAdv = getPipeAdvertisement();
        if (pipeAdv != null) {
            StructuredTextDocument advDoc = (StructuredTextDocument)
                                            pipeAdv.getDocument(encodeAs);
            StructuredDocumentUtils.copyElements(adv, adv, advDoc);
        }
        ModuleSpecID tmpId = getProxySpecID();
        if (tmpId != null) {
            e = adv.createElement(proxyIdTag, tmpId.toString());
            adv.appendChild(e);
        }
        tmpId = getAuthSpecID();
        if (tmpId != null) {
            e = adv.createElement(authIdTag, tmpId.toString());
            adv.appendChild(e);
        }
        return adv;
    }

    /**
     *  {@inheritDoc}
     */
    public final String [] getIndexFields() {
        return fields;
    }
}
