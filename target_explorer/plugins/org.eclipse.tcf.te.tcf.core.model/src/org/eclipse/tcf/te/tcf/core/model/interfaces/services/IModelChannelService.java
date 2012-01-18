/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.model.interfaces.services;

import org.eclipse.tcf.protocol.IChannel;

/**
 * Common interface to be implemented by a model channel service.
 */
public interface IModelChannelService extends IModelService {

	/**
	 * Returns the associated channel instance.
	 *
	 * @return The associated channel instance or <code>null</code>.
	 */
	public IChannel getChannel();

	/**
	 * Returns a fully open channel to the runtime model's associated peer.
	 * <p>
	 * If no channel has been associated yet, or the associated channel had been closed already, the
	 * method will open a new channel to the remote peer being associated with the parent runtime
	 * model.
	 * <p>
	 * If a channel had been associated already, and the channel is in open state, this method will
	 * return the associated channel instead of opening a new one.
	 *
	 * @param done The callback to be invoked. Must not be <code>null</code>.
	 */
	public void openChannel(DoneOpenChannel done);

	/**
	 * Client call back interface for openChannel(...).
	 */
	interface DoneOpenChannel {
		/**
		 * Called when the channel fully opened or failed to open.
		 *
		 * @param error The error description if operation failed, <code>null</code> if succeeded.
		 * @param channel The channel object or <code>null</code>.
		 */
		void doneOpenChannel(Throwable error, IChannel channel);
	}

	/**
	 * Close the associated channel.
	 */
	public void closeChannel();
}
