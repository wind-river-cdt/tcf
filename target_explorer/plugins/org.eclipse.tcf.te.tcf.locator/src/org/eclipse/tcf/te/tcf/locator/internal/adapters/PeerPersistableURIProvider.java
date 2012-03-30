/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.internal.adapters;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.core.adapters.ModelNodePersistableURIProvider;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNodeProperties;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.locator.model.ModelLocationUtil;
import org.eclipse.tcf.te.tcf.locator.nodes.PeerModel;

/**
 * Persistable implementation handling peer attributes.
 */
public class PeerPersistableURIProvider extends ModelNodePersistableURIProvider {

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

	public String exportStringFrom(final Object data) {
		final AtomicReference<String> encoded = new AtomicReference<String>();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				//				try {
				//					Map<String, Object> attrs = exportFrom(data);
				//
				//					// Remove all transient attributes
				//					String[] keys = attrs.keySet().toArray(new String[attrs.keySet().size()]);
				//					for (String key : keys) {
				//						if (key.endsWith(".transient")) { //$NON-NLS-1$
				//							attrs.remove(key);
				//						}
				//					}
				//
				//					encoded.set(JSON.toJSON(attrs));
				//				}
				//				catch (IOException e) {
				//					if (Platform.inDebugMode()) {
				//						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
				//										"PeerPersistableURIProvider export failure: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
				//						Platform.getLog(CoreBundleActivator.getDefault().getBundle()).log(status);
				//					}
				//				}
			}
		};

		if (Protocol.isDispatchThread()) {
			runnable.run();
		}
		else {
			Protocol.invokeAndWait(runnable);
		}

		return encoded.get() != null ? encoded.get() : ""; //$NON-NLS-1$
	}

	public Object importFrom(final String external) throws IOException {
		Assert.isNotNull(external);

		final AtomicReference<IPeerModel> decoded = new AtomicReference<IPeerModel>();
		final AtomicReference<IOException> error = new AtomicReference<IOException>();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					Object o = JSON.parseOne(external.getBytes("UTF-8")); //$NON-NLS-1$
					// The decoded object should be a map
					if (o instanceof Map) {
						@SuppressWarnings("unchecked")
						Map<String, String> attrs = (Map<String, String>)o;

						// Get the id of the decoded attributes
						String id = attrs.get("ID"); //$NON-NLS-1$
						if (id == null) {
							throw new IOException("PeerPersistableURIProvider#import: Mandatory attribure 'ID' is missing."); //$NON-NLS-1$
						}

						// Lookup the id within the model
						IPeerModel candidate = Model.getModel().getService(ILocatorModelLookupService.class).lkupPeerModelById(id);
						if (candidate != null) {
							decoded.set(candidate);
							return;
						}

						// Not found in the model -> create a ghost object
						IPeer peer = new TransientPeer(attrs);
						IPeerModel peerModel = new PeerModel(Model.getModel(), peer);
						peerModel.setProperty(IModelNode.PROPERTY_IS_GHOST, true);

						decoded.set(peerModel);
					} else {
						throw new IOException("PeerPersistableURIProvider#import: Object not of map type."); //$NON-NLS-1$
					}
				} catch (IOException e) {
					error.set(e);
				}
			}
		};

		if (Protocol.isDispatchThread()) {
			runnable.run();
		}
		else {
			Protocol.invokeAndWait(runnable);
		}

		if (error.get() != null) {
			throw error.get();
		}

		return decoded.get() != null ? decoded.get() : ""; //$NON-NLS-1$
	}
}
