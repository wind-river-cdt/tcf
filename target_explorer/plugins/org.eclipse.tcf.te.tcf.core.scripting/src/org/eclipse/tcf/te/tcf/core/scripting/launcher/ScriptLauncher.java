/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.scripting.launcher;

import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.core.Command;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.IService;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.core.scripting.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.core.scripting.interfaces.IScriptLauncher;
import org.eclipse.tcf.te.tcf.core.scripting.interfaces.IScriptLauncherProperties;
import org.eclipse.tcf.te.tcf.core.scripting.nls.Messages;
import org.eclipse.tcf.te.tcf.core.scripting.parser.Parser;
import org.eclipse.tcf.te.tcf.core.scripting.parser.Token;

/**
 * Script launcher implementation.
 */
public class ScriptLauncher extends PlatformObject implements IScriptLauncher {
	// The channel instance
	/* default */ IChannel channel;
	// The process properties instance
	private IPropertiesContainer properties;

	// The callback instance
	private ICallback callback;

	/**
     * Constructor.
     */
    public ScriptLauncher() {
    	super();
    }

	/* (non-Javadoc)
     * @see org.eclipse.tcf.te.tcf.core.scripting.interfaces.IScriptLauncher#dispose()
     */
    @Override
    public void dispose() {
		// Store a final reference to the channel instance
		final IChannel finChannel = channel;

		// Close the channel as all disposal is done
		if (finChannel != null) Tcf.getChannelManager().closeChannel(finChannel);

		// Dissociate the channel
		channel = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.tcf.core.scripting.interfaces.IScriptLauncher#launch(org.eclipse.tcf.protocol.IPeer, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
     */
    @Override
    public void launch(final IPeer peer, final IPropertiesContainer properties, final ICallback callback) {
    	Assert.isNotNull(peer);
    	Assert.isNotNull(properties);

		// Normalize the callback
		if (callback == null) {
			this.callback = new Callback() {
				/* (non-Javadoc)
				 * @see org.eclipse.tcf.te.runtime.callback.Callback#internalDone(java.lang.Object, org.eclipse.core.runtime.IStatus)
				 */
				@Override
				public void internalDone(Object caller, IStatus status) {
				}
			};
		}
		else {
			this.callback = callback;
		}

		// Remember the process properties
		this.properties = properties;

		// Open a dedicated channel to the given peer
		Tcf.getChannelManager().openChannel(peer, true, new IChannelManager.DoneOpenChannel() {
			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel#doneOpenChannel(java.lang.Throwable, org.eclipse.tcf.protocol.IChannel)
			 */
			@Override
			public void doneOpenChannel(Throwable error, IChannel channel) {
				if (error == null) {
					ScriptLauncher.this.channel = channel;

					// Attach a channel listener so we can dispose ourself if the channel
					// is closed from the remote side.
					channel.addChannelListener(new IChannelListener() {
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
							if (error != null) {
								IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
															NLS.bind(Messages.ScriptLauncher_error_channelConnectFailed, peer.getID(), error.getLocalizedMessage()),
															error);
								invokeCallback(status, null);
							}
						}
						/* (non-Javadoc)
						 * @see org.eclipse.tcf.protocol.IChannel.IChannelListener#congestionLevel(int)
						 */
						@Override
						public void congestionLevel(int level) {
						}
					});

					// Check if the channel is in connected state
					if (channel.getState() != IChannel.STATE_OPEN) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
													Messages.ScriptLauncher_error_channelNotConnected,
													new IllegalStateException());
						invokeCallback(status, null);
						return;
					}

					// Do some very basic sanity checking on the script properties
					if (properties.getStringProperty(IScriptLauncherProperties.PROP_SCRIPT) == null) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
													Messages.ScriptLauncher_error_missingScript,
													new IllegalArgumentException(IScriptLauncherProperties.PROP_SCRIPT));
						invokeCallback(status, null);
						return;
					}

