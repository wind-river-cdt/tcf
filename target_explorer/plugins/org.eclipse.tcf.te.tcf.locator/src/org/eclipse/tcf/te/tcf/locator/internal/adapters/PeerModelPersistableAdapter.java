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
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistable;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNodeProperties;
import org.eclipse.tcf.te.tcf.locator.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Persistable implementation handling peer attributes.
 */
public class PeerModelPersistableAdapter implements IPersistable {

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
			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);

			if (nodeURI.get() != null) uri = nodeURI.get();
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
			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);
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

}
