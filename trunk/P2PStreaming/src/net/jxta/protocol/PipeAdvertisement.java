/*
 *  $Id: PipeAdvertisement.java,v 1.3 2005/06/01 16:53:12 hamada Exp $
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
package net.jxta.protocol;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.jxta.document.Element;
import net.jxta.document.ExtendableAdvertisement;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredDocumentUtils;
import net.jxta.id.ID;

/**
 *  Describes a JXTA Pipe. A pipe is described by a pipe id and by a pipe type.
 *  A pipe can also optionally have a name and/or a description.
 *
 *@see    net.jxta.pipe.PipeService
 *@see    <a href="http://spec.jxta.org/nonav/v1.0/docbook/JXTAProtocols.html#proto-pbp"
 *      target="_blank">JXTA Protocols Specification : Pipe Binding Protocol</a>
 */
public abstract class PipeAdvertisement extends ExtendableAdvertisement {

    /**
     *  XML tag to store the PipeID
     */
    public final static String IdTag = "Id";

    /**
     *  XML tag to store the name of the Pipe
     */
    public final static String NameTag = "Name";

    /**
     *  XML tag to store the Pipe Type
     */
    public final static String TypeTag = "Type";

    /**
     *  XML tag to store the name of the Pipe
     */
    public final static String descTag = "Desc";

    /**
     *  Descriptive meta-data about this pipe.
     */
    private Element description = null;
    /**
     *  returns a map of indeables
     */
    protected Map indexMap = new HashMap();
    private String name = null;

    private ID pipeId = ID.nullID;
    private String type = null;

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
     *  Returns the identifying type of this Advertisement.
     *
     *@return    String the type of advertisement
     */
    public static String getAdvertisementType() {
        return "jxta:PipeAdvertisement";
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
            StructuredDocument newDoc = StructuredDocumentUtils.copyAsDocument(description);

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
     *  get an ID for indexing purposes. The PipeID uniquely identifies this
     *  ADV.
     *
     *@return    ID The Pipe ID itself.
     */
    public ID getID() {
        if ((null == pipeId) || pipeId.equals(ID.nullID)) {
            throw new IllegalStateException("Pipe has no assigned ID");
        }

        return pipeId;
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
     *  get the symbolic name associated with the pipe
     *
     *@return    String the name field.
     */
    public String getName() {
        return name;
    }

    /**
     *  get the pipe id
     *
     *@return    ID PipeServiceImpl id
     */
    public ID getPipeID() {
        return pipeId;
    }

    /**
     *  get the pipe type
     *
     *@return    String PipeService type
     */
    public String getType() {
        return type;
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
    }

    /**
     *  sets the description
     *
     *@param  description  the description
     */
    public void setDescription(String description) {

        if (null != description) {
            StructuredDocument newdoc = StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, "Desc", description);

            setDesc(newdoc);
        } else {
            this.description = null;
        }
    }

    /**
     *  set the symbolic name associated with the pipe
     *
     *@param  name  The new name value
     */
    public void setName(String name) {
        this.name = name; 
        if (name != null) {
            indexMap.put(NameTag, name);
        } else {
            indexMap.remove(NameTag);
        }

    }

    /**
     *  set the pipe Id
     *
     *@param  pipeId  The PipeId to be set.
     */
    public void setPipeID(ID pipeId) {
        this.pipeId = pipeId;
        if (pipeId != null) {
            indexMap.put(IdTag, pipeId.toString());
        } else {
            indexMap.remove(IdTag);
        }
    }

    /**
     *  set the pipe type
     *
     *@param  type  The type to be set.
     */
    public void setType(String type) {
        this.type = type;
    }
}

