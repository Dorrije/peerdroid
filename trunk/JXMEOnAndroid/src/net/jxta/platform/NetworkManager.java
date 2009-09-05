/*
 * Copyright (c) 2006-2007 Sun Microsystems, Inc.  All rights reserved.
 *
 *  The Sun Project JXTA(TM) Software License
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include the following acknowledgment: "This product includes software
 *     developed by Sun Microsystems, Inc. for JXTA(TM) technology."
 *     Alternately, this acknowledgment may appear in the software itself, if
 *     and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *     not be used to endorse or promote products derived from this software
 *     without prior written permission. For written permission, please contact
 *     Project JXTA at http://www.jxta.org.
 *
 *  5. Products derived from this software may not be called "JXTA", nor may
 *     "JXTA" appear in their name, without prior written permission of Sun.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SUN
 *  MICROSYSTEMS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  JXTA is a registered trademark of Sun Microsystems, Inc. in the United
 *  States and other countries.
 *
 *  Please see the license information page at :
 *  <http://www.jxta.org/project/www/license.html> for instructions on use of
 *  the license in source files.
 *
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many individuals
 *  on behalf of Project JXTA. For more information on Project JXTA, please see
 *  http://www.jxta.org.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 */
package net.jxta.platform;

import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * NetworkManager provides a simplified JXTA platform configuration abstraction, and provides a JXTA platform life-cycle
 * management. The node configuration is created during construction of this object and can be obtained for fine tuning
 * or alteration.  Note that all alterations must be done prior to calling #startNetwork(), otherwise the default
 * configuration is used.  Configuration persistence is on by default and maybe overridden by call to #setEnableConfigPersistence
 * <p/>
 * NetworkManager defines six abstractions of a node configurations as follows :
 * ADHOC : A node which typically deployed in an ad-hoc network
 * EDGE : In addition to supporting ADHOC function, an Edge node can attach to a infrastructure (a Rendezvous, Relay, or both)
 * RENDEZVOUS: provides network bootstrapping services, such as discovery, pipe resolution, etc.
 * RELAY: provides message relaying services, enabling cross firewall traversal
 * PROXY: provide JXME JXTA for J2ME proxying services
 * SUPER: provide the functionality of a Rendezvous, Relay, Proxy node.
 */
public class NetworkManager implements RendezvousListener {

    /**
     * Logger
     */
    private final static transient Logger LOG = Logger.getLogger(NetworkManager.class.getName());

    private final transient URI publicSeedingRdvURI = URI.create("http://rdv.jxtahosts.net/cgi-bin/rendezvous.cgi?3");
    private final transient URI publicSeedingRelayURI = URI.create("http://rdv.jxtahosts.net/cgi-bin/relays.cgi?3");

    /*
     * Define node standard node operating modes
     */
    /**
     * A AD-HOC node
     */
    public static final int ADHOC = 1;
    /**
     * A Edge node
     */
    public static final int EDGE = 2;

