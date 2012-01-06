/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.services.contexts.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.interfaces.IDisposable;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.listener.ModelAdapter;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.services.contexts.interfaces.IContexts;

/**
 * Contexts service adapter factory implementation.
 */
public class AdapterFactory implements IAdapterFactory {
	// Maintain a map of contexts service proxy per peer
	/* default */ Map<IPeer, IContexts> proxies = new HashMap<IPeer, IContexts>();

	private static final Class<?>[] CLASSES = new Class[] {
		IContexts.class
	};

	/**
     * Constructor.
     */
    public AdapterFactory() {
    	Model.getModel().addListener(new ModelAdapter() {
    		/* (non-Javadoc)
    		 * @see org.eclipse.tcf.te.tcf.locator.listener.ModelAdapter#locatorModelChanged(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel, org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel, boolean)
    		 */
    		@Override
    		public void locatorModelChanged(ILocatorModel model, IPeerModel peer, boolean added) {
    			// If a peer gets removed, remove the context service proxy
    			if (peer != null && peer.getPeer() != null) {
    				IContexts proxy = proxies.remove(peer.getPeer());
    				if (proxy instanceof IDisposable) ((IDisposable)proxy).dispose();
    			}
    		}
    	});
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IPeer) {
			// Lookup the proxy
			IContexts proxy = proxies.get(adaptableObject);
			// No proxy yet -> create a new one for this peer
			if (proxy == null) {
				proxy = new ContextsProxy((IPeer)adaptableObject);
				proxies.put((IPeer)adaptableObject, proxy);
			}
			return proxy;
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
