/*
 *
 * $Id: StringMessageElement.java,v 1.1 2005/05/03 06:38:25 hamada Exp $
 *
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
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *    must not be used to endorse or promote products derived from this
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
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 */

package net.jxta.endpoint;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.ref.SoftReference;

import java.io.UnsupportedEncodingException;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.jxta.document.MimeMediaType;
import net.jxta.util.CountingOutputStream;
import net.jxta.util.DevNullOutputStream;

//FIXME backport optimization from the latest revision in J2SE binding
/**
 *  A Message Element using character strings for the element data.
 *
 */
public class StringMessageElement extends TextMessageElement {
    /**
     *  Log4J Logger
     */
    private final static transient Logger LOG = Logger.getLogger(StringMessageElement.class.getName());

    /**
     *  The data for this Message Element.
     */
    protected String data;

    /**
     *  Create a new Message Element from the provided String. The String will
     *  be encoded for transmission using UTF-8.
     *
     *  @param name Name of the Element. May be the empty string ("") or null if
     *  the Element is not named.
     *  @param value A String containing the contents of this element.
     *  @param sig Message digest/digital signature element. If no signature is
     *  to be specified, pass <code>null</code>.
     *
     *  @throws IllegalArgumentException if <code>value</code> is
     *  <code>null</code>.
     */
    public StringMessageElement(String name, String value, MessageElement sig) {
        super(name, null, sig);
        if(null == value) {
            throw new IllegalArgumentException("value must be non-null");
        }
        //type = MimeMediaType.TEXTUTF8;
        data = value;
    }

    /**
     *  Create a new Message Element from the provided String. The string will
     *  be encoded for transmission using specified character encoding.
     *
     *  @param name Name of the MessageElement. May be the empty string ("") or
     *  <code>null</code> if the MessageElement is not named.
     *  @param value A String containing the contents of this element.
     *  @param encoding Name of the character encoding to use. If
     *  <code>null</code> then the system default charcter encoding will be
     *  used. (Using the system default charcter encoding should be used with
     *  extreme caution).
     *  @param sig Message digest/digital signature element. If no signature is
     *  to be specified, pass <code>null</code>.
     *
     *  @throws IllegalArgumentException if <code>value</code> is
     *  <code>null</code>.
     *  @throws UnsupportedEncodingException if the requested encoding is not
     *  supported.
     */
    public StringMessageElement(String name, String value, String encoding, MessageElement sig) throws UnsupportedEncodingException {
        super(name, null, sig);

        if(null == value) {
            throw new IllegalArgumentException("value must be non-null");
        }

        // There doesn't seem to be a way to do this directly. The goal is to get
        // the canonical name of the encoding being used. Since ByteArrayInputStream
        // does not copy the data, this operation is relatively cheap if a little
        // circuitous.
        InputStreamReader getEncoding;
        if(null == encoding) {
            getEncoding = new InputStreamReader(new ByteArrayInputStream(new byte [0]));
        } else {
            getEncoding = new InputStreamReader(new ByteArrayInputStream(new byte [0]), encoding);
        }

        encoding = getEncoding.getEncoding();

        try {
            getEncoding.close();
            getEncoding = null;
        } catch(IOException ignored) {
            ;
        }

        //type = new MimeMediaType(MimeMediaType.TEXT_DEFAULTENCODING, "charset=\"" + encoding + "\"", true);

        data = value;
    }

