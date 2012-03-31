/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.persistence;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.persistence.AbstractPropertiesToURIPersistenceDelegate;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;

/**
 * Static peers persistence delegate implementation.
 */
public class PeerToURIPersistenceDelegate extends AbstractPropertiesToURIPersistenceDelegate {

	/**
	 * Constructor.
	 */
	public PeerToURIPersistenceDelegate() {
		super("ini"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#getPersistedClass(java.lang.Object)
	 */
	@Override
	public Class<?> getPersistedClass(Object context) {
		return IPeer.class;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.AbstractPropertiesPersistenceDelegate#toMap(java.lang.Object)
	 */
	@Override
	protected Map<String, Object> toMap(final Object context) throws IOException {
		IPeer peer = getPeer(context);
		if (peer != null) {
			return super.toMap(peer.getAttributes());
		}

		return new HashMap<String, Object>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.AbstractPropertiesPersistenceDelegate#fromMap(java.util.Map, java.lang.Object)
	 */
	@Override
	protected Object fromMap(Map<String, Object> map, Object context) throws IOException {
		Map<String,String> attrs = new HashMap<String,String>();
		for (String key : map.keySet()) {
			attrs.put(key, map.get(key).toString());
		}

		return new TransientPeer(attrs);
	}

	/**
	 * Get a peer from the given context.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @return The peer or <code>null</code>.
	 */
	protected IPeer getPeer(Object context) {
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
}
