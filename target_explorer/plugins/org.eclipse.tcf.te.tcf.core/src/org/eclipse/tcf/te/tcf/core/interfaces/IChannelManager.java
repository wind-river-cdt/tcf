/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.interfaces;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IPeer;

/**
 * TCF channel manager public API declaration.
 */
public interface IChannelManager extends IAdaptable {

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
	 * Opens a new channel to communicate with the given peer.
	 * <p>
	 * If <code>forceNew</code> is <code>true</code>, a new and not reference counted channel is
	 * opened and returned. The returned channel must be closed by the caller himself. The channel
	 * manager is not keeping track of non reference counted channels.
	 * <p>
	 * Reference counted channels are cached by the channel manager and must be closed via {@link #closeChannel(IChannel)}.
	 * <p>
	 * The method can be called from any thread context.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	public void openChannel(IPeer peer, boolean forceNew, DoneOpenChannel done);

	/**
	 * Opens a new channel to communicate with the peer described by the given peer attributes.
	 * <p>
	 * If <code>forceNew</code> is <code>true</code>, a new and not reference counted channel is
	 * opened and returned. The returned channel must be closed by the caller himself. The channel
	 * manager is not keeping track of non reference counted channels.
	 * <p>
	 * Reference counted channels are cached by the channel manager and must be closed via {@link #closeChannel(IChannel)}.
	 * <p>
	 * The method can be called from any thread context.
	 *
	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	public void openChannel(Map<String, String> peerAttributes, boolean forceNew, DoneOpenChannel done);

	/**
	 * Returns the shared channel instance for the given peer. Channels retrieved using this
	 * method cannot be closed by the caller.
	 * <p>
	 * Callers of this method are expected to test for the current channel state themselves.
	 * <p>
	 * The method can be called from any thread context.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @return The channel instance or <code>null</code>.
	 */
	public IChannel getChannel(IPeer peer);

	/**
	 * Closes the given channel.
	 * <p>
	 * If the given channel is a reference counted channel, the channel will be closed if the reference counter
	 * reaches 0. For non reference counted channels, the channel is closed immediately.
	 * <p>
	 * The method can be called from any thread context.
	 *
	 * @param channel The channel. Must not be <code>null</code>.
	 */
	public void closeChannel(IChannel channel);

	/**
	 * Close all open channel, no matter of the current reference count.
	 * <p>
	 * The method can be called from any thread context.
	 */
	public void closeAll();
}
