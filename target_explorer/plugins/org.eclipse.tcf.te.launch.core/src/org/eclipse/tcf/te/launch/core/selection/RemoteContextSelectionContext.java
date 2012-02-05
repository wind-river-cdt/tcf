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

import org.eclipse.tcf.te.launch.core.selection.interfaces.IRemoteContextSelectionContext;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IContext;

/**
 * Remote context selection context implementation.
 */
public class RemoteContextSelectionContext extends AbstractSelectionContext implements IRemoteContextSelectionContext {
	// The remote context
	private IContext remoteCtx = null;

	/**
	 * Constructor.
	 *
	 * @param remoteCtx The remote context or <code>null</code>.
	 * @param isPreferred <code>True</code> to mark the selection context the preferred context,
	 *            <code>false</code> otherwise.
	 */
	public RemoteContextSelectionContext(IContext remoteCtx, boolean isPreferred) {
		this(remoteCtx, null, isPreferred);
	}

	/**
	 * Constructor.
	 *
	 * @param remoteCtx The remote context or <code>null</code>.
	 * @param selections The selected objects or <code>null</code>.
	 * @param isPreferred <code>True</code> to mark the selection context the preferred context,
	 *            <code>false</code> otherwise.
	 */
	public RemoteContextSelectionContext(IContext remoteCtx, Object[] selections, boolean isPreferred) {
		super(selections, isPreferred);
		this.remoteCtx = remoteCtx;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.selection.interfaces.IRemoteContextSelectionContext#getRemoteCtx()
	 */
	@Override
	public IContext getRemoteCtx() {
	    return remoteCtx;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer();

		if (remoteCtx != null) {
			toString.append(remoteCtx.toString());
		}
		toString.append(toString.length() > 0 ? " " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		toString.append(super.toString());

		return toString.toString();
	}
}
