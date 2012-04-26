/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
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
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.persistence.AbstractGsonMapPersistenceDelegate;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.locator.nodes.PeerModel;

/**
 * Peer to string persistence delegate implementation.
 */
public class GsonPeerPersistenceDelegate extends AbstractGsonMapPersistenceDelegate {

	/**
	 * Constructor.
	 */
	public GsonPeerPersistenceDelegate() {
		super("json"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#getPersistedClass(java.lang.Object)
	 */
	@Override
	public Class<?> getPersistedClass(Object context) {
		return IPeer.class;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.persistence.AbstractPropertiesToStringPersistenceDelegate#read(java.lang.Object, java.lang.Object, java.lang.String)
	 */
	@Override
	public Object read(final Object context, final Object container, String key) throws IOException {
		Assert.isNotNull(context);
		Assert.isNotNull(container);

		final IPeer peer = (IPeer)super.read(context, container, key);

		if (peer != null) {
			if (context instanceof IPeer || IPeer.class.equals(context)) {
				return peer;
			}
			else if (context instanceof Class && (((Class<?>)context).isAssignableFrom(IPeerModel.class))) {
				final AtomicReference<IPeerModel> model = new AtomicReference<IPeerModel>();

				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						// Get the id of the decoded attributes
						String id = peer.getID();
						if (id != null) {
							// Lookup the id within the model
							IPeerModel peerModel = Model.getModel().getService(ILocatorModelLookupService.class).lkupPeerModelById(id);
							if (peerModel == null) {
								// Not found in the model -> create a ghost object
								peerModel = new PeerModel(Model.getModel(), peer);
								peerModel.setProperty(IModelNode.PROPERTY_IS_GHOST, true);
							}

							model.set(peerModel);
						}
					}
				};

				if (Protocol.isDispatchThread()) {
					runnable.run();
				}
				else {
					Protocol.invokeAndWait(runnable);
				}

				return model.get();
			}
		}

		return null;
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
		for (Entry<String, Object> entry : map.entrySet()) {
			attrs.put(entry.getKey(), entry.getValue().toString());
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
