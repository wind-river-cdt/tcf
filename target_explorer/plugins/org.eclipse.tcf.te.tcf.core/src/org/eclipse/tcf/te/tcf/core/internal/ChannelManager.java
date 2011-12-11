/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.core.AbstractPeer;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.core.internal.nls.Messages;
import org.eclipse.tcf.te.tcf.core.internal.tracing.ITraceIds;


/**
 * TCF channel manager implementation.
 */
public final class ChannelManager extends PlatformObject implements IChannelManager {
	// The map of reference counters per peer id
	/* default */ final Map<String, AtomicInteger> refCounters = new HashMap<String, AtomicInteger>();
	// The map of channels per peer id
	/* default */ final Map<String, IChannel> channels = new HashMap<String, IChannel>();

	/**
	 * The channel monitor keeps watching the channel after it got successfully opened.
	 * If the channel is closing eventually, the channel monitor cleans out the reference
	 * counter and the channels map.
	 */
	private final class ChannelMonitor implements IChannel.IChannelListener {
		// Reference to the monitored channel
		private final IChannel channel;
		// Reference to the peer id the channel got opened for
		private final String id;

		/**
         * Constructor.
         *
         * @param The peer id. Must not be <code>null</code>.
         * @param The channel. Must not be <code>null</code>.
         */
        public ChannelMonitor(String id, IChannel channel) {
        	Assert.isNotNull(id);
        	Assert.isNotNull(channel);

        	this.id = id;
        	this.channel = channel;
        }

		/* (non-Javadoc)
		 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#onChannelOpened()
		 */
		@Override
		public void onChannelOpened() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#onChannelClosed(java.lang.Throwable)
		 */
		@Override
		public void onChannelClosed(Throwable error) {
			// Remove ourself
			channel.removeChannelListener(this);
			// Clean the reference counter and the channel map
			channels.remove(id);
			refCounters.remove(id);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#congestionLevel(int)
		 */
		@Override
		public void congestionLevel(int level) {
		}
	}

