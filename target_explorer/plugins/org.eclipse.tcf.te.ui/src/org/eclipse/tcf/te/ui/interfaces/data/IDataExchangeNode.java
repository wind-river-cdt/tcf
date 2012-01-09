/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.interfaces.data;

import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * Public interface for wizard or dialog pages, panel, controls or other UI elements
 * exchanging data via a shared data object.
 */
public interface IDataExchangeNode {

	/**
	 * Initialize the widgets based of the data from the given properties container.
	 * <p>
	 * This method may called multiple times during the lifetime of the node and the given
	 * properties container might be even <code>null</code>.
	 *
	 * @param data The properties container or <code>null</code>.
	 */
	public void setupData(IPropertiesContainer data);

	/**
	 * Extract the data from the widgets and write it back to the given properties container.
	 * <p>
	 * This method may called multiple times during the lifetime of the node and the given
	 * properties container might be even <code>null</code>.
	 *
	 * @param data The properties container or <code>null</code>.
	 */
	public void extractData(IPropertiesContainer data);
}
