/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.workingsets;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.ui.IWorkingSet;

/**
 * The adapter factory to adapt a working set to IElementComparer, which
 * customize the comparison during restoring expanding state.
 */
public class WorkingSetAdapterFactory implements IAdapterFactory {
	// The adapter list.
	private static Class<?>[] adapters = {IElementComparer.class};
	// The adapted IElementComparer of a working set.
	private static IElementComparer wsComparer = new WorkingSetComparer();
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof IWorkingSet && IElementComparer.class.equals(adapterType)) {
			return wsComparer;
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
