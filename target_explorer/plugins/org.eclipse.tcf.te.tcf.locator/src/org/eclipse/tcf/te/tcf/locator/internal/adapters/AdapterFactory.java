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

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableURIProvider;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;

/**
 * Static peers adapter factory implementation.
 */
public class AdapterFactory implements IAdapterFactory {
	// The single instance adapter references
	private final IPersistableURIProvider peerModelPersistableURIProvider = new PeerPersistableURIProvider();

	private static final Class<?>[] CLASSES = new Class[] {
		IPersistableURIProvider.class
	};

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof Map) {
			if (IPersistableURIProvider.class.equals(adapterType)) {
				Assert.isTrue(false);
			}
		}
		if (adaptableObject instanceof IPeerModel || adaptableObject instanceof IPeer || adaptableObject instanceof IPeerModelProvider) {
			if (IPersistableURIProvider.class.equals(adapterType)) {
				return peerModelPersistableURIProvider;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return CLASSES;
	}

}
