/*
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 * reserved.
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
 *RouterQueryMsg.jRouterQueryMsg.j
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
 * $Id: RouteResponse.java,v 1.1 2005/05/03 06:48:16 hamada Exp $
 */

package net.jxta.impl.protocol;

import java.util.Enumeration;

import java.lang.reflect.UndeclaredThrowableException;

import net.jxta.document.*;
import net.jxta.protocol.RouteAdvertisement;
import net.jxta.protocol.RouteResponseMsg;

/**
 * RouteResponse message used by the Endpoint Routing protocol to
 * query for route
 */
public class RouteResponse extends RouteResponseMsg {
    
    private static final String destRouteTag = "Dst";
    private static final String srcRouteTag = "Src";
    
    /**
     * Construct a doc from strings
     *
     */
    public RouteResponse() {
    }
    
    /**
     * Construct from a StructuredDocument
     *
     */
    public RouteResponse(Element root) {
        
        if(!TextElement.class.isInstance(root))
            throw new IllegalArgumentException(getClass().getName() + " only supports TextElement");
        
        TextElement doc = (TextElement) root;
        
        String typedoctype = "";
        if(doc instanceof Attributable) {
            Attribute itsType = ((Attributable)doc).getAttribute("type");
            if(null != itsType)
                typedoctype = itsType.getValue();
        }
        
        String doctype = doc.getName();
        
        if(!doctype.equals(getAdvertisementType()) &&
        !(doctype.equals(super.getAdvertisementType()) && getAdvertisementType().equals(typedoctype)))
            throw new IllegalArgumentException("Could not construct : "
            + getClass().getName() + "from doc containing a " + doc.getName());
        
        readIt(doc);
    }
    
    private void readIt(TextElement doc) {
        
        Enumeration elements = doc.getChildren();
        
        while (elements.hasMoreElements()) {
            TextElement elem = (TextElement) elements.nextElement();
            
            if(elem.getName().equals(destRouteTag)) {
                for(Enumeration eachXpt = elem.getChildren();
                eachXpt.hasMoreElements();) {
                    TextElement aXpt = (TextElement) eachXpt.nextElement();
                    
                    RouteAdvertisement route = (RouteAdvertisement)
                    AdvertisementFactory.newAdvertisement(aXpt);
                    setDestRoute(route);
                }
                continue;
            }
            
            if (elem.getName().equals(srcRouteTag)) {
                for(Enumeration eachXpt = elem.getChildren();
                eachXpt.hasMoreElements();) {
                    TextElement aXpt = (TextElement) eachXpt.nextElement();
                    
                    RouteAdvertisement route = (RouteAdvertisement)
                    AdvertisementFactory.newAdvertisement(aXpt);
                    setSrcRoute(route);
                }
                continue;
            }
        }
    }
    
    /**
     *  return a Document represetation of this object
     */
    public Document getDocument(MimeMediaType asMimeType) {
        
        StructuredDocument adv = (StructuredTextDocument)
        StructuredDocumentFactory.newStructuredDocument(asMimeType,
        getAdvertisementType());
        
        if(adv instanceof Attributable) {
            ((Attributable)adv).addAttribute("xmlns:jxta", "http://jxta.org");
        }
        
        Element e;
        
        RouteAdvertisement route = getDestRoute();
        if (route != null) {
            e = adv.createElement(destRouteTag);
            adv.appendChild(e);
            StructuredTextDocument xptDoc = (StructuredTextDocument)
            route.getDocument(asMimeType);
            StructuredDocumentUtils.copyElements(adv, e, xptDoc);
        }
        
        route = getSrcRoute();
        if (route != null) {
            e = adv.createElement(srcRouteTag);
            adv.appendChild(e);
            StructuredTextDocument xptDoc = (StructuredTextDocument)
            route.getDocument(asMimeType);
            StructuredDocumentUtils.copyElements(adv, e, xptDoc);
        }
        return adv;
    }
    
    /**
     * return a string representaion of this RouteResponse doc
     *
     */
    public String toString() {
        
        try {
            StructuredTextDocument doc =
            (StructuredTextDocument) getDocument(MimeMediaType.XMLUTF8);
            
            return doc.toString();
        } catch(Throwable e) {
            if(e instanceof Error) {
                throw (Error) e;
            } else if(e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new UndeclaredThrowableException(e);
            }
        }
    }
}




