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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ITargetSelectionContext;

/**
 * Target selection context implementation.
 */
public class TargetSelectionContext extends AbstractSelectionContext implements ITargetSelectionContext {
	// The target context
	private IAdaptable targetCtx = null;

	/**
	 * Constructor.
	 *
	 * @param target The target context or <code>null</code>.
	 * @param isPreferred <code>True</code> to mark the selection context the preferred context,
	 *            <code>false</code> otherwise.
	 */
	public TargetSelectionContext(IAdaptable target, boolean isPreferred) {
		this(target, null, isPreferred);
	}

	/**
	 * Constructor.
	 *
	 * @param target The target context or <code>null</code>.
	 * @param selections The selected objects or <code>null</code>.
	 * @param isPreferred <code>True</code> to mark the selection context the preferred context,
	 *            <code>false</code> otherwise.
	 */
	public TargetSelectionContext(IAdaptable target, Object[] selections, boolean isPreferred) {
		super(selections, isPreferred);
		this.targetCtx = target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.selection.interfaces.ITargetSelectionContext#getTargetCtx()
	 */
	@Override
	public IAdaptable getTargetCtx() {
	    return targetCtx;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer();

		if (targetCtx != null) {
			toString.append(targetCtx.toString());
		}
		toString.append(toString.length() > 0 ? " " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		toString.append(super.toString());

		return toString.toString();
	}
}
