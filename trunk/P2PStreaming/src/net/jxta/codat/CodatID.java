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
 * $Id: CodatID.java,v 1.1 2005/05/03 06:30:09 hamada Exp $
 */

package net.jxta.codat;

import net.jxta.id.ID;

/**
 * Codats are JXTA objects that can hold both data or code.
 *
 * <p>Codats are containers objects that are used to hold any kinds of
 * objects or data. A codat can represent a file, a class file, the saved
 * state of an application, a loadable C library. Codats are handled
 * transparently by the JXTA platform, and are used as placeholders for
 * any type of data. Codats holds a Document that represent the data that
 * they hold.</p>
 *
 * <p>Each Codat  is assigned a unique codat id that enables canonical
 * references to be made to the codat in the context of a specific peer group.
 * A CodatID is formed by the conjuction of a PeerGroupID, a randomly chosen
 * value that has a high probability of being unique, and an optional SHA1
 * cryptographic hash of the codat contents. The Id is the unique Id for this
 * Codat within the JXTA world. Some codats may not hold a document. In that
 * case the CodatId is constructed without the document hash value.
 *
 * @see     net.jxta.codat.Codat
 * @see     net.jxta.document.Document
 * @see     net.jxta.document.StructuredDocument
 *
 */

public abstract class CodatID extends ID {
    
    
    /**
     *  Constructs a new CodatID
     **/
    protected CodatID() {
    }
    
    /**
     *  Returns PeerGroupID of the Peer Group to which this Codat ID belongs.
     *
     *
     *  @return PeerGroupID of the Peer Group which this ID is part of.
     **/
    public abstract ID getPeerGroupID( );
    
    /**
     *  Returns true if this CodatID is associated with a static Codat.
     *
     *  @return boolean check if the codatId is for a codat without document
     *  (Static codat)
     *
     */
    public abstract boolean isStatic();
    
}
