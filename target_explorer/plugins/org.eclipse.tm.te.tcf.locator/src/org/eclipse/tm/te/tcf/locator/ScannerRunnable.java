/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.tm.tcf.core.ChannelTCP;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.te.tcf.locator.interfaces.IScanner;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
import org.eclipse.tm.te.tcf.locator.nodes.PeerModel;


/**
 * Scanner runnable to be executed for each peer to probe within the
 * TCF event dispatch thread.
 */
public class ScannerRunnable implements Runnable, IChannel.IChannelListener {
	/**
	 * The default socket connect timeout in milliseconds.
	 */
	private static final int DEFAULT_SOCKET_CONNECT_TIMEOUT = 10000;

	// Reference to the parent model scanner
	private final IScanner fParentScanner;
	// Reference to the peer model node to update
	private final IPeerModel fPeerNode;
	// Reference to the channel
	private IChannel fChannel = null;

	/**
	 * Constructor.
	 *
	 * @param scanner The parent model scanner or <code>null</code> if the runnable is constructed from outside a scanner.
	 * @param peerNode The peer model instance. Must be not <code>null</code>.
	 */
	public ScannerRunnable(IScanner scanner, IPeerModel peerNode) {
		super();

		fParentScanner = scanner;

		assert peerNode != null;
		fPeerNode = peerNode;
	}

	/**
	 * Returns the parent scanner instance.
	 *
	 * @return The parent scanner instance or <code>null</code>.
	 */
	protected final IScanner getParentScanner() {
		return fParentScanner;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (fPeerNode != null && fPeerNode.getPeer() != null) {
			// Open the channel
			fChannel = fPeerNode.getPeer().openChannel();
			// Configure the connect timeout
			if (fChannel instanceof ChannelTCP) {
				int timeout = fPeerNode.getIntProperty(IPeerModelProperties.PROP_CONNECT_TIMEOUT);
				if (timeout == -1) timeout = DEFAULT_SOCKET_CONNECT_TIMEOUT;
				((ChannelTCP)fChannel).setConnectTimeout(timeout);
			}
			// Add ourself as channel listener
			fChannel.addChannelListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IChannel.IChannelListener#onChannelOpened()
	 */
	public void onChannelOpened() {
		// Peer is reachable
		if (fChannel != null) {
			// Remove ourself as channel listener
			fChannel.removeChannelListener(this);
		}

		// Set the peer state property
		if (fPeerNode != null) {
			int counter = fPeerNode.getIntProperty(IPeerModelProperties.PROP_CHANNEL_REF_COUNTER);
			fPeerNode.setProperty(IPeerModelProperties.PROP_STATE, counter > 0 ? IPeerModelProperties.STATE_CONNECTED : IPeerModelProperties.STATE_REACHABLE);
			fPeerNode.setProperty(IPeerModelProperties.PROP_LAST_SCANNER_ERROR, null);
		}

		if (fChannel != null && fChannel.getState() == IChannel.STATE_OPEN) {
			// Get the parent model from the model mode
			final ILocatorModel model = (ILocatorModel)fPeerNode.getAdapter(ILocatorModel.class);
			if (model != null) {
				// Get the local service
				Collection<String> localServices = new ArrayList<String>(fChannel.getLocalServices());
				// Get the remote services
				Collection<String> remoteServices = new ArrayList<String>(fChannel.getRemoteServices());

				// Get the update service
				ILocatorModelUpdateService updateService = model.getService(ILocatorModelUpdateService.class);
				if (updateService != null) {
					// Update the services nodes
					updateService.updatePeerServices(fPeerNode, localServices, remoteServices);
				}

				// Use the open channel to ask the remote peer what other
				// peers it knows
				ILocator locator = fChannel.getRemoteService(ILocator.class);
				if (locator != null) {
					final Map<String, IPeer> peers = locator.getPeers();
					if (peers != null && !peers.isEmpty()) {
						// Execute asynchronously within the TCF dispatch thread
						Protocol.invokeLater(new Runnable() {
							public void run() {
								for (String peerId : peers.keySet()) {
									// Try to find an existing peer node first
									IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(peerId);
									if (peerNode == null) peerNode = new PeerModel(model, peers.get(peerId));
									// Add the peer node to model
									model.getService(ILocatorModelUpdateService.class).add(peerNode);
									// And schedule for immediate status update
									Runnable runnable = new ScannerRunnable(getParentScanner(), peerNode);
									Protocol.invokeLater(runnable);
								}
							}
						});
					}
				}
			}

			// And close the channel
			fChannel.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IChannel.IChannelListener#onChannelClosed(java.lang.Throwable)
	 */
	public void onChannelClosed(Throwable error) {
		// Peer is not reachable

		if (fChannel != null) {
			// Remove ourself as channel listener
			fChannel.removeChannelListener(this);
		}

		// Set the peer state property, if the scanner the runnable
		// has been scheduled from is still active.
		if (fPeerNode != null && (fParentScanner == null || fParentScanner != null && !fParentScanner.isTerminated())) {
			fPeerNode.setProperty(IPeerModelProperties.PROP_CHANNEL_REF_COUNTER, null);
			fPeerNode.setProperty(IPeerModelProperties.PROP_STATE,
			                  error instanceof SocketTimeoutException ? IPeerModelProperties.STATE_NOT_REACHABLE : IPeerModelProperties.STATE_ERROR);
			fPeerNode.setProperty(IPeerModelProperties.PROP_LAST_SCANNER_ERROR, error instanceof SocketTimeoutException ? null : error);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IChannel.IChannelListener#congestionLevel(int)
	 */
	public void congestionLevel(int level) {
	}

}