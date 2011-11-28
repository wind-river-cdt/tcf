/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.internal.listener;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.internal.interfaces.IChannelOpenListener;
import org.eclipse.tcf.te.tcf.core.internal.nls.Messages;
import org.eclipse.tcf.te.tcf.core.internal.tracing.ITraceIds;
import org.eclipse.tcf.te.tcf.core.internal.utils.LogUtils;


/**
 * Internal channel open listener taking care of logging and caching.
 */
public class InternalChannelOpenListener implements IChannelOpenListener {
	// Static map containing the channel listeners per channel. Access to the
	// map should happen from the TCF protocol dispatch thread only.
	private final Map<IChannel, IChannel.IChannelListener> channelListeners = new HashMap<IChannel, IChannel.IChannelListener>();

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.protocol.Protocol.ChannelOpenListener#onChannelOpen(org.eclipse.tcf.protocol.IChannel)
	 */
	@Override
	public void onChannelOpen(IChannel channel) {
		Assert.isNotNull(channel);
		Assert.isTrue(Protocol.isDispatchThread());

		// Trace the channel opening
		LogUtils.logMessageForChannel(channel, Messages.InternalChannelOpenListener_onChannelOpen_message, ITraceIds.TRACE_CHANNELS, this);

		// As the channel has just opened, there should be no channel listener, but better be safe and check.
		IChannel.IChannelListener channelListener = channelListeners.remove(channel);
		if (channelListener != null) channel.removeChannelListener(channelListener);
		// Create a new channel listener instance
		channelListener = new InternalChannelListener(channel);
		// Add the channel listener to the global map
		setChannelListener(channel, channelListener);
		// Attach channel listener to the channel
		channel.addChannelListener(channelListener);

		// Fire the property change event for the channel
		Tcf.fireChannelStateChangeListeners(channel, IChannel.STATE_OPEN);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.internal.interfaces.IChannelOpenListener#setChannelListener(org.eclipse.tcf.protocol.IChannel, org.eclipse.tcf.protocol.IChannel.IChannelListener)
	 */
	@Override
	public void setChannelListener(IChannel channel, IChannelListener listener) {
		Assert.isNotNull(channel);
		if (listener != null) channelListeners.put(channel, listener);
		else channelListeners.remove(channel);
	}
}
