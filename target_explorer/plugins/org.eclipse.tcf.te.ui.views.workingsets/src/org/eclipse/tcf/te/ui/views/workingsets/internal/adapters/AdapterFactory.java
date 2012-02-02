/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.workingsets.internal.adapters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tcf.te.ui.views.internal.View;
import org.eclipse.tcf.te.ui.views.workingsets.WorkingSetViewStateManager;

/**
 * Adapter factory implementation.
 */
public class AdapterFactory implements IAdapterFactory {
	// Maintain a map of view state manager adapters per view instance
	/* default */ Map<View, WorkingSetViewStateManager> adapters = new HashMap<View, WorkingSetViewStateManager>();

	private static final Class<?>[] CLASSES = new Class[] {
		WorkingSetViewStateManager.class
	};

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof View) {
			if (WorkingSetViewStateManager.class.equals(adapterType)) {
				// Lookup the adapter
				WorkingSetViewStateManager adapter = adapters.get(adaptableObject);
				// No adapter yet -> create a new one for this view
				if (adapter == null) {
					adapter = new WorkingSetViewStateManager((View)adaptableObject);
					adapters.put((View)adaptableObject, adapter);
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
