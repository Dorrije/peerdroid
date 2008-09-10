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
 *  $Id: ModuleClassAdvertisement.java,v 1.2 2005/05/24 02:39:57 hamada Exp $
 */
package net.jxta.protocol;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.jxta.document.ExtendableAdvertisement;
import net.jxta.id.ID;
import net.jxta.platform.ModuleClassID;

/**
 *  A ModuleClassAdvertisement describes a module class. Its main purpose is to
 *  formally document the existence of a module class.
 *
 *@see    net.jxta.platform.ModuleClassID
 */
public abstract class ModuleClassAdvertisement extends ExtendableAdvertisement
         implements Cloneable {
    public static final String nameTag = "Name";
    public static final String idTag = "MCID";
    public static final String descTag = "Desc";
    protected static final String [] fields = {nameTag, idTag};
    private String description = null;
    private ModuleClassID id = null;
    protected Map indexMap = new HashMap();

    private String name = null;

    /**
     *  Clone this ModuleClassAdvertisement
     *
     *@return    Object an object of class ModuleClassAdvertisement that is a
     *      deep-enough copy of this one.
     */
    public Object clone() {

        // All members are either immutable or never modified nor allowed to
        // be modified: all accessors return clones.
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
        return "jxta:MCA";
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
     *  returns the keywords/description associated with this class
     *
     *@return    String keywords/description associated with the class
     */

    public String getDescription() {
        return description;
    }

    /**
     *  returns a unique ID for that advertisement (for indexing purposes). The
     *  classID uniquely identifies this adv.
     *
     *@return    ID the class ID as a basic ID.
     */
    public ID getID() {
        return id;
    }


    /**
     *  returns the id of the class
     *
     *@return    ModuleClassID the class id
     */

    public ModuleClassID getModuleClassID() {
        return id;
    }

    /**
     *  returns the name of the class
     *
     *@return    String name of the class
     */

    public String getName() {
        return name;
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
     *  sets the id of the class
     *
     *@param  id  The id of the class
     */

    public void setModuleClassID(ModuleClassID id) {
        this.id = id;
        if (id != null) {
            indexMap.put(idTag, id.toString());
        } else {
            indexMap.remove(idTag);
        }
    }

    /**
     *  sets the name of the class
     *
     *@param  name  name of the class to be set
     */

    public void setName(String name) {
        this.name = name;
        if (name != null) {
            indexMap.put(nameTag, name);
        }

    }
    /**
     *  {@inheritDoc}
     */
    public final Map getIndexMap() {
        return  Collections.unmodifiableMap(indexMap);
    }
}

