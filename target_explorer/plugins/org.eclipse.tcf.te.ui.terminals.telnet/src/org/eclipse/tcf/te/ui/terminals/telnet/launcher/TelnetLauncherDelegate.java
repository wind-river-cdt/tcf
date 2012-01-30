/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [366374] [TERMINALS][TELNET] Add Telnet terminal support
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.telnet.launcher;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.services.interfaces.ITerminalService;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tcf.te.ui.terminals.interfaces.IConfigurationPanel;
import org.eclipse.tcf.te.ui.terminals.launcher.AbstractLauncherDelegate;
import org.eclipse.tcf.te.ui.terminals.telnet.controls.TelnetWizardConfigurationPanel;
import org.eclipse.tcf.te.ui.terminals.telnet.nls.Messages;

/**
 * SSH launcher delegate implementation.
 */
public class TelnetLauncherDelegate extends AbstractLauncherDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.ILauncherDelegate#needsUserConfiguration()
	 */
	@Override
	public boolean needsUserConfiguration() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.ILauncherDelegate#getPanel(org.eclipse.tcf.te.ui.controls.BaseDialogPageControl)
	 */
	@Override
	public IConfigurationPanel getPanel(BaseDialogPageControl parentControl) {
		return new TelnetWizardConfigurationPanel(parentControl);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.ILauncherDelegate#execute(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void execute(IPropertiesContainer properties, ICallback callback) {
		// Set the terminal tab title
		String terminalTitle = getTerminalTitle(properties);
		if (terminalTitle != null) {
			properties.setProperty(ITerminalsConnectorConstants.PROP_TITLE, terminalTitle);
		}

		// Get the terminal service
		ITerminalService terminal = ServiceManager.getInstance().getService(ITerminalService.class);
		// If not available, we cannot fulfill this request
		if (terminal != null) {
			terminal.openConsole(properties, callback);
		}
	}

	/**
	 * Returns the terminal title string.
	 * <p>
	 * The default implementation constructs a title like &quot;SSH @ host (Start time) &quot;.
	 *
	 * @return The terminal title string or <code>null</code>.
	 */
	private String getTerminalTitle(IPropertiesContainer properties) {
		String host = properties.getStringProperty(ITerminalsConnectorConstants.PROP_IP_HOST);

		if (host != null) {
			DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			String date = format.format(new Date(System.currentTimeMillis()));
			return NLS.bind(Messages.TelnetLauncherDelegate_terminalTitle, new String[]{host, date});
		}
		return Messages.TelnetLauncherDelegate_terminalTitle_default;
	}
}