    /**
     *  {@inheritDoc}
     */
    public boolean equals(Object target) {
        if(this == target) {
            return true;
        }

        if(target instanceof MessageElement) {
            if(!super.equals(target))
                return false;

            if(target instanceof StringMessageElement) {
                StringMessageElement likeMe = (StringMessageElement) target;

                return data.equals(likeMe.data); // same chars?
            }
            else if (target instanceof TextMessageElement) {
                // have to do a slow char by char comparison. Still better than the stream since it saves encoding.
                // XXX 20020615 bondolo@jxta.org the performance of this could be much improved.

                TextMessageElement likeMe = (TextMessageElement) target;

                try {
                    Reader myReader = getReader();
                    Reader itsReader = likeMe.getReader();

                    int mine;
                    int its;
                    do {
                        mine = myReader.read();
                        its = itsReader.read();

                        if(mine != its)
                            return false;       // content didn't match

                    }
                    while((-1 != mine) && (-1 != its));

                    return ((-1 == mine) && (-1 == its)); // end at the same time?
                }
                catch(IOException fatal) {
                    throw new IllegalStateException("MessageElements could not be compared." + fatal);
                }
            } else {
                // have to do a slow stream comparison.
                // XXX 20020615 bondolo@jxta.org the performance of this could be much improved.

                MessageElement likeMe = (MessageElement) target;

                try {
                    InputStream myStream = getStream();
                    InputStream itsStream = likeMe.getStream();

                    int mine;
                    int its;
                    do {
                        mine = myStream.read();
                        its = itsStream.read();

                        if(mine != its)
                            return false;       // content didn't match

                    }
                    while((-1 != mine) && (-1 != its));

                    return ((-1 == mine) && (-1 == its)); // end at the same time?
                }
                catch(IOException fatal) {
                    throw new IllegalStateException("MessageElements could not be compared." + fatal);
                }
            }
        }

        return false; // not a new message element
    }

    /**
     *  {@inheritDoc}
     */
    public int hashCode() {
        int result = super.hashCode() * 6037 + // a prime
                     data.hashCode();

        return result;
    }

    /**
     *  {@inheritDoc}
     */
    public String toString() {
        return data;
    }

    /**
     *  {@inheritDoc}
     */
    public synchronized byte[] getBytes(boolean copy) {
        byte [] result = null;

        if(!copy && (null != cachedGetBytes)) {
            result = (byte []) cachedGetBytes.get();

            if (null != result)
                return result;
        }

        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("creating getBytes of " + getClass().getName() + '@' + Integer.toHexString(hashCode()));
        }

        String charset = type.getParameter("charset");

        if(null == charset)
            result = data.getBytes();
        else {
            try {
                result = data.getBytes(charset);
            } catch(UnsupportedEncodingException caught) {
                if (LOG.isEnabledFor(Level.WARN)) {
                    LOG.warn("MessageElement Data could not be generated", caught);
                }
                throw new IllegalStateException("MessageElement Data could not be generated due to " + caught.getMessage());
            }
        }

        // if this is supposed to be a shared buffer then we can cache it.
        if(!copy) {
            cachedToString = new SoftReference(result);
        }

        return result;
    }

    /**
     *  {@inheritDoc}
     */
    public long getCharLength() {
        return data.length();
    }

    /**
     *  {@inheritDoc}
     */
    public char[] getChars(boolean copy) {
        // FIXME 20020822 bondolo@jxta.org This result could be cached if a copy
        // is not being requested.

        char [] result = new char [ data.length() ];

        data.getChars(0, data.length(), result, 0);

        return result;
    }

    /**
     *  {@inheritDoc}
     *
     * <p/>XXX 20020519 bondolo@jxta.org  This implementation is really horrible,
     * use getReader if you possibly can. The only alternative I see is to
     * pass the data through a piped output stream using a print writer. To ensure
     * that we don't deadlock, we would have to process chars from the string one
     * at a time.
     */
    public InputStream getStream() throws IOException {
        return new ByteArrayInputStream(getBytes(false));
    }

    /**
     *  {@inheritDoc}
     *
     *  @return InputStream of the stream containing element data.
     *  @throws IOException when there is a problem getting a reader.
     */
    public Reader getReader() throws IOException {

        return new StringReader(data);
    }

    /**
     *  {@inheritDoc}
     */
    public void sendToStream(OutputStream sendTo) throws IOException {
        OutputStreamWriter writer;
        String charset = type.getParameter("charset");

        if(null != charset) {
            writer = new OutputStreamWriter(sendTo, charset);
        } else {
            writer = new OutputStreamWriter(sendTo);
        }

        writer.write(data);
        writer.flush();
    }

    /**
     *  {@inheritDoc}
     */
    public void sendToWriter(Writer sendTo) throws IOException {
        sendTo.write(data);
    }
}
