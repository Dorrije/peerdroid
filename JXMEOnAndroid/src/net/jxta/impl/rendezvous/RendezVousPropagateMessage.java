/*
 *  $Id: RendezVousPropagateMessage.java,v 1.2 2005/08/31 06:32:57 hamada Exp $
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
package net.jxta.impl.rendezvous;

import java.net.URI;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.jxta.document.Attribute;
import net.jxta.document.Attributable;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLElement;
import net.jxta.document.XMLDocument;

import net.jxta.impl.id.UUID.UUID;
import net.jxta.impl.id.UUID.UUIDFactory;

/**
 *  This class defines the wire format of the Propagation header for messages.
 */
public class RendezVousPropagateMessage {
    /**
     *  Description of the Field
     */
    public final static String DestSNameTag = "DestSName";
    /**
     *  Description of the Field
     */
    public final static String DestSParamTag = "DestSParam";

    /**
     *  Log4J Logger
     */
    private final static transient Logger LOG = Logger.getLogger(RendezVousPropagateMessage.class.getName());

    /**
     *  Description of the Field
     */
    public final static String MSG_NAME = "jxta:RendezVousPropagateMessage";
    /**
     *  Description of the Field
     */
    public final static String MsgIdTag = "MessageId";

    /**
     *  Description of the Field
     */
    public final static String Name = "RendezVousPropagate";
    /**
     *  Description of the Field
     */
    public final static String PathTag = "Path";
    private int TTL = Integer.MIN_VALUE;
    /**
     *  Description of the Field
     */
    public final static String TTLTag = "TTL";
    private String destSName = null;
    private String destSParam = null;

    private UUID msgId = null;
    private final Set visited = new LinkedHashSet();

    /**
     *  Constructor for the RendezVousPropagateMessage object
     */
    public RendezVousPropagateMessage() { }

    /**
     *  Constructor for the RendezVousPropagateMessage object
     *
     *@param  root  Description of the Parameter
     */
    public RendezVousPropagateMessage(Element root) {
        this();

        if (!XMLElement.class.isInstance(root)) {
            throw new IllegalArgumentException(getClass().getName() + " only supports XLMElement");
        }

        XMLElement doc = (XMLElement) root;

        String doctype = doc.getName();

        String typedoctype = "";
        Attribute itsType = doc.getAttribute("type");

        if (null != itsType) {
            typedoctype = itsType.getValue();
        }

        if (!doctype.equals(MSG_NAME) && !MSG_NAME.equals(typedoctype)) {
            throw new IllegalArgumentException("Could not construct : " + getClass().getName() + "from doc containing a " + doc.getName());
        }

        Enumeration elements = doc.getChildren();

        while (elements.hasMoreElements()) {
            XMLElement elem = (XMLElement) elements.nextElement();
            if (!handleElement(elem)) {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("Unhandled Element: " + elem.toString());
                }
            }
        }

        // Sanity Check!!!
        if (Integer.MIN_VALUE == getTTL()) {
            throw new IllegalArgumentException("TTL value not specified");
        }

        if (null == destSName) {
            throw new IllegalArgumentException("Destination service name uninitialized");
        }

