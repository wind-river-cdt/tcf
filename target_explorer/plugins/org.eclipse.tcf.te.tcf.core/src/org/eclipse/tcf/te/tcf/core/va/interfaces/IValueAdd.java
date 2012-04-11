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

import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.extensions.IExecutableExtension;

/**
 * Interface to be implemented by value-add's.
 * <p>
 * <b>Note:</b> Asynchronous methods must be called from within of the TCF dispatch thread.
 */
public interface IValueAdd extends IExecutableExtension {

	/**
	 * Returns if or if not the value-add is optional.
	 *
	 * @return <code>True</code> if the value-add is optional, <code>false</code> otherwise.
	 */
	public boolean isOptional();

	/**
	 * Returns if or if not the value-add is alive for the given target peer.
	 * <p>
	 * In this context, alive typically means that the value-add process is
	 * up, running and responsive.
	 *
	 * @param id The target peer id. Must not be <code>null</code>.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	public void isAlive(String id, ICallback done);

	/**
	 * Launch the value-add for the given target peer.
	 *
	 * @param id The target peer id. Must not be <code>null</code>.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	public void launch(String id, ICallback done);

	/**
	 * Shuts down the value-add for the given target peer.
	 *
	 * @param id The target peer id. Must not be <code>null</code>.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	public void shutdown(String id, ICallback done);

	/**
	 * Returns the peer representing the value add for the given target peer.
	 *
	 * @param id The target peer id. Must not be <code>null</code>.
	 * @return The value-add's peer.
	 */
	public IPeer getPeer(String id);
}
