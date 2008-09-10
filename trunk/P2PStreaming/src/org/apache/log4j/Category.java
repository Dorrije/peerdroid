/*
 *  Copyright (c) 2005 Sun Microsystems, Inc.  All rights reserved.
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

package org.apache.log4j;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class Category {
    protected String name;
    protected volatile Level level;
    protected volatile Category parent;
    protected Category() {}
    public void assertLog(boolean assertion, String msg) {}
    public void debug(Object message) {}
    public void debug(Object message, Throwable t) {}
    public void debug(Object messagePattern, Object arg) {}
    public void debug(String messagePattern, Object arg1, Object arg2) {}
    public boolean isErrorEnabled() {
        return false;
    }
    public void error(Object message) {}
    public void error(Object message, Throwable t) {}
    public void error(Object messagePattern, Object arg) {}
    public void error(String messagePattern, Object arg1, Object arg2) {}
    public static Logger exists(String name) {
        return Logger.getLogger("stub");
    }
    public void fatal(Object message) {}
    public void fatal(Object messagePattern, Object arg) {}
    public void fatal(Object message, Throwable t) {}
    protected void forcedLog() {}
    public boolean getAdditivity() {
        return false;
    }
    public Enumeration getAllAppenders() {
        return Collections.enumeration(Collections.EMPTY_LIST);
    }
    public Level getEffectiveLevel() {
        return null; // If reached will cause an NullPointerException.
    }

    public static Enumeration getCurrentCategories() {
        return Collections.enumeration(Collections.EMPTY_LIST);
    }
    public final String getName() {
        return "stub";
    }
    public final Category getParent() {
        return this;
    }
    public final Level getLevel() {
        return this.level;
    }
    public ResourceBundle getResourceBundle() {
        return null;
    }

    protected String getResourceBundleString(String key) {
        return null;
    }
    public void info(Object message) {}
    public void info(Object messagePattern, Object arg) {}
    public void info(String messagePattern, Object arg1, Object arg2) {}
    public void info(Object message, Throwable t) {}
    public boolean isDebugEnabled() {
        return false;
    }

    public boolean isTraceEnabled() {
        return false;
    }
    public boolean isEnabledFor(Level level) {
        return false;
    }
    public boolean isInfoEnabled() {
        return false;
    }
    public void l7dlog(Level level, String key, Throwable t) {}
    public void l7dlog(Level level, String key, Object[] params, Throwable t) {}

    /**
     * This generic form is intended to be used by wrappers.
     */
    public void log(Level level, Object message, Throwable t) {}
    public void log(Level level, Object message) {}

    public void log(String callerFQCN, Level level, Object message, Throwable t) {}
    public void removeAllAppenders() {}
    public void removeAppender(String name) {}
    public void setAdditivity(boolean additive) {}
    public void setLevel(Level level) {}
    public boolean isWarnEnabled() {
        return false;
    }
    public void warn(Object message) {}
    public void warn(Object message, Throwable t) {}
    public void warn(Object messagePattern, Object arg) {}
    public void warn(String messagePattern, Object arg1, Object arg2) {}
}
