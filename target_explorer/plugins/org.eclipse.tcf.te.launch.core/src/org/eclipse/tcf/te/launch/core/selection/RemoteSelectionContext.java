/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.selection;

import org.eclipse.tcf.te.launch.core.selection.interfaces.IRemoteSelectionContext;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;

/**
 * Step context selection context implementation.
 */
public class RemoteSelectionContext extends AbstractSelectionContext implements IRemoteSelectionContext {

	/**
	 * Constructor.
	 *
	 * @param remoteCtx The remote context or <code>null</code>.
	 * @param isPreferred <code>True</code> to mark the selection context the preferred context,
	 *            <code>false</code> otherwise.
	 */
	public RemoteSelectionContext(IModelNode remoteCtx, boolean isPreferred) {
		this(remoteCtx, new Object[]{remoteCtx}, isPreferred);
	}

	/**
	 * Constructor.
	 *
	 * @param remoteCtx The remote context or <code>null</code>.
	 * @param selections The selected objects or <code>null</code>.
	 * @param isPreferred <code>True</code> to mark the selection context the preferred context,
	 *            <code>false</code> otherwise.
	 */
	public RemoteSelectionContext(IModelNode remoteCtx, Object[] selections, boolean isPreferred) {
		super(remoteCtx, selections, isPreferred);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.selection.interfaces.IRemoteSelectionContext#getRemoteCtx()
	 */
	@Override
	public IModelNode getRemoteCtx() {
		return (IModelNode)getContext();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer();

		if (getContext() != null) {
			toString.append(getContext().toString());
		}
		toString.append(toString.length() > 0 ? " " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		toString.append(super.toString());

		return toString.toString();
	}
}
