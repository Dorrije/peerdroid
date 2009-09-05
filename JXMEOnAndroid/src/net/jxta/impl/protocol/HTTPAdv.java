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
 *  $Id: HTTPAdv.java,v 1.3 2005/06/01 16:53:13 hamada Exp $
 */
package net.jxta.impl.protocol;

import java.util.Enumeration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.jxta.document.Advertisement;
import net.jxta.document.Attributable;
import net.jxta.document.Attribute;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.XMLElement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.protocol.TransportAdvertisement;

/**
 *  Configuration parameters for HttpServelet Message Transport.
 */
public class HTTPAdv extends TransportAdvertisement {

    private final static String CONFIGMODES[] = {"auto", "manual"};
    private final static String ClientOffTag = "ClientOff";
    private final static String ConfModeTag = "ConfigMode";
    private final static String FlagsTag = "Flags";
    private final static String INDEXFIELDS[] = {
    /*
     *  none
     */
            };
    private final static String IntfAddrTag = "InterfaceAddress";

    /**
     *  Log4J Logger
     */
    private final static Logger LOG = Logger.getLogger(HTTPAdv.class.getName());
    private final static String PortTag = "Port";

    private final static String ProtocolTag = "Protocol";
    private final static String ProxyOffTag = "ProxyOff";
    private final static String ProxyTag = "Proxy";
    private final static String PublicAddressOnlyAttr = "PublicAddressOnly";
    private final static String ServerOffTag = "ServerOff";
    private final static String ServerTag = "Server";
    private boolean clientEnabled = true;
    // What IP to bind to locally

    private String configMode = CONFIGMODES[0];
    // The real port a server listens to

    private String interfaceAddress = null;
    private int listenPort = -1;

    private String proxy = null;

    // These are for configuration; They get saved in the document only if they are
    // off and the correspondig item has a non-null value. So that the value is not lost.
    // When HttpTransport is done initializing, the unused values are set to null, and thus
    // pruned from the published adv.

    private boolean proxyEnabled = true;
    private boolean publicAddressOnly = false;
    private String server = null;
    private boolean serverEnabled = true;

    /**
     *  Constructor for new advertisements. Use instantiator
     */
    private HTTPAdv() { }

