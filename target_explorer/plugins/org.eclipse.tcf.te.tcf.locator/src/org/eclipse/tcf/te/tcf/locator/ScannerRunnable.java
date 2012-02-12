/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.core.Command;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ILocator;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.locator.interfaces.IScanner;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
import org.eclipse.tcf.te.tcf.locator.nodes.PeerModel;
import org.eclipse.tcf.te.tcf.locator.nodes.PeerRedirector;


/**
 * Scanner runnable to be executed for each peer to probe within the
 * TCF event dispatch thread.
 */
public class ScannerRunnable implements Runnable, IChannel.IChannelListener {

	// Reference to the parent model scanner
	private final IScanner parentScanner;
	// Reference to the peer model node to update
	/* default */ final IPeerModel peerNode;
	// Reference to the channel
	/* default */ IChannel channel = null;
	// Mark if the used channel is a shared channel instance
	/* default */ boolean sharedChannel = false;
	// The state of the peer node at the point of time the runnable starts execution
	private int oldState = IPeerModelProperties.STATE_UNKNOWN;

	/**
	 * Constructor.
	 *
	 * @param scanner The parent model scanner or <code>null</code> if the runnable is constructed from outside a scanner.
	 * @param peerNode The peer model instance. Must not be <code>null</code>.
	 */
	public ScannerRunnable(IScanner scanner, IPeerModel peerNode) {
		super();

		parentScanner = scanner;

		Assert.isNotNull(peerNode);
		this.peerNode = peerNode;
	}

