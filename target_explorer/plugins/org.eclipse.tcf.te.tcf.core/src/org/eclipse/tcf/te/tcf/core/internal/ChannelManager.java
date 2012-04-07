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
import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
import org.eclipse.tcf.te.tcf.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.core.interfaces.tracing.ITraceIds;
import org.eclipse.tcf.te.tcf.core.nls.Messages;
import org.eclipse.tcf.te.tcf.core.va.ValueAddManager;
import org.eclipse.tcf.te.tcf.core.va.interfaces.IValueAdd;


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
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#openChannel(org.eclipse.tcf.protocol.IPeer, java.util.Map, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)
	 */
	@Override
	public void openChannel(final IPeer peer, final Map<String, Boolean> flags, final DoneOpenChannel done) {
		Runnable runnable = new Runnable() {
			@Override
            public void run() {
				// Check on the value-add's first
				internalHandleValueAdds(peer, flags, new DoneHandleValueAdds() {
					@Override
					public void doneHandleValueAdds(Throwable error, IValueAdd[] valueAdds) {
						// Open the channel
						internalOpenChannel(peer, flags, done);
					}
				});
			}
		};
		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeLater(runnable);
	}

	/**
	 * Internal implementation of {@link #openChannel(IPeer, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)}.
	 * <p>
	 * Reference counted channels are cached by the channel manager and must be closed via {@link #closeChannel(IChannel)} .
	 * <p>
	 * Method must be called within the TCF dispatch thread.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @param flags Map containing the flags to parameterize the channel opening, or <code>null</code>.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	/* default */ void internalOpenChannel(final IPeer peer, final Map<String, Boolean> flags, final DoneOpenChannel done) {
		Assert.isNotNull(peer);
		Assert.isNotNull(done);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		// The channel instance to return
		IChannel channel = null;

		// Get the peer id
		final String id = peer.getID();

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
			CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_message, id, flags),
														0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
		}

		// Extract the flags of interest form the given flags map
		boolean forceNew = flags != null && flags.containsKey(IChannelManager.FLAG_FORCE_NEW) ? flags.get(IChannelManager.FLAG_FORCE_NEW).booleanValue() : false;
		boolean noValueAdd = flags != null && flags.containsKey(IChannelManager.FLAG_NO_VALUE_ADD) ? flags.get(IChannelManager.FLAG_NO_VALUE_ADD).booleanValue() : false;
		// If noValueAdd == true -> forceNew has to be true as well
		if (noValueAdd) forceNew = true;

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
																		0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, ChannelManager.this);
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
			// Wait for the channel to be fully opened if still in "OPENING" state
			if (channel.getState() == IChannel.STATE_OPENING) {
				final IChannel finChannel = channel;
				channel.addChannelListener(new IChannel.IChannelListener() {

					@Override
					public void onChannelOpened() {
						done.doneOpenChannel(null, finChannel);
					}

					@Override
					public void onChannelClosed(Throwable error) {
						done.doneOpenChannel(error, finChannel);
					}

					@Override
					public void congestionLevel(int level) {
					}
				});
			}
			else {
				done.doneOpenChannel(null, channel);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#openChannel(java.util.Map, java.util.Map, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)
	 */
	@Override
	public void openChannel(final Map<String, String> peerAttributes, final Map<String, Boolean> flags, final DoneOpenChannel done) {
		Runnable runnable = new Runnable() {
			@Override
            public void run() {
				internalOpenChannel(peerAttributes, flags, done);
			}
		};
		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeLater(runnable);
	}

	/**
	 * Internal implementation of {@link #openChannel(Map, org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel)}.
	 * <p>
	 * Method must be called within the TCF dispatch thread.
	 *
	 * @param peerAttributes The peer attributes. Must not be <code>null</code>.
	 * @param flags Map containing the flags to parameterize the channel opening, or <code>null</code>.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	/* default */ void internalOpenChannel(final Map<String, String> peerAttributes, final Map<String, Boolean> flags, final DoneOpenChannel done) {
		Assert.isNotNull(peerAttributes);
		Assert.isNotNull(done);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		// Call openChannel(IPeer, ...) instead of calling internalOpenChannel(IPeer, ...) directly
		// to include the value-add handling.
		openChannel(getOrCreatePeerInstance(peerAttributes), flags, done);
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
		Runnable runnable = new Runnable() {
			@Override
            public void run() {
				internalCloseChannel(channel);
			}
		};
		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeLater(runnable);
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

			// Clean the reference counter and the channel map
			refCounters.remove(id);
			channels.remove(id);
		} else {
			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_closeChannel_inuse_message, id, counter.toString()),
															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager#closeAll()
	 */
	@Override
	public void closeAll() {
		Runnable runnable = new Runnable() {
			@Override
            public void run() {
				internalCloseAll();
			}
		};
		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeLater(runnable);
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

	/**
	 * Client call back interface for internalHandleValueAdds(...).
	 */
	interface DoneHandleValueAdds {
		/**
		 * Called when all the value-adds are launched or the launched failed.
		 *
		 * @param error The error description if operation failed, <code>null</code> if succeeded.
		 * @param channel The channel object or <code>null</code>.
		 */
		void doneHandleValueAdds(Throwable error, IValueAdd[] valueAdds);
	}

	/**
	 * Check on the value-adds for the given peer. Launch the value-adds
	 * if necessary.
	 *
	 * @param id The peer id. Must not be <code>null</code>.
	 * @param peer The peer. Must not be <code>null</code>.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	/* default */ void internalHandleValueAdds(final IPeer peer, final Map<String, Boolean> flags, final DoneHandleValueAdds done) {
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(peer);
		Assert.isNotNull(done);

		// Get the peer id
		final String id = peer.getID();

		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
			CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_valueAdd_check, id),
														0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
		}

		// Extract the flags of interest form the given flags map
		boolean forceNew = flags != null && flags.containsKey(IChannelManager.FLAG_FORCE_NEW) ? flags.get(IChannelManager.FLAG_FORCE_NEW).booleanValue() : false;
		boolean noValueAdd = flags != null && flags.containsKey(IChannelManager.FLAG_NO_VALUE_ADD) ? flags.get(IChannelManager.FLAG_NO_VALUE_ADD).booleanValue() : false;
		// If noValueAdd == true -> forceNew has to be true as well
		if (noValueAdd) forceNew = true;

		// Check if there is already a channel opened to this peer
		IChannel channel = !forceNew ? channels.get(id) : null;
		if (channel != null && (channel.getState() == IChannel.STATE_OPEN || channel.getState() == IChannel.STATE_OPENING)) {
			// Got an existing channel -> drop out immediately
			done.doneHandleValueAdds(null, null);
			return;
		}

		// Do we have applicable value-add contributions
		final IValueAdd[] valueAdds = ValueAddManager.getInstance().getValueAdd(peer);
		if (valueAdds.length == 0) {
			// There are no applicable value-add's -> drop out immediately
			if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
				CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_valueAdd_noneApplicable, id),
															0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
			}
			done.doneHandleValueAdds(null, null);
			return;
		}

		// There are at least applicable value-add contributions
		if (CoreBundleActivator.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_CHANNEL_MANAGER)) {
			CoreBundleActivator.getTraceHandler().trace(NLS.bind(Messages.ChannelManager_openChannel_valueAdd_numApplicable, Integer.valueOf(valueAdds.length), id),
														0, ITraceIds.TRACE_CHANNEL_MANAGER, IStatus.INFO, this);
		}

		// Launching the value-add's may take a little while and must happen
		// outside of the TCF dispatch thread.
		ExecutorsUtil.execute(new Runnable() {
			@Override
			public void run() {
				doHandleValueAdds(id, valueAdds, done);
			}
		});
	}

	/**
	 * Tests all given value-add's to be alive and launch them if necessary.
	 *
	 * @param id The peer id. Must not be <code>null</code>.
	 * @param valueAdds The list of value-add's to check. Must not be <code>null</code>.
	 * @param done The client callback. Must not be <code>null</code>.
	 */
	/* default */ void doHandleValueAdds(final String id, final IValueAdd[] valueAdds, final DoneHandleValueAdds done) {
		Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(id);
		Assert.isNotNull(valueAdds);
		Assert.isNotNull(done);

		Throwable error = null;

		// Loop all applicable value-adds and check if there are up and running.
		// If not, trigger a launch of the value-add.
		for (IValueAdd valueAdd : valueAdds) {
			boolean alive = valueAdd.isAlive(id);
			// If the value-add is not alive, launch it
			if (!alive) {
				error = valueAdd.launch(id);
			}
			if (error != null) break;
		}

		// The callback must be invoked within the TCF dispatch thread
		final Throwable finError = error;
		Protocol.invokeLater(new Runnable() {
			@Override
			public void run() {
				done.doneHandleValueAdds(finError, valueAdds);
			}
		});
	}
}
