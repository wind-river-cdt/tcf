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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable2;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNodeProperties;
import org.eclipse.tcf.te.tcf.locator.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.locator.nodes.PeerModel;

/**
 * Persistable implementation handling peer attributes.
 */
public class PeerModelPersistableAdapter implements IPersistable2 {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#getStorageID()
	 */
	@Override
	public String getStorageID() {
		return "org.eclipse.tcf.te.tcf.locator.persistence"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#getURI(java.lang.Object)
	 */
	@Override
	public URI getURI(final Object data) {
		Assert.isNotNull(data);

		URI uri = null;

		// Only peer model objects are supported
		if (data instanceof IPeerModel) {
			// Get the URI the peer model has been created from
			final AtomicReference<URI> nodeURI = new AtomicReference<URI>();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					String value = ((IPeerModel)data).getPeer().getAttributes().get(IPersistableNodeProperties.PROPERTY_URI);
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
		}

		return uri;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#getInterfaceType(java.lang.Object)
	 */
	@Override
	public String getInterfaceTypeName(Object data) {
		if (data instanceof IPeerModel) {
			return CoreBundleActivator.getUniqueIdentifier() + ":" + IPeerModel.class.getName(); //$NON-NLS-1$
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#exportFrom(java.lang.Object)
	 */
	@Override
	public Map<String, Object> exportFrom(final Object data) throws IOException {
		Assert.isNotNull(data);

		final AtomicReference<Map<String, String>> attributes = new AtomicReference<Map<String, String>>();

		// Only peer model objects are supported
		if (data instanceof IPeerModel) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					attributes.set(((IPeerModel)data).getPeer().getAttributes());
				}
			};
			if (Protocol.isDispatchThread()) {
				runnable.run();
			}
			else {
				Protocol.invokeAndWait(runnable);
			}
		}

		Map<String, Object> result = null;
		if (attributes.get() != null) {
			result = new HashMap<String, Object>();
			for (String key : attributes.get().keySet()) {
				if (!key.endsWith(".transient")) { //$NON-NLS-1$
					result.put(key, attributes.get().get(key));
				}
			}
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable#importTo(java.lang.Object, java.util.Map)
	 */
	@Override
	public void importTo(final Object data, final Map<String, Object> external) throws IOException {
		Assert.isNotNull(data);
		Assert.isNotNull(external);

		// A direct import of the attributes in a peer is not possible.
		// A new peer with the new attributes needs to be created and set to the peer model.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable2#exportStringFrom(java.lang.Object)
	 */
	@Override
	public String exportStringFrom(final Object data) {
		final AtomicReference<String> encoded = new AtomicReference<String>();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					Map<String, Object> attrs = exportFrom(data);

					// Remove all transient attributes
					String[] keys = attrs.keySet().toArray(new String[attrs.keySet().size()]);
					for (String key : keys) {
						if (key.endsWith(".transient")) { //$NON-NLS-1$
							attrs.remove(key);
						}
					}

					encoded.set(JSON.toJSON(attrs));
				}
				catch (IOException e) {
					if (Platform.inDebugMode()) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										"PeerModelPersistableAdapter export failure: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
						Platform.getLog(CoreBundleActivator.getDefault().getBundle()).log(status);
					}
				}
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

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable2#getEncodedClassName(java.lang.Object)
	 */
	@Override
	public String getEncodedClassName(final Object data) {
		return IPeerModel.class.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable2#importFrom(java.lang.String)
	 */
	@Override
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
							throw new IOException("PeerModelPersistableAdapter#import: Mandatory attribure 'ID' is missing."); //$NON-NLS-1$
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
						throw new IOException("PeerModelPersistableAdapter#import: Object not of map type."); //$NON-NLS-1$
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
