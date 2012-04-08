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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
import org.eclipse.tcf.te.runtime.utils.net.IPAddressUtil;
import org.eclipse.tcf.te.tcf.core.nls.Messages;

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
	 * @see org.eclipse.tcf.te.tcf.core.va.interfaces.IValueAdd#getPeer(java.lang.String)
	 */
	@Override
	public IPeer getPeer(String id) {
		Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(id);

		IPeer peer = null;

		ValueAddEntry entry = entries.get(id);
		if (entry != null) {
			peer = entry.peer;
		}

	    return peer;
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
	@SuppressWarnings("unchecked")
    @Override
	public Throwable launch(final String id) {
		Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(id);

		Throwable error = null;

		// Get the location of the executable image
		IPath path = getLocation();
		if (path != null && path.toFile().canRead()) {
			ValueAddLauncher launcher = new ValueAddLauncher(path);
			try {
				launcher.launch();
			} catch (Throwable e) {
				error = e;
			}

			// Prepare the value-add entry
			ValueAddEntry entry = new ValueAddEntry();

			if (error == null) {
				// Get the external process
				Process process = launcher.getProcess();
				try {
					// Check if the process exited right after the launch
					int exitCode = process.exitValue();
					// Died -> Fail the launch
					error = new IOException("Value-add process died with exit code " + exitCode); //$NON-NLS-1$
				} catch (IllegalThreadStateException e) {
					// Still running -> Associate the process with the entry
					entry.process = process;
				}
			}

			String output = null;

			if (error == null) {
				// The agent is started with "-S" to write out the peer attributes in JSON format.
				int counter = 10;
				while (counter > 0 && output == null) {
					// Try to read in the output
					output = launcher.getOutputReader().getOutput();
					if ("".equals(output)) { //$NON-NLS-1$
						output = null;
						ExecutorsUtil.waitAndExecute(200, null);
					}
					counter--;
				}
				if (output == null) {
					error = new IOException("Failed to read output from value-add."); //$NON-NLS-1$
				}
			}

			 Map<String, String> attrs = null;

			if (error == null) {
				// Strip away "Server-Properties:"
				output = output.replace("Server-Properties:", " "); //$NON-NLS-1$ //$NON-NLS-2$
				output = output.trim();

				// Read into an object
				Object object = null;
				try {
					object = JSON.parseOne(output.getBytes("UTF-8")); //$NON-NLS-1$
			        attrs = new HashMap<String, String>((Map<String, String>)object);
				} catch (IOException e) {
					error = e;
				}
			}

			if (error == null) {
				// Construct the peer id from peer attributes

				// The expected peer id is "<transport>:<canonical IP>:<port>"
				String transport = attrs.get(IPeer.ATTR_TRANSPORT_NAME);
				String port = attrs.get(IPeer.ATTR_IP_PORT);
				String ip = IPAddressUtil.getInstance().getCanonicalAddress();

				if (transport != null && ip != null && port != null) {
					String peerId = transport + ":" + ip + ":" + port; //$NON-NLS-1$ //$NON-NLS-2$
					attrs.put(IPeer.ATTR_ID, peerId);
					attrs.put(IPeer.ATTR_IP_HOST, ip);

					entry.peer = new TransientPeer(attrs);
				} else {
					error = new IOException("Invalid or incomplete peer attributes reported by value-add."); //$NON-NLS-1$
				}
			}

			if (error == null) {
				Assert.isNotNull(entry.process);
				Assert.isNotNull(entry.peer);

				entries.put(id, entry);
			}
		} else {
			error = new FileNotFoundException(NLS.bind(Messages.AbstractExternalValueAdd_error_invalidLocation, this.getId()));
		}

	    return error;
	}

	/**
	 * Returns the absolute path to the value-add executable image.
	 *
	 * @return The absolute path or <code>null</code> if not found.
	 */
	protected abstract IPath getLocation();

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.va.interfaces.IValueAdd#shutdown(java.lang.String)
	 */
	@Override
	public Throwable shutdown(String id) {
		Assert.isTrue(!Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(id);

		Throwable error = null;

		final ValueAddEntry entry = entries.get(id);
		if (entry != null) {
			if (isAlive(id)) {
				entries.remove(id);
				entry.process.destroy();
			}
		}

		return error;
	}
}
