/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.interfaces.nodes;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Interface to be implemented by nodes providing access to an peer
 * model object instance without being a peer model object itself.
 */
public interface IPeerModelProvider extends IAdaptable {

	/**
	 * Returns the associated peer model object.
	 *
	 * @return The peer model object instance or <code>null</code>.
	 */
	public IPeerModel getPeerModel();
}