	/**
	 * Constructor.
	 */
	public ChannelManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#openChannel(org.eclipse.tcf.protocol.IPeer, boolean, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)
	 */
	@Override
	public void openChannel(final IPeer peer, final boolean forceNew, final DoneOpenChannel done) {
		if (Protocol.isDispatchThread()) {
			internalOpenChannel(peer, forceNew, done);
		} else {
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					internalOpenChannel(peer, forceNew, done);
				}
			});
		}
	}

	/**
	 * Internal implementation of {@link #openChannel(IPeer, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)}.
	 * <p>
	 * If <code>forceNew</code> is <code>true</code>, a new and not reference counted channel is opened and returned. The returned
	 * channel must be closed by the caller himself. The channel manager is not keeping track of non reference counted channels.
	 * <p>
	 * Reference counted channels are cached by the channel manager and must be closed via {@link #closeChannel(IChannel)} .
	 * <p>
	 * Method must be called within the TCF dispatch thread.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @param forceNew Specify <code>true</code> to force opening a new channel, <code>false</code> otherwise.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	/* default */ void internalOpenChannel(final IPeer peer, final boolean forceNew, final DoneOpenChannel done) {
		Assert.isNotNull(peer);
		Assert.isNotNull(done);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		// The channel instance to return
		IChannel channel = null;

		// Get the peer id
		final String id = peer.getID();

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
			CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_message, id, Boolean.valueOf(forceNew)),
														0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
		}

		// Check if there is already a channel opened to this peer
		channel = !forceNew ? channels.get(id) : null;
		if (channel != null && (channel.getState() == IChannel.STATE_OPEN || channel.getState() == IChannel.STATE_OPENING)) {
			// Increase the reference count
			AtomicInteger counter = refCounters.get(id);
			if (counter == null) {
				counter = new AtomicInteger(0);
				refCounters.put(id, counter);
			}
			counter.incrementAndGet();

			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_reuse_message, id, counter.toString()),
															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
			}
		} else if (channel != null) {
			// Channel is not in open state -> drop the instance
			channel = null;
			channels.remove(id);
			refCounters.remove(id);
		}

		// Opens a new channel if necessary
		if (channel == null) {
			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_new_message, id),
															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
			}

			channel = peer.openChannel();

			if (channel != null) {
				if (!forceNew) channels.put(id, channel);
				if (!forceNew) refCounters.put(id, new AtomicInteger(1));

				// Register the channel listener
				final IChannel finChannel = channel;
				channel.addChannelListener(new IChannel.IChannelListener() {

					@Override
					public void onChannelOpened() {
						// Remove ourself as listener from the channel
						finChannel.removeChannelListener(this);
						// Register the channel monitor
						finChannel.addChannelListener(new ChannelMonitor(id, finChannel));

						if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
							CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_success_message, id),
																		0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
						}

						// Channel opening succeeded
						done.doneOpenChannel(null, finChannel);
					}

					@Override
					public void onChannelClosed(Throwable error) {
						// Remove ourself as listener from the channel
						finChannel.removeChannelListener(this);
						// Clean the reference counter and the channel map
						channels.remove(id);
						refCounters.remove(id);

						if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
							CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_failed_message, id, error),
																		0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
						}

						// Channel opening failed
						done.doneOpenChannel(error, finChannel);
					}

					@Override
					public void congestionLevel(int level) {
						// ignored
					}
				});
			} else {
				// Channel is null? Something went terrible wrong.
				done.doneOpenChannel(new Exception("Unexpected null return value from IPeer#openChannel()!"), null); //$NON-NLS-1$
			}

		} else {
			done.doneOpenChannel(null, channel);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#openChannel(java.util.Map, boolean, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)
	 */
	@Override
	public void openChannel(final Map<String, String> peerAttributes, final boolean forceNew, final DoneOpenChannel done) {
		if (Protocol.isDispatchThread()) {
			internalOpenChannel(peerAttributes, forceNew, done);
		} else {
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					internalOpenChannel(peerAttributes, forceNew, done);
				}
			});
		}
	}

	/**
	 * Internal implementation of {@link #openChannel(Map, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)}.
	 * <p>
	 * Method must be called within the TCF dispatch thread.
	 *
	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
	 * @param forceNew Specify <code>true</code> to force opening a new channel, <code>false</code> otherwise.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	/* default */ void internalOpenChannel(final Map<String, String> peerAttributes, final boolean forceNew, final DoneOpenChannel done) {
		Assert.isNotNull(peerAttributes);
		Assert.isNotNull(done);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		internalOpenChannel(getOrCreatePeerInstance(peerAttributes), forceNew, done);
	}

	/**
	 * Tries to find an existing peer instance or create an new {@link IPeer}
	 * instance if not found.
	 * <p>
	 * <b>Note:</b> This method must be invoked at the TCF dispatch thread.
	 *
	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
	 * @return The peer instance.
	 */
	private IPeer getOrCreatePeerInstance(final Map<String, String> peerAttributes) {
		Assert.isNotNull(peerAttributes);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		// Get the peer id from the properties
		String peerId = peerAttributes.get(IPeer.ATTR_ID);
		Assert.isNotNull(peerId);

		// Check if we shall open the peer transient
		boolean isTransient = peerAttributes.containsKey("transient") ? Boolean.parseBoolean(peerAttributes.remove("transient")) : false; //$NON-NLS-1$ //$NON-NLS-2$

		// Look the peer via the Locator Service.
		IPeer peer = Protocol.getLocator().getPeers().get(peerId);
		// If not peer could be found, create a new one
		if (peer == null) {
			peer = isTransient ? new TransientPeer(peerAttributes) : new AbstractPeer(peerAttributes);

			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_createPeer_new_message, peerId, Boolean.valueOf(isTransient)),
															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
			}
		}

		// Return the peer instance
		return peer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#getChannel(org.eclipse.tcf.protocol.IPeer)
	 */
	@Override
	public IChannel getChannel(final IPeer peer) {
		final AtomicReference<IChannel> channel = new AtomicReference<IChannel>();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				channel.set(internalGetChannel(peer));
			}
		};
		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

	    return channel.get();
	}

	/**
	 * Returns the shared channel instance for the given peer.
	 * <p>
	 * <b>Note:</b> This method must be invoked at the TCF dispatch thread.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @return The channel instance or <code>null</code>.
	 */
	public IChannel internalGetChannel(IPeer peer) {
		Assert.isNotNull(peer);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		return channels.get(peer.getID());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#closeChannel(org.eclipse.tcf.protocol.IChannel)
	 */
	@Override
	public void closeChannel(final IChannel channel) {
		if (Protocol.isDispatchThread()) {
			internalCloseChannel(channel);
		} else {
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					internalCloseChannel(channel);
				}
			});
		}
	}

	/**
	 * Closes the given channel.
	 * <p>
	 * If the given channel is a reference counted channel, the channel will be closed if the reference counter
	 * reaches 0. For non reference counted channels, the channel is closed immediately.
	 * <p>
	 * <b>Note:</b> This method must be invoked at the TCF dispatch thread.
	 *
	 * @param channel The channel. Must not be <code>null</code>.
	 */
	/* default */ void internalCloseChannel(IChannel channel) {
		Assert.isNotNull(channel);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		// Get the id of the remote peer
		String id = channel.getRemotePeer().getID();

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
			CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_closeChannel_message, id),
														0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
		}

		// Get the reference counter
		AtomicInteger counter = refCounters.get(id);

		// If the counter is null or get 0 after the decrement, close the channel
		if (counter == null || counter.decrementAndGet() == 0) {
			channel.close();

			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_closeChannel_closed_message, id),
															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
			}
		} else {
			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_closeChannel_inuse_message, id, counter.toString()),
															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
			}
		}

		// Clean the reference counter and the channel map
		refCounters.remove(id);
		channels.remove(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#closeAll()
	 */
	@Override
	public void closeAll() {
		if (Protocol.isDispatchThread()) {
			internalCloseAll();
		} else {
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					internalCloseAll();
				}
			});
		}
	}

	/**
	 * Close all open channel, no matter of the current reference count.
	 * <p>
	 * <b>Note:</b> This method must be invoked at the TCF dispatch thread.
	 */
	/* default */ void internalCloseAll() {
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		IChannel[] openChannels = channels.values().toArray(new IChannel[channels.values().size()]);

		refCounters.clear();
		channels.clear();

		for (IChannel channel : openChannels) internalCloseChannel(channel);
	}
}
