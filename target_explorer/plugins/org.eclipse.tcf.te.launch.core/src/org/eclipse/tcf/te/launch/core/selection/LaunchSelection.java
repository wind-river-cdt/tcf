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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;

/**
 * Launch selection implementation.
 */
public class LaunchSelection implements ILaunchSelection {
	// The launch mode the selection has been created for
	private String mode;
	// The selection contexts
	private ISelectionContext[] contexts = null;

	/**
	 * Constructor.
	 *
	 * @param mode The launch mode or <code>null</code>.
	 * @param context The selection context or <code>null</code>.
	 */
	public LaunchSelection(String mode, ISelectionContext context) {
		this(mode, context != null ? new ISelectionContext[] { context } : null);
	}

	/**
	 * Constructor.
	 *
	 * @param mode The launch mode or <code>null</code>.
	 */
	public LaunchSelection(String mode, ISelectionContext[] contexts) {
		this.mode = mode;
		this.contexts = contexts != null ? Arrays.copyOf(contexts, contexts.length) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection#getLaunchMode()
	 */
	@Override
    public final String getLaunchMode() {
		return mode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection#getSelectedContexts()
	 */
	@Override
    public ISelectionContext[] getSelectedContexts() {
		return contexts != null ? Arrays.copyOf(contexts, contexts.length) : new ISelectionContext[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection#getSelectedContexts(java.lang.Class)
	 */
	@Override
    public ISelectionContext[] getSelectedContexts(Class<?> type) {
		List<ISelectionContext> contexts = new ArrayList<ISelectionContext>();

		for (ISelectionContext selectionContext : getSelectedContexts()) {
			if (type.isInstance(selectionContext)) {
				contexts.add(selectionContext);
			}
		}

		return contexts.toArray(new ISelectionContext[contexts.size()]);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean equals = obj instanceof ILaunchSelection;

		if (equals) {
			ILaunchSelection otherSelection = (ILaunchSelection)obj;
			// Launch mode must be the same
			equals &= mode == null && otherSelection.getLaunchMode() == null || mode != null && mode.equals(otherSelection.getLaunchMode());

			// And all selection contexts must be the same
			ISelectionContext[] c1 = getSelectedContexts();
			ISelectionContext[] c2 = otherSelection.getSelectedContexts();

			equals &= c1.length == c2.length;

			int i = 0;
			while (equals && i < c1.length) {
				equals &= c1[i].equals(c2[i]);
				i++;
			}
		}

		return equals;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = 0;

		if (mode != null) hashCode = mode.hashCode() << 16;

		for (ISelectionContext context : getSelectedContexts()) {
			hashCode = hashCode ^ context.hashCode();
		}

		return hashCode;
	}
}
