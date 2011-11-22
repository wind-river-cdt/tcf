/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.adapters;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;

/**
 * Adapter helper property tester implementation.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		IAdapterManager manager = Platform.getAdapterManager();
		if (manager == null) return false;

		// "hasAdapter": Checks if the adapter given by the arguments is registered for the given receiver
		if ("hasAdapter".equals(property)) { //$NON-NLS-1$
			// The class to adapt to is within the expected value
			String adapterType = expectedValue instanceof String ? (String)expectedValue : null;
			if (adapterType != null) {
				return manager.hasAdapter(receiver, adapterType);
			}
		}
		if ("canAdaptTo".equals(property)) { //$NON-NLS-1$
			// Read the arguments and look for "forceAdapterLoad"
			boolean forceAdapterLoad = false;
			for (Object arg : args) {
				if (arg instanceof String && "forceAdapterLoad".equalsIgnoreCase((String)arg)) { //$NON-NLS-1$
					forceAdapterLoad = true;
				}
			}

			// The class to adapt to is within the expected value
			String adapterType = expectedValue instanceof String ? (String)expectedValue : null;
			if (adapterType != null) {
				Object adapter = manager.getAdapter(receiver, adapterType);
				if (adapter != null) return true;

				// No adapter. This can happen too if the plug-in contributing the adapter
				// factory hasn't been loaded yet.
				if (forceAdapterLoad) adapter = manager.loadAdapter(receiver, adapterType);
				if (adapter != null) return true;
			}
		}

	    return false;
	}
}