        if (null == msgId) {
            throw new IllegalArgumentException("Message id uninitialized");
        }
    }

    /**
     *  Adds a location to the Visited Set
     *
     *@param  location  which was visited.
     */
    public void addVisited(URI location) {
        visited.add(location);
    }

    /**
     *  Gets the destSName attribute of the RendezVousPropagateMessage object
     *
     *@return    The destSName value
     */
    public String getDestSName() {
        return destSName;
    }

    /**
     *  Gets the destSParam attribute of the RendezVousPropagateMessage object
     *
     *@return    The destSParam value
     */
    public String getDestSParam() {
        return destSParam;
    }

    /**
     *  Gets the document attribute of the RendezVousPropagateMessage object
     *
     *@param  encodeAs  Description of the Parameter
     *@return           The document value
     */
    public Document getDocument(MimeMediaType encodeAs) {

        // Sanity Check!!!

        if (getTTL() <= 0) {
            throw new IllegalStateException("TTL value < 1");
        }

        if (null == destSName) {
            throw new IllegalStateException("Destination service name uninitialized");
        }

        if (null == msgId) {
            throw new IllegalStateException("Message id uninitialized");
        }

        if (visited.isEmpty()) {
            throw new IllegalStateException("Message has not visited local peer.");
        }

        StructuredDocument doc = (StructuredDocument) StructuredDocumentFactory.newStructuredDocument(encodeAs, MSG_NAME);

        if (doc instanceof XMLDocument) {
            ((Attributable) doc).addAttribute("xmlns:jxta", "http://jxta.org");
            ((Attributable) doc).addAttribute("xml:space", "preserve");
        }

        Element e = null;

        e = doc.createElement(MsgIdTag, msgId.toString());
        doc.appendChild(e);

        e = doc.createElement(DestSNameTag, destSName);
        doc.appendChild(e);

        if (null != destSParam) {
            e = doc.createElement(DestSParamTag, destSParam);
            doc.appendChild(e);
        }

        e = doc.createElement(TTLTag, Integer.toString(TTL));
        doc.appendChild(e);

        Iterator eachVisited = visited.iterator();

        while (eachVisited.hasNext()) {
            e = doc.createElement(PathTag, eachVisited.next().toString());
            doc.appendChild(e);
        }

        return doc;
    }

    /**
     *  Gets the msgId attribute of the RendezVousPropagateMessage object
     *
     *@return    The msgId value
     */
    public UUID getMsgId() {
        return msgId;
    }

    /**
     *  Returns the path which this message has travelled.
     *
     *@return    the path this message travelled.
     */
    public URI[] getPath() {
        return (URI[]) visited.toArray(new URI[visited.size()]);
    }

    /**
     *  Gets the TTL attribute of the RendezVousPropagateMessage object
     *
     *@return    The TTL value
     */
    public int getTTL() {
        return TTL;
    }

    /**
     *  {@inheritDoc}
     *
     *@param  raw  Description of the Parameter
     *@return      Description of the Return Value
     */
    protected boolean handleElement(Element raw) {

        XMLElement elem = (XMLElement) raw;

        if (elem.getName().equals(MsgIdTag)) {
            try {
                msgId = new UUID(elem.getTextValue().trim());
            } catch (IllegalArgumentException iae) {
                // old message id format
                try {
                    msgId = UUIDFactory.newHashUUID(Long.parseLong(elem.getTextValue().trim()), 0);
                } catch (NumberFormatException notanumber) {
                    msgId = UUIDFactory.newHashUUID(elem.getTextValue().trim().hashCode(), 0);
                }
            }
            return true;
        }

        if (elem.getName().equals(DestSNameTag)) {
            destSName = elem.getTextValue().trim();
            return true;
        }

        if (elem.getName().equals(DestSParamTag)) {
            destSParam = elem.getTextValue().trim();
            return true;
        }

        if (elem.getName().equals(TTLTag)) {
            TTL = Integer.parseInt(elem.getTextValue().trim());
            return true;
        }

        if (elem.getName().equals(PathTag)) {
            addVisited(URI.create(elem.getTextValue().trim()));
            return true;
        }

        return false;
    }

    /**
     *  Returns true if the specified location is in the visited Set.
     *
     *@param  location  The location to check
     *@return           true if specified location has been visited.
     */
    public boolean isVisited(URI location) {
        return visited.contains(location);
    }

    /**
     *  Sets the destSName attribute of the RendezVousPropagateMessage object
     *
     *@param  sName  The new destSName value
     */
    public void setDestSName(String sName) {
        this.destSName = sName;
    }

    /**
     *  Sets the destSParam attribute of the RendezVousPropagateMessage object
     *
     *@param  sParam  The new destSParam value
     */
    public void setDestSParam(String sParam) {
        this.destSParam = sParam;
    }

    /**
     *  Sets the msgId attribute of the RendezVousPropagateMessage object
     *
     *@param  id  The new msgId value
     */
    public void setMsgId(UUID id) {
        msgId = id;
    }

    /**
     *  Sets the TTL attribute of the RendezVousPropagateMessage object
     *
     *@param  t  The new TTL value
     */
    public void setTTL(int ttl) {
        TTL = ttl;
    }
}

