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
import org.eclipse.tcf.te.ui.views.interfaces.handler.IDeleteHandlerDelegate;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate;

/**
 * Adapter factory implementation.
 */
public class AdapterFactory implements IAdapterFactory {
	// The adapter for ILabelProvider.class
	private ILabelProvider labelProvider = new LaunchTreeLabelProvider();
	// The refresh handler delegate adapter
	private IRefreshHandlerDelegate refreshDelegate = new RefreshHandlerDelegate();
	// The delete handler delegate adapter
	private static IDeleteHandlerDelegate deleteDelegate = new DeleteHandlerDelegate();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof LaunchNode) {
			if (ILabelProvider.class.equals(adapterType)) {
				return labelProvider;
			}
			if (IDeleteHandlerDelegate.class.equals(adapterType) && LaunchNode.TYPE_LAUNCH_CONFIG.equals(((LaunchNode)adaptableObject).getType())) {
				return deleteDelegate;
			}
			if (IRefreshHandlerDelegate.class.equals(adapterType)) {
				return refreshDelegate;
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
						IDeleteHandlerDelegate.class,
						IRefreshHandlerDelegate.class
		};
	}

}
