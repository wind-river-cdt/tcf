/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.interfaces.nodes;

import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;

/**
 * The peer model is an extension to the TCF peer representation, implementing the {@link IPeer}
 * interface. The peer model provides an offline cache for a peers known list of local and remote
 * services and is the merge point of peer attributes from custom data storages.
 * <p>
 * <b>Note:</b> Read and write access to the peer model must happen within the TCF dispatch thread.
 */
public interface IPeerModel extends IContainerModelNode {

	/**
	 * Returns the parent locator model instance.
	 * <p>
	 * This method may be called from any thread.
	 *
	 * @return The parent locator model instance.
	 */
	public ILocatorModel getModel();

	/**
	 * Returns the native {@link IPeer} object.
	 * <p>
	 * This method may be called from any thread.
	 *
	 * @return The native {@link IPeer} instance.
	 */
	public IPeer getPeer();

	/**
	 * Returns the peer id.
	 * <p>
	 * This method may be called from any thread.
	 *
	 * @return The peer id.
	 */
	public String getPeerId();

	/**
	 * Returns if or if not the peer attributes are complete to open a channel to it.
	 *
	 * @return <code>True</code> if the peer attributes are complete, <code>false</code> otherwise.
	 */
	public boolean isComplete();
}
