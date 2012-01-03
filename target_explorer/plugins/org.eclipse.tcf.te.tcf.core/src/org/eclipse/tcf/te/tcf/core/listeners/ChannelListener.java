/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.listeners;

import org.eclipse.core.runtime.Assert;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.tracing.ITraceIds;
import org.eclipse.tcf.te.tcf.core.internal.utils.LogUtils;
import org.eclipse.tcf.te.tcf.core.listeners.interfaces.IChannelOpenListener;
import org.eclipse.tcf.te.tcf.core.nls.Messages;


/**
 * Channel listener implementation.
 */
public class ChannelListener implements IChannel.IChannelListener {
	// The reference to the channel
	private final IChannel channel;

	/**
	 * Constructor.
	 *
	 * @param channel The channel. Must not be <code>null</code>.
	 */
	public ChannelListener(IChannel channel) {
		Assert.isNotNull(channel);
		this.channel = channel;
	}

	/**
	 * Return the associated channel.
	 *
	 * @return The channel instance.
	 */
	protected final IChannel getChannel() {
		return channel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#congestionLevel(int)
	 */
	@Override
	public void congestionLevel(int level) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#onChannelClosed(java.lang.Throwable)
	 */
	@Override
	public void onChannelClosed(Throwable error) {
		Assert.isTrue(Protocol.isDispatchThread());

		// Detach the listeners cleanly
		detachListeners(getChannel());

		// Construct the cause message
		String cause = ""; //$NON-NLS-1$
		if (error != null) {
			cause = NLS.bind(Messages.InternalChannelListener_onChannelClosed_cause, error.getLocalizedMessage());
		}

		// Trace the channel closing
		LogUtils.logMessageForChannel(getChannel(), NLS.bind(Messages.InternalChannelListener_onChannelClosed_message, cause), ITraceIds.TRACE_CHANNELS, this);

		// Fire the property change event for the channel
		Tcf.fireChannelStateChangeListeners(getChannel(), IChannel.STATE_CLOSED);
	}

	/**
	 * Detach all registered listeners from the given channel.
	 *
	 * @param channel The channel. Must not be <code>null</code>.
	 */
	protected void detachListeners(IChannel channel) {
		Assert.isNotNull(channel);

		// Cleanly remove all listeners from the channel
		channel.removeChannelListener(this);

		// And remove the listener references from the global channel open listener
		IChannelOpenListener openListener = (IChannelOpenListener)Tcf.getAdapter(IChannelOpenListener.class);
		if (openListener != null) openListener.setChannelListener(channel, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#onChannelOpened()
	 */
	@Override
	public void onChannelOpened() {
	}
}
