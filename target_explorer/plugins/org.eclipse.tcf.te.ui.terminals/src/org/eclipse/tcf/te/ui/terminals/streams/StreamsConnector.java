/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.streams;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.runtime.interfaces.IDisposable;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

/**
 * Streams connector implementation.
 */
@SuppressWarnings("restriction")
public class StreamsConnector extends AbstractStreamsConnector implements IDisposable {
	// Reference to the streams settings
	private final StreamsSettings settings;

	/**
	 * Constructor.
	 */
	public StreamsConnector() {
		this(new StreamsSettings());
	}

	/**
	 * Constructor.
	 *
	 * @param settings The streams settings. Must not be <code>null</code>
	 */
	public StreamsConnector(StreamsSettings settings) {
		super();

		Assert.isNotNull(settings);
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.terminal.provisional.api.provider.TerminalConnectorImpl#connect(org.eclipse.tcf.internal.terminal.provisional.api.ITerminalControl)
	 */
	@Override
	public void connect(ITerminalControl control) {
		Assert.isNotNull(control);
		super.connect(control);

		// connect the streams
		connectStreams(control, settings.getStdinStream(), settings.getStdoutStream(), settings.getStderrStream(), settings.isLocalEcho(), settings.getLineSeparator());

		// Set the terminal control state to CONNECTED
		control.setState(TerminalState.CONNECTED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.terminal.provisional.api.provider.TerminalConnectorImpl#isLocalEcho()
	 */
	@Override
	public boolean isLocalEcho() {
		return settings.isLocalEcho();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		disconnect();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.terminal.provisional.api.provider.TerminalConnectorImpl#doDisconnect()
	 */
	@Override
	public void doDisconnect() {
		// Dispose the streams
		super.doDisconnect();

		// Set the terminal control state to CLOSED.
		fControl.setState(TerminalState.CLOSED);
	}

	// ***** Process Connector settings handling *****

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.terminal.provisional.api.provider.TerminalConnectorImpl#getSettingsSummary()
	 */
	@Override
	public String getSettingsSummary() {
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.terminal.provisional.api.provider.TerminalConnectorImpl#load(org.eclipse.tcf.internal.terminal.provisional.api.ISettingsStore)
	 */
	@Override
	public void load(ISettingsStore store) {
		settings.load(store);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.terminal.provisional.api.provider.TerminalConnectorImpl#save(org.eclipse.tcf.internal.terminal.provisional.api.ISettingsStore)
	 */
	@Override
	public void save(ISettingsStore store) {
		settings.save(store);
	}
}
