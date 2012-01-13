/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.interfaces.handler;

import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * Interface to be implemented by refreshable nodes.
 * <p>
 * The interface can be implemented directly or provided as adapter.
 */
public interface IRefreshHandlerDelegate {

	/**
	 * Determines if the given element can be refreshed or not. If the
	 * method returns <code>false</code>, the refresh action will be still
	 * visible for such an element, but disabled.
	 *
	 * @param element The element. Must not be <code>null</code>.
	 * @return <code>True</code> if the element can be refreshed, <code>false</code> otherwise.
	 */
	public boolean canRefresh(Object element);

	/**
	 * Refresh the given element.
	 * <p>
	 * <b>Note:</b> The refresh method will be called for each element of the current selection.
	 * This might not be very practicable in all situations where the refresh needs optimize the
	 * refreshed elements of a given selection. Clients can use the state properties container
	 * to store refresh operation wide status information and/or identify a refresh operation
	 * via the state properties container unique id.
	 * <p>
	 * The default refresh handler implementation stores the current selection to the state properties
	 * container using the key &quot;selection&quot;.
	 *
	 * @param element The element. Must not be <code>null</code>.
	 * @param state The refresh operation state. Must not be <code>null</code>.
	 * @param callback The callback to invoke once the operation finished, or <code>null</code>.
	 */
	public void refresh(Object element, IPropertiesContainer state, ICallback callback);
}
