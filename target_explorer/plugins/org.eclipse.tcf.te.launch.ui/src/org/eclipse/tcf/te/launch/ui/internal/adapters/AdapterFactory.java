/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.tcf.te.launch.ui.internal.viewer.LaunchTreeLabelProvider;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable;

/**
 * Adapter factory implementation.
 */
public class AdapterFactory implements IAdapterFactory {
	// The adapter for ILabelProvider.class
	private ILabelProvider labelProvider = new LaunchTreeLabelProvider();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof LaunchNode) {
			LaunchNode node = (LaunchNode)adaptableObject;
			if (ILabelProvider.class.equals(adapterType)) {
				return labelProvider;
			}
			if (ICategorizable.class.equals(adapterType) && node.getLaunchConfiguration() != null) {
				return new CategorizableAdapter(node);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return new Class<?>[] {
						ILabelProvider.class,
						ICategorizable.class
		};
	}

}
