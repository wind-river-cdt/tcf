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
import org.eclipse.tcf.core.AbstractChannel;
import org.eclipse.tcf.core.AbstractChannel.TraceListener;
import org.eclipse.tcf.core.Command;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.IService;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.events.EventManager;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.core.scripting.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.core.scripting.events.ScriptEvent;
import org.eclipse.tcf.te.tcf.core.scripting.interfaces.IScriptLauncher;
import org.eclipse.tcf.te.tcf.core.scripting.interfaces.IScriptLauncherProperties;
import org.eclipse.tcf.te.tcf.core.scripting.nls.Messages;
import org.eclipse.tcf.te.tcf.core.scripting.parser.Parser;
import org.eclipse.tcf.te.tcf.core.scripting.parser.Token;
import org.eclipse.tcf.te.tcf.core.utils.JSONUtils;

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

	// The channel trace listener instance
	/* default */ TraceListener traceListener;

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
    	if (channel != null) {
    		// Remove the trace listener
    		if (traceListener != null) { ((AbstractChannel)channel).removeTraceListener(traceListener); traceListener = null; }

    		// Close the channel as all disposal is done
    		Tcf.getChannelManager().closeChannel(channel);

			// Fire the stop event
			ScriptEvent event = new ScriptEvent(ScriptLauncher.this, ScriptEvent.Type.STOP, null);
			EventManager.getInstance().fireEvent(event);

    		// Dissociate the channel
    		channel = null;
    	}
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

					// Fire the start event
					ScriptEvent event = new ScriptEvent(ScriptLauncher.this, ScriptEvent.Type.START, null);
					EventManager.getInstance().fireEvent(event);

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
							if (traceListener != null) { ((AbstractChannel)ScriptLauncher.this.channel).removeTraceListener(traceListener); traceListener = null; }
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

					// Create the trace listener instance
					traceListener = new TraceListener() {

						@Override
						public void onMessageSent(char type, String token, String service, String name, byte[] data) {
							if (isFiltered(type, name)) return;
							String message = formatMessage(type, token, service, name, data, false);
							ScriptEvent event = new ScriptEvent(ScriptLauncher.this, ScriptEvent.Type.OUTPUT, new ScriptEvent.Message(type, message));
							EventManager.getInstance().fireEvent(event);
						}

						@Override
						public void onMessageReceived(char type, String token, String service, String name, byte[] data) {
							if (isFiltered(type, name)) return;
							String message = formatMessage(type, token, service, name, data, true);
							ScriptEvent event = new ScriptEvent(ScriptLauncher.this, ScriptEvent.Type.OUTPUT, new ScriptEvent.Message(type, message));
							EventManager.getInstance().fireEvent(event);
						}

						@Override
						public void onChannelClosed(Throwable error) {
						}

						/**
						 * Checks if a given message is filtered. Filtered messages are not send to
						 * the script output console.
						 *
						 * @param type The message type.
						 * @param name The message name.
						 *
						 * @return <code>True</code> if the message is filtered, <code>false</code> otherwise.
						 */
						private boolean isFiltered(char type, String name) {
							boolean filtered = false;

							// Filter out the heart beat and framework messages
							if (type == 'F' || (name != null && name.toLowerCase().contains("heartbeat"))) { //$NON-NLS-1$
								filtered = true;
							}

							return filtered;
						}

						/**
						 * Format the trace message.
						 */
						protected String formatMessage(char type, String token, String service, String name, byte[] data, boolean received) {
							// Decode the arguments again for tracing purpose
							String args = JSONUtils.decodeStringFromByteArray(data);

							// Construct the full message
							//
							// The message format is: [<---|--->] <type> <token> <service>#<name> <args>
							StringBuilder message = new StringBuilder();
							message.append(received ? "<---" : "--->"); //$NON-NLS-1$ //$NON-NLS-2$
							message.append(" ").append(Character.valueOf(type)); //$NON-NLS-1$
							if (token != null) message.append(" ").append(token); //$NON-NLS-1$
							if (service != null) message.append(" ").append(service); //$NON-NLS-1$
							if (name != null) message.append(" ").append(name); //$NON-NLS-1$
							if (args != null && args.trim().length() > 0) message.append(" ").append(args.trim()); //$NON-NLS-1$

							return message.toString();
						}
					};

					// Register the trace listener
					((AbstractChannel)channel).addTraceListener(traceListener);

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
		if (service != null) {
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
		} else {
			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										NLS.bind(Messages.ScriptLauncher_error_serviceNotAvailable, token.getServiceName(), channel.getRemotePeer().getID()),
										null);
			invokeCallback(status, null);
		}
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
