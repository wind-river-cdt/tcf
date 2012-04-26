/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.selection.interfaces;

/**
 * A selection context.
 */
public interface ISelectionContext {

	/**
	 * Returns the context object of the selection.
	 * 
	 * @return The context object.
	 */
	public Object getContext();

	/**
	 * Returns all selected objects within the selected context.
	 *
	 * @param An array containing all selected objects, or <code>null</code>.
	 */
	public Object[] getSelections();

	/**
	 * Returns the type of this selection context.
	 *
	 * @param The type or <code>null</code>.
	 */
	public String getType();

	/**
	 * Sets the preferred context flag.
	 *
	 * @param isPreferred <code>True</code> to mark the selection context the preferred context,
	 *            <code>false</code> otherwise.
	 */
	public void setIsPreferredContext(boolean isPreferred);

	/**
	 * Return <code>true</code>, if this context is the preferred one. Preferred contexts always
	 * needs to be positively validated. If a context is _NOT_ marked as preferred, a selection
	 * should be valid even when this context is not valid.
	 * <p>
	 * The mechanism of preferred contexts is used for launch action enablement and to find/create
	 * launch configurations. A launch action should be enabled if at least the preferred contexts
	 * are valid, but for find/create, also not preferred contexts are used if valid. If not valid
	 * they should be ignored.
	 */
	public boolean isPreferredContext();
}