    private final Object networkConnectLock = new String("rendezvous connection lock");
    private PeerGroup netPeerGroup = null;
    private volatile boolean started = false;
    private volatile boolean connected = false;
    private volatile boolean stopped = false;
    private RendezVousService rendezvous;
    private String instanceName = "NA";
    private int mode;
    private URI instanceHome;
    private PeerGroupID infrastructureID = PeerGroupID.defaultNetPeerGroupID;
    private PeerID peerID = IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID);
    private NetworkConfigurator config;
    private boolean configPersistent = true;
    private boolean useDefaultSeeds;

    /**
     * Creates NetworkManger instance with default instance home set to "$CWD"/.jxta"
     * At this point, alternate Infrastructure PeerGroupID maybe specified, as well as a PeerID. if neither are
     * specified, the default NetPeerGroupID will be used, and a new PeerID will be generated. Also note the default
     * seeding URIs are the to development. Alternate values must be specified, if desired, prior to a call to {@link #startNetwork}
     *
     * @param mode         Operating mode  the node operating Mode
     * @param instanceName Node name
     * @throws IOException if an io error occurs
     */
    public NetworkManager(int mode, String instanceName) throws IOException {
        this(mode, instanceName, new File(".jxta/").toURI());
    }

    /**
     * Creates NetworkManger instance.
     * At this point, alternate Infrastructure PeerGroupID maybe specified, as well as a PeerID. if neither are
     * specified, the default NetPeerGroupID will be used, and a new PeerID will be generated. Also note the default
     * seeding URIs are the to development. Alternate values must be specified, if desired, prior to a call to {@link #startNetwork}
     *
     * @param mode         Operating mode  the node operating mode
     * @param instanceName Node name
     * @param instanceHome instance home is a uri to the instance persistent store (aka Cache Manager store home)
     * @throws IOException if an io error occurs
     */
    public NetworkManager(int mode, String instanceName, URI instanceHome) throws IOException {
        this.instanceName = instanceName;
        this.mode = mode;
        this.instanceHome = instanceHome;
    }

    /**
     * Returns the {@link NetworkConfigurator} for additional tuning
     *
     * @return the {@link NetworkConfigurator} for additional tuning
     * @throws java.io.IOException if an io error occurs
     */
    public synchronized NetworkConfigurator getConfigurator() throws IOException {
        if (config == null) {
            configure(mode);
        }
        return config;
    }

    /**
     * Getter for property 'infrastructureID'.
     *
     * @return Value for property 'infrastructureID'.
     */
    public PeerGroupID getInfrastructureID() {
        return infrastructureID;
    }

    /**
     * Setter for property 'infrastructureID'.
     *
     * @param infrastructureID Value to set for property 'infrastructureID'.
     */
    public void setInfrastructureID(PeerGroupID infrastructureID) {
        this.infrastructureID = infrastructureID;
        if (config != null) {
            config.setInfrastructureID(infrastructureID);
        }
    }

    /**
     * Getter for property 'instanceName'.
     *
     * @return Value for property 'instanceName'.
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Setter for property 'instanceName'.
     *
     * @param instanceName Value to set for property 'instanceName'.
     */
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    /**
     * Getter for property 'instanceHome'.
     *
     * @return Value for property 'instanceHome'.
     */
    public URI getInstanceHome() {
        return instanceHome;
    }

    /**
     * Setter for property 'instanceHome'.
     *
     * @param instanceHome Value to set for property 'instanceHome'.
     */
    public void setInstanceHome(URI instanceHome) {
        this.instanceHome = instanceHome;
    }

    /**
     * Getter for property node operating 'mode'.
     *
     * @return Value for property 'mode'.
     */
    public int getMode() {
        return mode;
    }

    /**
     * Setter for property 'mode'.
     *
     * @param mode Value to set for property 'mode'.
     * @throws IOException if an io error occurs
     */
    public void setMode(int mode) throws IOException {
        this.mode = mode;
        configure(mode);
    }

    /**
     * Getter for property 'peerID'.
     *
     * @return Value for property 'peerID'.
     */
    public PeerID getPeerID() {
        return peerID;
    }

    /**
     * Setter for property 'peerID'.
     *
     * @param peerID Value to set for property 'peerID'.
     */
    public void setPeerID(PeerID peerID) {
        this.peerID = peerID;
    }

    /**
     * Getter for property 'configPersistent'.
     *
     * @return Value for property 'configPersistent'.
     */
    public boolean isConfigPersistent() {
        return configPersistent;
    }

    /**
     * Setter for property 'configPersistent'. if disabled a PlatformConfig is not persisted. It assumed that
     * the PeerID is will be set, or a new PeerID will always be generated.
     *
     * @param persisted Value to set for property 'configPersistent'.
     */
    public void setConfigPersistent(boolean persisted) {
        this.configPersistent = persisted;
    }

    private void configure(int mode) throws IOException {
        switch (mode) {
            case ADHOC:
                config = NetworkConfigurator.newAdHocConfiguration(instanceHome);
                break;
            case EDGE:
                config = NetworkConfigurator.newEdgeConfiguration(instanceHome);
                break;
            default:
                config = NetworkConfigurator.newAdHocConfiguration(instanceHome);
        }
        config.setDescription("Created by NetworkManager");
        config.setPeerID(peerID);
        config.setInfrastructureID(infrastructureID);
        config.setName(instanceName);
        if (useDefaultSeeds) {
            config.addRdvSeedingURI(publicSeedingRdvURI);
            config.addRelaySeedingURI(publicSeedingRelayURI);
        }
    }

    /**
     * Creates and starts the JXTA infrastructure peer group (aka NetPeerGroup) based on the specified mode
     * template. This class also registers a listener for rendezvous events.
     *
     * @return The Net Peer Group
     * @throws net.jxta.exception.PeerGroupException
     *                             if the group fails to initialize
     * @throws java.io.IOException if an io error occurs
     */
    public synchronized PeerGroup startNetwork() throws PeerGroupException, IOException {

        if (started) {
            return netPeerGroup;
        }

        if (config == null) {
            configure(mode);
        }
        config.save();

        // create, and Start the default jxta NetPeerGroup
        netPeerGroup = PeerGroupFactory.newNetPeerGroup();

        if (configPersistent) {
            config.save();
        }

        rendezvous = netPeerGroup.getRendezVousService();
        rendezvous.addListener(this);
        started = true;
        return netPeerGroup;
    }

    /**
     * Stops and unreferences the NetPeerGroup
     */
    public synchronized void stopNetwork() {
        if (stopped || !started) {
            return;
        }

        stopped = true;
        synchronized (networkConnectLock) {
            connected = false;
            networkConnectLock.notifyAll();
        }

        rendezvous.removeListener(this);
        netPeerGroup.stopApp();
        netPeerGroup.unref();
        netPeerGroup = null;
        // permit restart.
        started = false;

    }

    /**
     * Gets the netPeerGroup object
     *
     * @return The netPeerGroup value
     */
    public PeerGroup getNetPeerGroup() {
        return netPeerGroup;
    }

    /**
     * Blocks only, if not connected to a rendezvous, or until a connection to rendezvous node occurs.
     *
     * @param timeout timeout in milliseconds, a zero timeout of waits forever
     * @return true if connected to a rendezvous, false otherwise
     */
    public boolean waitForRendezvousConnection(long timeout) {
        if (0 == timeout) {
            timeout = Long.MAX_VALUE;
        }

        long timeoutAt = System.currentTimeMillis() + timeout;

        if (timeoutAt <= 0) {
            // handle overflow.
            timeoutAt = Long.MAX_VALUE;
        }

        while (started && !stopped && !rendezvous.isConnectedToRendezVous() && !rendezvous.isRendezVous()) {
            try {
                long waitFor = timeoutAt - System.currentTimeMillis();

                if (waitFor > 0) {
                    synchronized (networkConnectLock) {
                        networkConnectLock.wait(timeout);
                    }
                } else {
                    // all done with waiting.
                    break;
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
                break;
            }
        }

        return rendezvous.isConnectedToRendezVous() || rendezvous.isRendezVous();
    }

    /**
     * rendezvousEvent the rendezvous event
     *
     * @param event rendezvousEvent
     */
    public void rendezvousEvent(RendezvousEvent event) {
        if (event.getType() == RendezvousEvent.RDVCONNECT || event.getType() == RendezvousEvent.RDVRECONNECT
                || event.getType() == RendezvousEvent.BECAMERDV) {
            synchronized (networkConnectLock) {
                connected = true;
                networkConnectLock.notifyAll();
            }
        }
    }

    /**
     * if true uses the public rendezvous seeding service
     *
     * @param useDefaultSeeds if true uses the default development seeding service
     */
    public void setUseDefaultSeeds(boolean useDefaultSeeds) {
        this.useDefaultSeeds = useDefaultSeeds;
    }

    /**
     * Returns true if useDefaultSeeds is set to true
     *
     * @return true if useDefaultSeeds is set to true
     */
    public boolean getUseDefaultSeeds() {
        return useDefaultSeeds;
    }
}
