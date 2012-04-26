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

import org.eclipse.core.resources.IProject;
import org.eclipse.tcf.te.launch.core.selection.interfaces.IProjectSelectionContext;

/**
 * Project launch selection context implementation.
 */
public class ProjectSelectionContext extends AbstractSelectionContext implements IProjectSelectionContext {

	/**
	 * Constructor.
	 *
	 * @param project The project context or <code>null</code>.
	 * @param isPreferred <code>True</code> to mark the selection context the preferred context,
	 *            <code>false</code> otherwise.
	 */
	public ProjectSelectionContext(IProject project, boolean isPreferred) {
		this(project, new Object[]{project}, isPreferred);
	}

	/**
	 * Constructor.
	 *
	 * @param project The project context or <code>null</code>.
	 * @param selections The selected objects or <code>null</code>.
	 * @param isPreferred <code>True</code> to mark the selection context the preferred context,
	 *            <code>false</code> otherwise.
	 */
	public ProjectSelectionContext(IProject project, Object[] selections, boolean isPreferred) {
		super(project, selections, isPreferred);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.selection.interfaces.IProjectSelectionContext#getProjectCtx()
	 */
	@Override
	public IProject getProjectCtx() {
		return (IProject)getContext();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer();

		if (getProjectCtx() != null) {
			toString.append(getProjectCtx().getName());
		}
		toString.append(toString.length() > 0 ? " " : ""); //$NON-NLS-1$ //$NON-NLS-2$
		toString.append(super.toString());

		return toString.toString();
	}
}
