/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.va;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;

/**
 * Abstract external value add implementation.
 */
public abstract class AbstractExternalValueAdd extends AbstractValueAdd {
	// The per peer id value add entry map
	private final Map<String, ValueAddEntry> entries = new HashMap<String, ValueAddEntry>();

	/**
	 * Class representing a value add entry
	 */
	protected static class ValueAddEntry {
		public Process process;
		public IPeer peer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.va.interfaces.IValueAdd#isAlive(java.lang.String)
	 */
	@Override
	public boolean isAlive(final String id) {
		Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(id);

		boolean alive = false;

		// Query the associated entry
		final ValueAddEntry entry = entries.get(id);
		if (entry != null) {
			// Check if the process is still alive or has auto-exited already
			boolean exited = false;

			if (entry.process != null) {
				Assert.isNotNull(entry.peer);

				try {
					entry.process.exitValue();
					exited = true;
				} catch (IllegalThreadStateException e) {
					/* ignored on purpose */
				}
			}

			// If the process is still running, try to open a channel
			if (!exited) {
				final IChannel channel = entry.peer.openChannel();
				Protocol.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						channel.addChannelListener(new IChannel.IChannelListener() {

							@Override
							public void onChannelOpened() {
								synchronized(entry) {
									entry.notifyAll();
								}
							}

							@Override
							public void onChannelClosed(Throwable error) {
								synchronized(entry) {
									entry.notifyAll();
								}
							}

							@Override
							public void congestionLevel(int level) {
							}
						});
					}
				});

				// Wait until the channel opening completed
				synchronized(entry) {
					try {
	                    entry.wait();
                    } catch (InterruptedException e) {
	                    /* ignored on purpose */
                    }
				}

				// Check if the channel got successfully opened
				if (channel.getState() == IChannel.STATE_OPEN) {
					alive = true;
					// Close the channel, it is not longer needed
					channel.close();
				}
			}

			// If the external value-add is not longer alive, clean up
			if (!alive) {
				entries.remove(id);
				if (!exited) {
					entry.process.destroy();
				}
			}
		}

		return alive;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.va.interfaces.IValueAdd#launch(java.lang.String)
	 */
	@Override
	public Throwable launch(String id) {
		Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(id);

	    return null;
	}
}