    /**
     *  Constructor for existing advertisements. Use instantiator
     *
     *@param  root  Description of the Parameter
     */
    private HTTPAdv(Element root) {
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

        if (!doctype.equals(getAdvertisementType()) && !getAdvertisementType().equals(typedoctype)) {
            throw new IllegalArgumentException("Could not construct : " + getClass().getName() + "from doc containing a " + doc.getName());
        }

        Attribute attr = doc.getAttribute(FlagsTag);

        if (attr != null) {
            String options = attr.getValue();

            publicAddressOnly = (options.indexOf(PublicAddressOnlyAttr) != -1);
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

        // For consistency we force the flags to "disabled" for items we do not
        // have data for. However, the flags truely matter only when there is
        // data.
        if (proxy == null) {
            proxyEnabled = false;
        }

        if (!Arrays.asList(CONFIGMODES).contains(configMode)) {
            throw new IllegalArgumentException("Unsupported configuration mode.");
        }

        // XXX 20050118 bondolo Some versions apparently don't initialize this field. Eventually make it required.
        if (null == getProtocol()) {
            setProtocol("http");
        }
    }

    /**
     *  {@inheritDoc}
     *
     *@return    The advertisementType value
     */
    public static String getAdvertisementType() {
        return "jxta:HTTPTransportAdvertisement";
    }

    /**
     *  returns the config mode. That is, how the user prefers to configure the
     *  interface address: "auto", "manual"
     *
     *@return    string config mode
     */
    public String getConfigMode() {
        return configMode;
    }

    /**
     *  {@inheritDoc} <p/>
     *
     *  <emphasis>NB</emphasis> : we do not try to enforce dependency rules such
     *  as Proxy only when router, because we want to convey the complete
     *  configuration, even items corresponding to not currently enabled
     *  features. HttpTransport will gracefully disregard items that have no use
     *  in the current context.
     *
     *@param  encodeAs  Description of the Parameter
     *@return           The document value
     */
    public Document getDocument(MimeMediaType encodeAs) {
        if (listenPort < 1) {
            throw new IllegalStateException("Illegal Listen Port Value");
        }

        // XXX 20050118 bondolo Some versions apparently don't initialize this field. Eventually make it required.
        if (null == getProtocol()) {
            setProtocol("http");
        }

        StructuredDocument adv = (StructuredDocument) super.getDocument(encodeAs);

        if (adv instanceof Attributable) {
            // Only one flag for now. Easy.
            if (publicAddressOnly) {
                ((Attributable) adv).addAttribute(FlagsTag, PublicAddressOnlyAttr);
            }
        }

        Element e1 = adv.createElement(ProtocolTag, getProtocol());

        adv.appendChild(e1);

        Element e2 = adv.createElement(IntfAddrTag, getInterfaceAddress());

        adv.appendChild(e2);

        Element e3 = adv.createElement(ConfModeTag, getConfigMode());

        adv.appendChild(e3);

        Element e4 = adv.createElement(PortTag, Integer.toString(getPort()));

        adv.appendChild(e4);

        Element ext;

        if (proxy != null) {
            ext = adv.createElement(ProxyTag, proxy);
            adv.appendChild(ext);
        }

        // If disabled, say it; otherwise it is assumed on. In published
        // advs, we only keep data for items that are ON, so we do not
        // have to clutter them with the flag.
        if (!proxyEnabled) {
            ext = adv.createElement(ProxyOffTag);
            adv.appendChild(ext);
        }

        if (server != null) {
            ext = adv.createElement(ServerTag, server);
            adv.appendChild(ext);
        }

        // If disabled, say it; otherwise it is assumed on. In published
        // advs, we only keep data for items that are ON, so we do not
        // have to clutter them with the flag.
        if (!serverEnabled) {
            ext = adv.createElement(ServerOffTag);
            adv.appendChild(ext);
        }

        // If disabled, say it; otherwise it is assumed on. In published
        // advs, we only keep data for items that are ON, so we do not
        // have to clutter them with the flag.
        if (!clientEnabled) {
            ext = adv.createElement(ClientOffTag);
            adv.appendChild(ext);
        }

        return adv;
    }

    /**
     *  {@inheritDoc}
     *
     *@return    The indexFields value
     */
    public final String[] getIndexFields() {
        return INDEXFIELDS;
    }

    /**
     *  {@inheritDoc}
     *
     *@return    The indexMap value
     */
    public final Map getIndexMap() {
        return Collections.unmodifiableMap(Collections.EMPTY_MAP);
    }

    /**
     *  Returns the interfaceAddr. That is, the ip of the IF to which to bind
     *  locally created sockets.
     *
     *@return    string The address.
     */

    public String getInterfaceAddress() {
        return interfaceAddress;
    }

    /**
     *  Returns the port number to which server sockets are locally bound.
     *
     *@return    String the port
     */

    public int getPort() {
        return listenPort;
    }

    /**
     *  Gets the proxy attribute of the HTTPAdv object
     *
     *@return    The proxy value
     */
    public String getProxy() {
        return proxy;
    }

    /**
     *  Gets the publicAddressOnly attribute of the HTTPAdv object
     *
     *@return    The publicAddressOnly value
     */
    public boolean getPublicAddressOnly() {
        return publicAddressOnly;
    }

    /**
     *  Gets the server attribute of the HTTPAdv object
     *
     *@return    The server value
     */
    public String getServer() {
        return server;
    }

    /**
     *  {@inheritDoc}
     *
     *@param  raw  Description of the Parameter
     *@return      Description of the Return Value
     */
    protected boolean handleElement(Element raw) {

        if (super.handleElement(raw)) {
            return true;
        }

        XMLElement elem = (XMLElement) raw;

        String tag = elem.getName();

        if (tag.equals(ProxyOffTag)) {
            proxyEnabled = false;
            return true;
        }

        if (tag.equals(ServerOffTag)) {
            serverEnabled = false;
            return true;
        }

        if (tag.equals(ClientOffTag)) {
            clientEnabled = false;
            return true;
        }

        String value = elem.getTextValue();

        if (tag.equals(ProtocolTag)) {
            setProtocol(value);
            return true;
        }

        if (tag.equals(IntfAddrTag)) {
            setInterfaceAddress(value);
            return true;
        }

        if (tag.equals(ConfModeTag)) {
            setConfigMode(value);
            return true;
        }

        if (tag.equals(PortTag)) {
            setPort(Integer.parseInt(value.trim()));
            return true;
        }

        if (tag.equals(ProxyTag)) {
            proxy = value;
            return true;
        }

        if (tag.equals(ServerTag)) {
            server = value;
            return true;
        }

        return false;
    }

    /**
     *  Gets the clientEnabled attribute of the HTTPAdv object
     *
     *@return    The clientEnabled value
     */
    public boolean isClientEnabled() {
        return clientEnabled;
    }

    /**
     *  Gets the proxyEnabled attribute of the HTTPAdv object
     *
     *@return    The proxyEnabled value
     */
    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    /**
     *  Gets the serverEnabled attribute of the HTTPAdv object
     *
     *@return    The serverEnabled value
     */
    public boolean isServerEnabled() {
        return serverEnabled;
    }

    /**
     *  Sets the clientEnabled attribute of the HTTPAdv object
     *
     *@param  enabled  The new clientEnabled value
     */
    public void setClientEnabled(boolean enabled) {
        clientEnabled = enabled;
    }

    /**
     *  set the config mode. That is, how the user prefers to configure the
     *  interface address: "auto", "manual" This is just a pure config item. It
     *  is never in published advs. The TCP transport strips it when it
     *  initializes.
     *
     *@param  mode  Can be "auto", "manual" other settings will act as the
     *      default which is "auto".
     */
    public void setConfigMode(String mode) {
        if (!Arrays.asList(CONFIGMODES).contains(mode)) {
            throw new IllegalArgumentException("Unsupported configuration mode.");
        }

        configMode = mode;
    }

    /**
     *  Returns the interfaceAddr. That is, the ip of the IF to which to bind
     *  locally created sockets.
     *
     *@param  address  The new interfaceAddress value
     */
    public void setInterfaceAddress(String address) {
        this.interfaceAddress = address;
    }

    /**
     *  Sets the port number to which server sockets are locally bound.
     *
     *@param  newPort  The new port value
     */

    public void setPort(int newPort) {
        listenPort = newPort;
    }

    // If one of proxy, server, or router is cleared, the corresponding
    // enabled flag should be false (the opposite is not true).

    /**
     *  Sets the proxy attribute of the HTTPAdv object
     *
     *@param  name  The new proxy value
     */
    public void setProxy(String name) {
        proxy = name;
        if (name == null) {
            proxyEnabled = false;
        }
    }

    /**
     *  Sets the proxyEnabled attribute of the HTTPAdv object
     *
     *@param  enabled  The new proxyEnabled value
     */
    public void setProxyEnabled(boolean enabled) {
        proxyEnabled = enabled;
    }

    /**
     *  Sets the publicAddressOnly attribute of the HTTPAdv object
     *
     *@param  only  The new publicAddressOnly value
     */
    public void setPublicAddressOnly(boolean only) {
        publicAddressOnly = only;
    }

    /**
     *  Sets the server attribute of the HTTPAdv object
     *
     *@param  name  The new server value
     */
    public void setServer(String name) {
        server = name;
    }

    /**
     *  Sets the serverEnabled attribute of the HTTPAdv object
     *
     *@param  enabled  The new serverEnabled value
     */
    public void setServerEnabled(boolean enabled) {
        serverEnabled = enabled;
    }

    /**
     *  Our instantiator.
     */
    public static class Instantiator implements AdvertisementFactory.Instantiator {

        /**
         *  {@inheritDoc}
         *
         *@return    The advertisementType value
         */
        public String getAdvertisementType() {
            return HTTPAdv.getAdvertisementType();
        }

        /**
         *  {@inheritDoc}
         *
         *@return    Description of the Return Value
         */
        public Advertisement newInstance() {
            return new HTTPAdv();
        }

        /**
         *  {@inheritDoc}
         *
         *@param  root  Description of the Parameter
         *@return       Description of the Return Value
         */
        public Advertisement newInstance(Element root) {
            return new HTTPAdv(root);
        }
    }
}

