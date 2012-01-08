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
public interface IDataExchangeNode2 extends IDataExchangeNode {

	/**
	 * Initialize the given properties container with default values for the data this node is
	 * managing.
	 * <p>
	 * This method is called once for each handler. The widgets are typically not yet created as
	 * this method can be called before the node is set visible.
	 *
	 * @param data The properties container or <code>null</code>.
	 */
	public void initializeData(IPropertiesContainer data);
}