	/**
	 * Returns the parent scanner instance.
	 *
	 * @return The parent scanner instance or <code>null</code>.
	 */
	protected final IScanner getParentScanner() {
		return parentScanner;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Remember the peer node state
		oldState = peerNode.getIntProperty(IPeerModelProperties.PROP_STATE);
		// Do not open a channel to incomplete peer nodes
		if (peerNode.isComplete() && peerNode.getPeer() != null) {
			// Check if there is a shared channel available which is still in open state
			channel = Tcf.getChannelManager().getChannel(peerNode.getPeer());
			if (channel == null || channel.getState() != IChannel.STATE_OPEN) {
				sharedChannel = false;
				// Open the channel
				channel = peerNode.getPeer().openChannel();
				// Add ourself as channel listener
				channel.addChannelListener(this);
			} else {
				sharedChannel = true;
				// Shared channel is in open state -> use it
				onChannelOpened();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#onChannelOpened()
	 */
	@SuppressWarnings("unused")
    @Override
	public void onChannelOpened() {
		// Peer is reachable
		if (channel != null && !sharedChannel) {
			// Remove ourself as channel listener
			channel.removeChannelListener(this);
		}

		// Turn off change notifications temporarily
		boolean changed = peerNode.setChangeEventsEnabled(false);
		// Flag to set for refreshing the editor tab list
		boolean refreshEditorTabs = false;

		// Set the peer state property
		int counter = peerNode.getIntProperty(IPeerModelProperties.PROP_CHANNEL_REF_COUNTER);
		peerNode.setProperty(IPeerModelProperties.PROP_STATE, counter > 0 ? IPeerModelProperties.STATE_CONNECTED : IPeerModelProperties.STATE_REACHABLE);
		peerNode.setProperty(IPeerModelProperties.PROP_LAST_SCANNER_ERROR, null);
		// If the old state had been one of the error states before, and is now changed to
		// either reachable or connected, the editor tabs needs refreshment.
		refreshEditorTabs = oldState != IPeerModelProperties.STATE_CONNECTED && oldState != IPeerModelProperties.STATE_REACHABLE;

		if (channel != null && channel.getState() == IChannel.STATE_OPEN) {
			// Keep the channel open as long as the query for the remote peers is running.
			boolean keepOpen = false;

			// Get the parent model from the model mode
			final ILocatorModel model = (ILocatorModel)peerNode.getAdapter(ILocatorModel.class);
			if (model != null) {
				// Get the local service
				Collection<String> localServices = new ArrayList<String>(channel.getLocalServices());
				// Get the remote services
				Collection<String> remoteServices = new ArrayList<String>(channel.getRemoteServices());

				// Update the services
				ILocatorModelUpdateService updateService = model.getService(ILocatorModelUpdateService.class);
				updateService.updatePeerServices(peerNode, localServices, remoteServices);

				// Use the open channel to ask the remote peer what other peers it knows
				ILocator locator = channel.getRemoteService(ILocator.class);
				if (locator != null) {
					// Channel must be kept open as long as the command runs
					keepOpen = true;
					// Issue the command
		            new Command(channel, locator, "getPeers", null) { //$NON-NLS-1$
		                @Override
		                public void done(Exception error, Object[] args) {
		                    if (error == null) {
		                        assert args.length == 2;
		                        error = toError(args[0]);
		                    }
		                    // If the error is still null here, process the returned peers
		                    if (error == null && args[1] != null) {
	                        	// Get the parent peer
	                        	IPeer parentPeer = channel.getRemotePeer();
	                        	// Get the old child list
	                        	List<IPeerModel> oldChildren = new ArrayList<IPeerModel>(model.getChildren(parentPeer.getID()));

		                    	// "getPeers" returns a collection of peer attribute maps
		                        @SuppressWarnings("unchecked")
                                Collection<Map<String,String>> peerAttributesList = (Collection<Map<String,String>>)args[1];
		                        for (Map<String,String> attributes : peerAttributesList) {
		                        	// Get the peer id
		                        	String peerId = attributes.get(IPeer.ATTR_ID);
		                        	// Create a peer instance
		                        	IPeer peer = new PeerRedirector(parentPeer, attributes);
									// Try to find an existing peer node first
									IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(parentPeer.getID(), peerId);
									if (peerNode == null) {
										// Not yet known -> add it
										peerNode = new PeerModel(model, peer);
										peerNode.setParent(ScannerRunnable.this.peerNode);
										// Validate the peer node before adding
										peerNode = model.validateChildPeerNodeForAdd(peerNode);
										if (peerNode != null) {
											// Add the child peer node to model
											model.getService(ILocatorModelUpdateService.class).addChild(peerNode);
											// And schedule for immediate status update
											Runnable runnable = new ScannerRunnable(getParentScanner(), peerNode);
											Protocol.invokeLater(runnable);
										}
									} else {
										// The parent node should be set and match
										Assert.isTrue(peerNode.getParent(IPeerModel.class) != null && peerNode.getParent(IPeerModel.class).equals(ScannerRunnable.this.peerNode));
										// Peer node found, update the peer instance
										peerNode.setProperty(IPeerModelProperties.PROP_INSTANCE, peer);
										// And remove it from the old child list
										oldChildren.remove(peerNode);
									}
		                        }

		                        // Everything left in the old child list is not longer known to the remote peer
		                        // However, the child list may include manual redirected static peers. Do not
		                        // remove them here.
		                        for (IPeerModel child : oldChildren) {
		                        	String value = child.getPeer().getAttributes().get("static.transient"); //$NON-NLS-1$
		                        	if (value == null || !Boolean.parseBoolean(value.trim())) {
		                        		// Remove the child peer node from the model
		                        		model.getService(ILocatorModelUpdateService.class).removeChild(child);
		                        	}
		                        }
		                    }

		                    // Once everything is processed, close the channel
		                    if (!sharedChannel) channel.close();
		                }
		            };
				}

				// If we don't queried the DNS name of the peer, or the peer IP changed,
				// trigger a query (can run in any thread, outside TCF dispatch and UI
				// thread). This make sense only if there is an IP address to query at all.
				final String ip = channel.getRemotePeer().getAttributes().get(IPeer.ATTR_IP_HOST);
				if (ip != null && !"".equals(ip)) { //$NON-NLS-1$
					if (peerNode.getStringProperty("dns.name.transient") == null || !ip.equals(peerNode.getStringProperty("dns.lastIP.transient"))) { //$NON-NLS-1$ //$NON-NLS-2$
						// If the IP address changed, reset the "do not query again" marker
						if (!ip.equals(peerNode.getStringProperty("dns.lastIP.transient"))) { //$NON-NLS-1$
							peerNode.setProperty("dns.lastIP.transient", ip); //$NON-NLS-1$
							peerNode.setProperty("dns.skip.transient", false); //$NON-NLS-1$
						}

						if (!peerNode.getBooleanProperty("dns.skip.transient")) { //$NON-NLS-1$
							Runnable runnable = new Runnable() {
								@Override
								public void run() {
									try {
										InetAddress address = InetAddress.getByName(ip);
										final String name = address.getCanonicalHostName();
										Protocol.invokeLater(new Runnable() {
											@Override
											public void run() {
												if (name != null && !"".equals(name) && !ip.equals(name)) { //$NON-NLS-1$
													String dnsName = name.indexOf('.') != -1 ? name.substring(0, name.indexOf('.')) : name;
													if (!ip.equalsIgnoreCase(dnsName)) {
														peerNode.setProperty("dns.name.transient", dnsName.toLowerCase()); //$NON-NLS-1$
													}
												}
											}
										});
									}
									catch (UnknownHostException e) {
										Protocol.invokeLater(new Runnable() {
											@Override
                                            public void run() {
												peerNode.setProperty("dns.skip.transient", true); //$NON-NLS-1$
											}
										});
									}
								}
							};

							Thread thread = new Thread(runnable, "DNS Query Thread for " + ip); //$NON-NLS-1$
							thread.start();
						}
					}
				}
			}

			// And close the channel
			if (!sharedChannel && !keepOpen) channel.close();
		}

		// Re-enable the change events a fire a "properties" change event
		if (changed) {
			peerNode.setChangeEventsEnabled(true);
			peerNode.fireChangeEvent("properties", null, peerNode.getProperties()); //$NON-NLS-1$
		}

		if (refreshEditorTabs) {
			peerNode.fireChangeEvent("editor.refreshTab", Boolean.FALSE, Boolean.TRUE); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#onChannelClosed(java.lang.Throwable)
	 */
	@Override
	public void onChannelClosed(Throwable error) {
		// Peer is not reachable

		if (channel != null) {
			// Remove ourself as channel listener
			channel.removeChannelListener(this);
		}

		// Set the peer state property, if the scanner the runnable
		// has been scheduled from is still active.
		if (parentScanner == null || parentScanner != null && !parentScanner.isTerminated()) {
			// Turn off change notifications temporarily
			boolean changed = peerNode.setChangeEventsEnabled(false);

			peerNode.setProperty(IPeerModelProperties.PROP_CHANNEL_REF_COUNTER, null);
			boolean timeout = error instanceof SocketTimeoutException || (error instanceof ConnectException && error.getMessage() != null && error.getMessage().startsWith("Connection timed out:")); //$NON-NLS-1$
			peerNode.setProperty(IPeerModelProperties.PROP_STATE, timeout ? IPeerModelProperties.STATE_NOT_REACHABLE : IPeerModelProperties.STATE_ERROR);
			peerNode.setProperty(IPeerModelProperties.PROP_LAST_SCANNER_ERROR, error instanceof SocketTimeoutException ? null : error);

			// Clear out previously determined services
			ILocatorModel model = (ILocatorModel)peerNode.getAdapter(ILocatorModel.class);
			if (model != null) {
				ILocatorModelUpdateService updateService = model.getService(ILocatorModelUpdateService.class);
				updateService.updatePeerServices(peerNode, null, null);
			}

			// Clean out DNS name detection
			peerNode.setProperty("dns.name.transient", null); //$NON-NLS-1$
			peerNode.setProperty("dns.lastIP.transient", null); //$NON-NLS-1$
			peerNode.setProperty("dns.skip.transient", null); //$NON-NLS-1$

			// Re-enable the change events a fire a "properties" change event
			if (changed) {
				peerNode.setChangeEventsEnabled(true);
				peerNode.fireChangeEvent("properties", null, peerNode.getProperties()); //$NON-NLS-1$
			}

			// If the old state had been one of the reachable states before, and is now changed to
			// either not reachable or error, the editor tabs needs refreshment.
			if (oldState != IPeerModelProperties.STATE_NOT_REACHABLE && oldState != IPeerModelProperties.STATE_ERROR) {
				peerNode.fireChangeEvent("editor.refreshTab", Boolean.FALSE, Boolean.TRUE); //$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#congestionLevel(int)
	 */
	@Override
	public void congestionLevel(int level) {
	}

}
