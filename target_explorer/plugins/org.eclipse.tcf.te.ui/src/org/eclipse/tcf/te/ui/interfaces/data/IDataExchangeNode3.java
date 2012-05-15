/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
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
public interface IDataExchangeNode3 extends IDataExchangeNode2 {

	/**
	 * Remove the data the node is managing from the given properties container.
	 * <p>
	 * This method may called multiple times during the lifetime of the node and the given
	 * properties container might be even <code>null</code>.
	 *
	 * @param data The properties container or <code>null</code>.
	 */
	public void removeData(IPropertiesContainer data);

	/**
	 * Copy the data the node is managing from the given source properties container
	 * to the given destination properties container.
	 *
	 * @param src The source properties container. Must not be <code>null</code>.
	 * @param dst The destination properties. Must not be <code>null/code>.
	 */
	public void copyData(IPropertiesContainer src, IPropertiesContainer dst);
}
