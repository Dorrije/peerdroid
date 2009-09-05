/*
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *0
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
 * $Id: Codat.java,v 1.1 2005/05/03 06:30:09 hamada Exp $
 */

package net.jxta.codat;

import java.io.IOException;

import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.document.Document;

/**
 * Codats are container objects that can hold both data or code and are
 * associated with a JXTA ID.  The Codat class is offered as a standard way for
 * applications and services to exchange any kind of contents via a common API and
 * associate a unique JXTA id to these contents.
 *
 * <p>Codats are containers objects that are used to hold any kinds of
 * objects or data. A codat can represent a file, a class file, the saved
 * state of an application, a loadable C library. Codats are handled
 * transparently by the JXTA platform, and are used as placeholders for
 * any types of data. Codats hold Document that represent the data that
 * they hold.</p>
 *
 * <p>Codats are published in peer groups. A Codat can belong to only one peer
 * group. Multiple copies of a codat can be made to be published in multiple
 * peer groups.</p>
 *
 * <p>Codats are uniquely identified via a unique CodatID. This Id
 * is guaranteed to be unique within the JXTA world.</p>
 *
 * <p>The core manipulates two main types of codats:</p>
 *      <ul type-disc>
 *      <li><b>Codat</b> - hold data or code</li>
 *      <li><b>Metadata</b> - hold information about another Codat</li>
 *      </ul>
 *
 * <p>The JXTA platform defines Codat as the unit of information shared and
 * exchanged within a JXTA group.  All instances of Codats reside within a
 * peer group. The PeerGroup content caching service provides storage and retrieval
 * methods for codats using codatId as index.</p>
 *
 * @see     net.jxta.codat.CodatID
 * @see     net.jxta.document.Document
 * @see     net.jxta.document.StructuredDocument
 * @see     net.jxta.document.StructuredTextDocument
 *
 **/
public class Codat {
    
    /**
     * Id of this Codat. This is the "address" which may be used to
     * refer to this Codat. see {@link net.jxta.codat.CodatID}
     **/
    protected ID id = ID.nullID;
    
    /**
     * Codat Id of a Codat to which this Codat is related. This may be the
     * Codat Id of another codat in the same Peer Group or nullID
     **/
    protected ID metaId = ID.nullID;
    
    /**
     * A JXTA Document which contains the data held by this Codat.
     **/
    protected Document doc = null;
    
    /**
     * Makes a new Codat with a new CodatId given a PeerGroupID and a document.
     *
     * @param groupID   PeerGroupID the group to which this
     *         codat will belong.
     * @param about CodatId for which this Codat is metadata
     * @param document Document held by this codat.
     * @throws IOException if there is an error accessing the document.
     **/
    public Codat( PeerGroupID groupID, ID about, Document document) throws IOException {
        
        this.id = IDFactory.newCodatID( groupID, document.getStream() );
        
        this.metaId = about;
        this.doc = document;
    }
    
    /**
     *  Makes a new Codat instance from an existing Codat,
     *  with a given CodatID and a document.
     *
     *  @param id CodatId of the new codat
     *  @param about CodatID for which this Codat is metadata
     *  @param document Document hold by this codat
     **/
    public Codat( CodatID id, CodatID about, Document document) {
        this.id = id;
        this.metaId = about;
        this.doc = document;
    }
    
    /**
     *  Returns the CodatId associated with this Codat.
     *
     *  @return  CodateID associated with this codat
     **/
    public ID getCodatID() {
        return id;
    }
    
    /**
     *  Returns Codat id of related codat associated with this metadata Codat.
     *
     *  @return  CodateID associated with this codat
     *
     **/
    public ID getMetaID() {
        return metaId;
    }
    
    /**
     *  Returns the Document associated with this Codat.
     *
     *
     *  @return Document associated with this Codat
     **/
    public Document getDocument() {
        return doc;
    }
}
