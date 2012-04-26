/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.ui.IPersistableElement;
/**
 * The adapter factory to adapt an IPeerModel to an IPersistableElement.
 */
public class PeerModelAdapterFactory implements IAdapterFactory {
	// The adapter list.
	private static Class<?>[] adapters = {IPersistableElement.class};
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof IPeerModel) {
			IPeerModel peerModel = (IPeerModel) adaptableObject;
			if(IPersistableElement.class.equals(adapterType)) {
				return new PersistablePeerModel(peerModel);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return adapters;
	}
}