					// Execute the launch now
					executeLaunch();
				} else {
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
												NLS.bind(Messages.ScriptLauncher_error_channelConnectFailed, peer.getID(), error.getLocalizedMessage()),
												error);
					invokeCallback(status, null);
				}
			}
		});
    }

	/**
	 * Executes the script launch.
	 */
    protected void executeLaunch() {
		// Get the script properties container
		final IPropertiesContainer properties = getProperties();
		if (properties == null) {
			// This is an illegal argument. Properties must be set
			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										NLS.bind(Messages.ScriptLauncher_error_illegalNullArgument, "properties"), //$NON-NLS-1$
										new IllegalArgumentException());
			invokeCallback(status, null);
			return;
		}

		// Get the script to execute
		String script = properties.getStringProperty(IScriptLauncherProperties.PROP_SCRIPT);
		if (script == null || "".equals(script.trim())) { //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										Messages.ScriptLauncher_error_missingScript,
										new IllegalArgumentException(IScriptLauncherProperties.PROP_SCRIPT));
			invokeCallback(status, null);
			return;
		}

		// Create the script parser instance
		Parser parser = new Parser(script);
		try {
			// Parse the script
			Token[] tokens = parser.parse();

			// And execute the tokens extracted, one by one sequentially
			if (tokens != null && tokens.length > 0) {
				executeToken(tokens, 0);
			} else {
				invokeCallback(Status.OK_STATUS, null);
				return;
			}
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										NLS.bind(Messages.ScriptLauncher_error_parsingScript, e.getLocalizedMessage()),
										e);
			invokeCallback(status, null);
			return;
		}
	}

	/**
	 * Executes the token at the given index.
	 *
	 * @param tokens The tokens. Must not be <code>null</code>.
	 * @param index The index.
	 */
	@SuppressWarnings("unused")
	protected void executeToken(final Token[] tokens, final int index) {
		Assert.isNotNull(tokens);

		if (index < 0 || index >= tokens.length) {
			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										NLS.bind(Messages.ScriptLauncher_error_illegalIndex, Integer.valueOf(index)),
										new IllegalArgumentException("index")); //$NON-NLS-1$
			invokeCallback(status, null);
			return;
		}

		Token token = tokens[index];

		IService service = channel.getRemoteService(token.getServiceName());
		new Command(channel, service, token.getCommandName(), token.getArguments()) {

			@Override
			public void done(Exception error, Object[] args) {
				if (error == null) {
					// Execute the next token
					int nextIndex = index + 1;
					if (nextIndex == tokens.length) {
						// All tokens executed
						invokeCallback(Status.OK_STATUS, null);
					} else {
						executeToken(tokens, nextIndex);
					}
				} else {
					// Stop the execution
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
									NLS.bind(Messages.ScriptLauncher_error_parsingScript, error.getLocalizedMessage()),
									error);
					invokeCallback(status, null);
				}
			}
		};
	}

	/**
	 * Invoke the callback with the given parameters. If the given status severity
	 * is {@link IStatus#ERROR}, the process launcher object is disposed automatically.
	 *
	 * @param status The status. Must not be <code>null</code>.
	 * @param result The result object or <code>null</code>.
	 */
	protected void invokeCallback(IStatus status, Object result) {
		// Dispose the process launcher if we report an error
		if (status.getSeverity() == IStatus.ERROR) {
			dispose();
		}

		// Invoke the callback
		ICallback callback = getCallback();
		if (callback != null) {
			callback.setResult(result);
			callback.done(this, status);
		}
	}

	/**
	 * Returns the channel instance.
	 *
	 * @return The channel instance or <code>null</code> if none.
	 */
	public final IChannel getChannel() {
		return channel;
	}

	/**
	 * Returns the process properties container.
	 *
	 * @return The process properties container or <code>null</code> if none.
	 */
	public final IPropertiesContainer getProperties() {
		return properties;
	}

	/**
	 * Returns the callback instance.
	 *
	 * @return The callback instance or <code>null</code> if none.
	 */
	protected final ICallback getCallback() {
		return callback;
	}

}
