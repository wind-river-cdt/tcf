/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
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
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.IDisposable;
import org.eclipse.tcf.te.tcf.locator.interfaces.IModelListener;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.listener.ModelAdapter;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.services.contexts.interfaces.IContextService;

/**
 * Context service adapter factory implementation.
 */
public class AdapterFactory implements IAdapterFactory {
	// Maintain a map of contexts service adapters per peer
	/* default */ Map<IPeer, IContextService> adapters = new HashMap<IPeer, IContextService>();

	private static final Class<?>[] CLASSES = new Class[] {
		IContextService.class
	};

	/**
     * Constructor.
     */
    public AdapterFactory() {
    	final IModelListener listener = new ModelAdapter() {
    		/* (non-Javadoc)
    		 * @see org.eclipse.tcf.te.tcf.locator.listener.ModelAdapter#locatorModelChanged(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel, org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel, boolean)
    		 */
    		@Override
    		public void locatorModelChanged(ILocatorModel model, IPeerModel peer, boolean added) {
    			// If a peer gets removed, remove the context service proxy
    			if (peer != null && peer.getPeer() != null && !added) {
    				IContextService adapter = adapters.remove(peer.getPeer());
    				if (adapter instanceof IDisposable) ((IDisposable)adapter).dispose();
    			}
    		}
    	};

    	Runnable runnable = new Runnable() {
			@Override
			public void run() {
		    	Model.getModel().addListener(listener);
			}
		};

		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IPeer) {
			// Lookup the adapter
			IContextService adapter = adapters.get(adaptableObject);
			// No adapter yet -> create a new one for this peer
			if (adapter == null) {
				adapter = new ContextServiceAdapter((IPeer)adaptableObject);
				adapters.put((IPeer)adaptableObject, adapter);
			}
			return adapter;
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
