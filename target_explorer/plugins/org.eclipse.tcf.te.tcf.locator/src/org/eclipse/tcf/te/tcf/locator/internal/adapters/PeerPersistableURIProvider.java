/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.internal.adapters;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.core.adapters.ModelNodePersistableURIProvider;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNodeProperties;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;
import org.eclipse.tcf.te.tcf.locator.model.ModelLocationUtil;

/**
 * Persistable implementation handling peer attributes.
 */
public class PeerPersistableURIProvider extends ModelNodePersistableURIProvider {

	/**
	 * Determine the peer from the given context object.
	 *
	 * @param context The context object or <code>null</code>.
	 * @return The peer or <code>null</code>.
	 */
	private IPeer getPeer(Object context) {
		IPeer peer = null;

		if (context instanceof IPeer) {
			peer = (IPeer)context;
		}
		else if (context instanceof IPeerModel) {
			peer = ((IPeerModel)context).getPeer();
		}
		else if (context instanceof IPeerModelProvider) {
			peer = ((IPeerModelProvider)context).getPeerModel().getPeer();
		}

		return peer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableURIProvider#getURI(java.lang.Object)
	 */
	@Override
	public URI getURI(final Object context) {
		Assert.isNotNull(context);

		URI uri = null;
		final IPeer peer = getPeer(context);

		if (peer != null) {
			// Get the URI the peer model has been created from
			final AtomicReference<URI> nodeURI = new AtomicReference<URI>();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					String value = peer.getAttributes().get(IPersistableNodeProperties.PROPERTY_URI);
					if (value != null && !"".equals(value.trim())) { //$NON-NLS-1$
						nodeURI.set(URI.create(value.trim()));
					}
				}
			};
			if (Protocol.isDispatchThread()) {
				runnable.run();
			}
			else {
				Protocol.invokeAndWait(runnable);
			}

			if (nodeURI.get() != null) {
				uri = nodeURI.get();
			}

			if (uri == null) {
				String name = peer.getName();
				if (name == null) {
					name = peer.getID();
				}
				name = makeValidFileSystemName(name);
				// Get the URI from the name
				uri = getRoot().append(name).toFile().toURI();

			}
		}

		return uri;
	}

	/**
	 * Returns the root location of the peers storage.
	 *
	 * @return The root location or <code>null</code> if it cannot be determined.
	 */
	@Override
	public IPath getRoot() {
		return ModelLocationUtil.getStaticPeersRootLocation();
	}
}
