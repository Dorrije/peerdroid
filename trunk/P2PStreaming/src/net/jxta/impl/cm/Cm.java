/*
 *  $Id: Cm.java,v 1.8 2006/03/13 17:25:15 hamada Exp $
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
 *  $Id: Cm.java,v 1.8 2006/03/13 17:25:15 hamada Exp $
 */
package net.jxta.impl.cm;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import net.jxta.id.ID;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredTextDocument;
import net.jxta.impl.util.JxtaHash;
import net.jxta.impl.util.TimeUtils;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.SrdiMessage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class implements a limited document caching mechanism
 * intended to provide cache for services that have a need for cache
 * to search and exchange jxta documents.
 *
 * Only Core Services are intended to use this mechanism.
 */
public final class Cm {

    /**
     * Log4J Logger
     */
    private final static Logger LOG = Logger.getLogger(Cm.class.getName());
    /**
     *  adv types
     */
    private static final String[] DIRNAME = {"Peers", "Groups", "Adv", "Raw"};
    private boolean stop = false;
    private boolean trackDeltas = false;
    private Map deltaMap = new HashMap(3);
    private Map advMaps = new HashMap(4);
    private Map indexMap = new HashMap(4);
    private Map deltas  = new HashMap(4);
    private transient int cacheSize = 50;

    /**
     * Constructor for cm
     *
     * @param  trackDeltas     when true deltas are tracked 
     */
    public Cm(boolean trackDeltas) {

        this.trackDeltas = trackDeltas;
        createCaches();
    }

    private void createCaches() {
        for (int i=0; i<DIRNAME.length; i++) {
            if (advMaps.get(DIRNAME[i]) == null) {
                LRUCache lru = new LRUCache(cacheSize);
                advMaps.put(DIRNAME[i], lru);
            }
        }
    }

    private static String getDirName(Advertisement adv) {
        if (adv instanceof PeerAdvertisement) {
            return DIRNAME[DiscoveryService.PEER];
        } else if (adv instanceof PeerGroupAdvertisement) {
            return DIRNAME[DiscoveryService.GROUP];
        }
        return DIRNAME[DiscoveryService.ADV];
    }

    /**
     * Generates a random file name using doc hashcode
     *
     * @param  doc  to hash to generate a unique name
     * @return      String a random file name
     */
    public static String createTmpName(StructuredDocument doc) {
        try {
            StringWriter out = new StringWriter();
            ((StructuredTextDocument) doc).sendToWriter(out);
            JxtaHash digester = new JxtaHash(out.toString());
            BigInteger hash = digester.getDigestInteger();
            if (hash.compareTo(BigInteger.ZERO) < 0) {
                hash = hash.negate();
            }
            String strHash = "cm" + hash.toString(16);
            out.close();
            return strHash;
        } catch (IOException ex) {
            if (LOG.isEnabledFor(Level.WARN)) {
                LOG.warn("Exception creating tmp name: ", ex);
            }
        }
        return null;
    }

