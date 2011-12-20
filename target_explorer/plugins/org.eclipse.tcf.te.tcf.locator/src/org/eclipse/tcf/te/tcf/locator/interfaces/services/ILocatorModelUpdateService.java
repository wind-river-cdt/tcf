/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.interfaces.services;

import java.util.Collection;

import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;


/**
 * The service to update the properties of given locator model nodes.
 */
public interface ILocatorModelUpdateService extends ILocatorModelService {

	/**
	 * Adds the given peer to the list of know peers. A previous
	 * mapping to a peer model with the same id as the given
	 * peer model is overwritten.
	 *
	 * @param peer The peer model object. Must not be <code>null</code>.
	 */
	public void add(IPeerModel peer);

	/**
	 * Removes the given peer from the list of known peers.
	 *
	 * @param peer The peer model object. Must not be <code>null</code.
	 */
	public void remove(IPeerModel peer);

	/**
	 * Update the service nodes of the given peer node with the new set of
	 * local and/or remote services.
	 *
	 * @param peerNode The peer model instance. Must not be <code>null</code>.
	 * @param localServices The list of local service names or <code>null</code>.
	 * @param remoteServices The list of remote service names or <code>null</code>.
	 */
	public void updatePeerServices(IPeerModel peerNode, Collection<String> localServices, Collection<String> remoteServices);

	/**
	 * Adds the given child peer to the parent peer.
	 *
	 * @param parent The parent peer. Must not be <code>null</code>.
	 * @param child The child peer model object. Must not be <code>null</code>.
	 */
	public void addChild(IPeer parent, IPeerModel child);

	/**
	 * Removes the given child peer from the parent peer.
	 *
	 * @param parent The parent peer. Must not be <code>null</code>.
	 * @param child The child peer model object. Must not be <code>null</code>.
	 */
	public void removeChild(IPeer parent, IPeerModel child);
}
