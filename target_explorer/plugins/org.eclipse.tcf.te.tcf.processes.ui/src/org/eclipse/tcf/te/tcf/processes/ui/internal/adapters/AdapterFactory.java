/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.internal.columns.ProcessLabelProvider;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate;

/**
 * Adapter factory implementation.
 */
public class AdapterFactory implements IAdapterFactory {
	// The adapter for ILabelProvider.class
	private ILabelProvider labelProvider = new ProcessLabelProvider();
	// The refresh handler delegate adapter
	private IRefreshHandlerDelegate refreshDelegate = new RefreshHandlerDelegate();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ProcessTreeNode) {
			if (ILabelProvider.class.equals(adapterType)) {
				return labelProvider;
			}
			if (IPeerModel.class.equals(adapterType)) {
				return ((ProcessTreeNode)adaptableObject).peerNode;
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
						IPeerModel.class,
						IRefreshHandlerDelegate.class
					};
	}

}
