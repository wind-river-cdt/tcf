/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.core.internal.adapters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tcf.te.runtime.interfaces.IDisposable;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.listener.ModelAdapter;
import org.eclipse.tcf.te.tcf.locator.model.Model;

/**
 * Adapter factory implementation.
 */
public class AdapterFactory implements IAdapterFactory {
	// Maintain a map of step context adapters per peer model
	/* default */ Map<IPeerModel, IStepContext> adapters = new HashMap<IPeerModel, IStepContext>();

	private static final Class<?>[] CLASSES = new Class[] {
		IStepContext.class
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
    			// If a peer gets removed, remove the step context adapter too
    			if (peer != null && !added) {
    				IStepContext adapter = adapters.remove(peer);
    				if (adapter instanceof IDisposable) ((IDisposable)adapter).dispose();
    			}
    		}
    	});
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IPeerModel) {
			if (IStepContext.class.equals(adapterType)) {
				// Lookup the adapter
				IStepContext adapter = adapters.get(adaptableObject);
				// No adapter yet -> create a new one for this peer
				if (adapter == null) {
					adapter = new StepContextAdapter((IPeerModel)adaptableObject);
					adapters.put((IPeerModel)adaptableObject, adapter);
				}
				return adapter;
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
