/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.va.interfaces;

import org.eclipse.tcf.te.runtime.interfaces.extensions.IExecutableExtension;

/**
 * Interface to be implemented by value-add's.
 * <p>
 * <b>Note:</b> All methods of this interface must be called from <b>outside</b> of the TCF dispatch thread.
 */
public interface IValueAdd extends IExecutableExtension {

	/**
	 * Returns if or if not the value-add is alive for the given target peer.
	 * <p>
	 * In this context, alive typically means that the value-add process is
	 * up, running and responsive.
	 *
	 * @param id The target peer id. Must not be <code>null</code>.
	 * @return <code>True</code> if the value-add is alive, <code>false</code> otherwise.
	 */
	public boolean isAlive(String id);

	/**
	 * Launch the value-add for the given target peer.
	 *
	 * @param id The target peer id. Must not be <code>null</code>.
	 * @return A throwable describing the error if the launch failed, <code>null</code> if the launch succeeds.
	 */
	public Throwable launch(String id);
}