    /**
     * Gets the list of all the files into the given folder
     *
     * @param  dn  contains the name of the folder
     *
     * @return Vector Strings containing the name of the
     * files
     */
    public Vector getRecords(String dn,
                             int threshold,
                             Vector expirations,
                             boolean bytes) {

        LRUCache cache = (LRUCache) advMaps.get(dn);
        Vector result = new Vector();
        if (cache!= null) {
            Iterator it = cache.iterator(threshold);
            while (it.hasNext()) {
                Record record = (Record) it.next();
                if (!bytes) {
                    result.add(record.advertisement);
                } else {
                    result.add(record.advertisement.toString());
                }
                if (expirations != null) {
                    expirations.add(new Long(record.expiration));
                }
                if (result.size() >= threshold) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     *  Returns the relative time in milliseconds at which the file
     *  will expire.
     *
     * @param  dn  contains the name of the folder
     * @param  fn  contains the name of the file
     *
     * @return the absolute time in milliseconds at which this
     * document will expire. -1 is returned if the file is not
     * recognized or already expired.
     */
    public synchronized long getLifetime(String dn, String fn) {
        LRUCache cache = (LRUCache) advMaps.get(dn);
        if (cache!= null) {
            Record record = (Record) cache.get(fn);
            return TimeUtils.toRelativeTimeMillis(record.lifetime);
        }
        return -1;
    }

    /**
     *  Returns the maximum duration in milliseconds for which this
     *  document should cached by those other than the publisher. This
     *  value is either the cache lifetime or the remaining lifetime
     *  of the document, whichever is less.
     *
     * @param  dn  contains the name of the folder
     * @param  fn  contains the name of the file
     * @return     number of milliseconds until the file expires or -1 if the
     * file is not recognized or already expired.
     */
    public synchronized long getExpirationtime(String dn, String fn) {
        LRUCache cache = (LRUCache) advMaps.get(dn);
        if (cache!= null) {
            Record record = (Record) cache.get(fn);
            return record.expiration;
        }
        return -1;
    }

    /**
     * Remove a file
     *
     * @param  dn            directory name
     * @param  fn            file name
     * @throws  IOException  if an I/O error occurs
     */
    public synchronized void remove(String dn, String fn)
    throws IOException {
        LRUCache cache = (LRUCache) advMaps.get(dn);
        if (cache!= null) {
            Record record = (Record) cache.get(fn);
            if (fn != null && record != null) {
                cache.remove(fn);
                addDelta(dn, record.advertisement.getIndexMap(), 0);
            }
        }

        //FIXME need to also remove memory indexes, however they will eventually
        //be expungend since they're stored in an LRU cache
    }

    /**
     * Stores a StructuredDocument in specified dir, and file name
     *
     * @param  dn               directory name
     * @param  fn               file name
     * @param  adv              Advertisement to store
     * @exception  IOException  if an I/O error occurs
     */
    public void save(String dn, String fn, Advertisement adv) throws IOException {
        save(dn, fn, adv, DiscoveryService.INFINITE_LIFETIME, DiscoveryService.NO_EXPIRATION);
    }

    /**
     * Stores a StructuredDocument in specified dir, and file name, and
     * associated doc timeouts
     *
     * @param  dn               directory name
     * @param  fn               file name
     * @param  adv              Advertisement to save
     * @param  expiration    document expiration time in ms
     * @param  lifetime         document lifetime in ms
     * @exception  IOException  if an I/O error occurs
     */
    public synchronized void save(String dn, String fn, Advertisement adv, long lifetime, long expiration)
    throws IOException {
        if (expiration < 0 || lifetime < 0) {
            throw new IllegalArgumentException("cannot store an advertisement with negative expiry");
        }
        if (adv == null) {
            throw new IllegalArgumentException("advertisement can be null");
        }
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Saving :"+fn);
        }

        long absoluteLifetime = TimeUtils.toAbsoluteTimeMillis(lifetime);
        index(adv.getIndexMap(), dn, fn, lifetime);
        Record record = new Record(adv, absoluteLifetime, expiration);
        LRUCache cache = (LRUCache) advMaps.get(dn);
        if (cache!= null) {
            cache.put(fn, record);
            index(adv.getIndexMap(), dn, fn, lifetime);
        } else {
            throw new IOException("unknown directory type");
        }
    }



    private void index(Map indexables, String dn, String fn, long lifetime) {
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Indexing :"+fn);
        }
        Iterator it = indexables.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            IndexRecord indexRecord = new IndexRecord(fn, (String) indexables.get(key), lifetime);
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Adding index for :"+key +" + "+indexables.get(key));
            }
            Map keyTable = (Map) indexMap.get(dn);
            if (keyTable != null) {
                Set set = (Set) keyTable.get(key);
                if (set != null) {
                    set.add(indexRecord);
                } else {
                    set = new HashSet();
                    set.add(indexRecord);
                    keyTable.put(key, set);
                }
            } else {
                keyTable = new HashMap();
                Set set = new HashSet();
                set.add(indexRecord);
                keyTable.put(key, set);
                indexMap.put(dn, keyTable);
            }
        }
        addDelta(dn, indexables, lifetime);
    }

    /**
     * adds a primary index 'dn' to indexables
     */
    private static Map addKey(String dn, Map map) {
        if (map == null) {
            return null;
        }
        Map tmp = new HashMap();

        if (map.size() > 0) {
            Iterator it = map.keySet().iterator();
            while (it != null && it.hasNext()) {
                String name = (String) it.next();
                tmp.put(dn + name, map.get(name));
            }
        }
        return tmp;
    }

    /**
     * Search and recovers documents that contains at least
     * a macthing pair of tag/value.
     *
     * @param  dn         contains the name of the folder on which to 
     *                    perform the search
     * @param  value      contains the value to search on.
     * @param  attribute  attribute to search on
     * @param  threshold  threshold
     * @return            Enumeration containing of all the documents names
     */
    public synchronized Vector search(String dn,
                                      String attribute,
                                      String value,
                                      int threshold,
                                      Vector expirations) {
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Searching for :"+dn+" + "+attribute+" + "+value +" + "+threshold);
        }
        Vector res = new Vector();
        LRUCache advmap = (LRUCache) advMaps.get(dn);
        if (advmap == null || advmap.size() == 0) {
            return res;
        }
        Map keyTable = (Map) indexMap.get(dn);
        if (keyTable != null) {
            Set set = (Set) keyTable.get(attribute);
            if (set != null) {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("Found a matching table for :"+attribute);
                }

                Iterator it = set.iterator();
                while (it.hasNext()) {
                    IndexRecord idx = (IndexRecord) it.next();
                    if (value != null) {
                if (LOG.isEnabledFor(Level.DEBUG)) {
                    LOG.debug("Comparing "+idx.value +" to "+value);
                }
                        if (idx.value.toUpperCase().equals(value.toUpperCase())) {
                            if (LOG.isEnabledFor(Level.DEBUG)) {
                                LOG.debug("Found a matching record :"+idx.key);
                            }
                            Record record = (Record) advmap.get(idx.key);
                            if (record != null) {
                                res.add(record.advertisement);
                                if (expirations != null) {
                                    expirations.add(new Long(record.expiration));
                                }
                            }
                        }

                    } else {
                        Record record = (Record) advmap.get(idx.key);
                        if (record != null) {
                            res.add(record.advertisement);
                            if (expirations != null) {
                                expirations.add(new Long(record.expiration));
                            }

                        }
                    }
                    if (res.size()>= threshold) {
                        break;
                    }
                }
            }
        }
        return res;
    }

    /**
     * returns all entries that are cached
     *
     * @param  dn  the relative dir name
     * @return     SrdiMessage.Entries
     */
    public synchronized Vector getEntries(String dn, boolean clearDeltas) {
        Vector res = new Vector();
        Map keyTable = (Map) indexMap.get(dn);
        if (keyTable == null) {
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug("Returning empty vector of entries");
            }
            return res;
        }
        Iterator it = keyTable.values().iterator();
        while (it.hasNext()) {
            Set set = (Set) it.next();
            Iterator sets = set.iterator();
            while (sets.hasNext()) {
                IndexRecord record = (IndexRecord) sets.next();
                SrdiMessage.Entry entry = new SrdiMessage.Entry(dn, record.key, (record.lifetime- System.currentTimeMillis()));
                res.add(entry);
            }
        }
        if (clearDeltas) {
            clearDeltas(dn);
        }
        if (LOG.isEnabledFor(Level.DEBUG)) {
            LOG.debug("Returning vector of size :"+res.size());
        }
        return res;
    }

    /**
     * returns all entries that are added since this method was last called
     *
     * @param  dn  the relative dir name
     * @return     SrdiMessage.Entries
     */
    public synchronized Vector getDeltas(String dn) {
        Vector result = new Vector();
        List deltas = (List) deltaMap.get(dn);
        if (deltas != null) {
            result.addAll(deltas);
            deltas.clear();
        }
        return result;
    }

    private synchronized void clearDeltas(String dn) {
        List deltas = (List) deltaMap.get(dn);
        if (deltas == null) {
            return;
        }
        deltas.clear();
    }

    private synchronized void addDelta(String dn, Map indexables, long exp) {

        if (trackDeltas) {
            Iterator eachIndex = indexables.entrySet().iterator();

            if (eachIndex.hasNext()) {
                List deltas = (List) deltaMap.get(dn);

                if (deltas == null) {
                    deltas = new ArrayList();
                    deltaMap.put(dn, deltas);
                }
                while (eachIndex.hasNext()) {
                    Map.Entry anEntry = (Map.Entry) eachIndex.next();
                    String attr = (String) anEntry.getKey();
                    String value = (String) anEntry.getValue();
                    SrdiMessage.Entry entry = new SrdiMessage.Entry(attr, value, exp);
                    deltas.add(entry);
                    if (LOG.isEnabledFor(Level.DEBUG)) {
                        LOG.debug("Added entry  :" + entry + " to deltas");
                    }
                }
            }
        }
    }

    public synchronized void setTrackDeltas(boolean trackDeltas) {

        this.trackDeltas = trackDeltas;
        if (!trackDeltas) {
            deltaMap.clear();
        }
    }

    /**
     * stop the cm
     */
    public synchronized void stop() {
        stop = true;
        deltas.clear();
        deltaMap.clear();
        indexMap.clear();
        advMaps.clear();
    }
    public class Record {
        public transient Advertisement advertisement;
        public transient long lifetime;
        public transient long expiration;

        public Record(Advertisement advertisement, long lifetime, long expiration) {
            this.advertisement = advertisement;
            this.lifetime = lifetime;
            this.expiration = expiration;
        }
        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            if (advertisement.getID() == null || advertisement.getID().equals(ID.nullID)) {
                return advertisement.getID().hashCode();
            } else {
                return advertisement.hashCode();
            }
        }
        public String toString() {
            return advertisement.toString()+"  "+lifetime+" "+expiration;
        }
    }
    public class IndexRecord implements Comparable {
        public transient String key;
        public transient String value;
        public transient long lifetime;
        public IndexRecord(String key, String value, long lifetime) {
            this.key = key;
            this.value = value;
            this.lifetime = lifetime;
        }
        public boolean equals(Object obj) {
            return key.equals(obj);
        }
        public int hashCode() {
            return key.hashCode();
        }
        public int compareTo(Object obj) {
            return key.compareTo((String)obj);
        }
    }
}

