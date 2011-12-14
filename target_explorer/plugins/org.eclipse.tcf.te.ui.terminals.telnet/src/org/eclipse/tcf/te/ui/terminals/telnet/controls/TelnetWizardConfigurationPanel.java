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
package org.eclipse.tcf.te.ui.terminals.telnet.controls;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tcf.te.ui.terminals.panels.AbstractConfigurationPanel;
import org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataWizardPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.telnet.NetworkPortMap;
import org.eclipse.tm.internal.terminal.telnet.TelnetConnector;
import org.eclipse.tm.internal.terminal.telnet.TelnetSettings;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * telnet wizard configuration panel implementation.
 */
@SuppressWarnings("restriction")
public class TelnetWizardConfigurationPanel extends AbstractConfigurationPanel implements ISharedDataWizardPage {

    private TelnetSettings telnetSettings;
	private ISettingsPage telnetSettingsPage;

	/**
	 * Constructor.
	 *
	 * @param parentControl The parent control. Must not be <code>null</code>!
	 */
	public TelnetWizardConfigurationPanel(BaseDialogPageControl parentControl) {
	    super(parentControl);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#setupPanel(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	public void setupPanel(Composite parent, FormToolkit toolkit) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		panel.setLayoutData(data);

		TelnetConnector conn = new TelnetConnector();
		telnetSettings = (TelnetSettings) conn.getTelnetSettings();
		telnetSettings.setHost(getSelectionHost());
		// MWE otherwise we don't get a valid default selection of the combo
		telnetSettings.setNetworkPort(NetworkPortMap.PROP_VALUETELNET);
		telnetSettingsPage = conn.makeSettingsPage();
		telnetSettingsPage.createControl(panel);

		setControl(panel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#dataChanged(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.swt.events.TypedEvent)
	 */
	@Override
	public boolean dataChanged(IPropertiesContainer data, TypedEvent e) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataWizardPage#setupData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
    public void setupData(IPropertiesContainer data) {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataWizardPage#extractData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
    public void extractData(IPropertiesContainer data) {
    	// set the terminal connector id for ssh
    	data.setProperty(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID, "org.eclipse.tm.internal.terminal.telnet.TelnetConnector"); //$NON-NLS-1$

    	// set the connector type for ssh
    	data.setProperty(ITerminalsConnectorConstants.PROP_CONNECTOR_TYPE_ID, "org.eclipse.tcf.te.ui.terminals.type.telnet"); //$NON-NLS-1$

    	telnetSettingsPage.saveSettings();
		data.setProperty(ITerminalsConnectorConstants.PROP_IP_HOST,telnetSettings.getHost());
		data.setProperty(ITerminalsConnectorConstants.PROP_IP_PORT, telnetSettings.getNetworkPort());
		data.setProperty(ITerminalsConnectorConstants.PROP_TIMEOUT, telnetSettings.getTimeout());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataWizardPage#initializeData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
    public void initializeData(IPropertiesContainer data) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataWizardPage#removeData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
    public void removeData(IPropertiesContainer data) {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		String host = getSelectionHost();
		if (host != null) {
			// is there a section for this host
			IDialogSettings hostSettings = settings.getSection(host);
			if (hostSettings != null) {
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_IP_HOST) != null) {
					telnetSettings.setHost(hostSettings.get(ITerminalsConnectorConstants.PROP_IP_HOST));
				}
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_IP_PORT) != null) {
					telnetSettings.setNetworkPort(hostSettings.get(ITerminalsConnectorConstants.PROP_IP_PORT));
				}
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_TIMEOUT) != null) {
					telnetSettings.setTimeout(hostSettings.get(ITerminalsConnectorConstants.PROP_TIMEOUT));
				}

				// set settings in page
				telnetSettingsPage.loadSettings();
			}
		}
		else {
			// MWE TODO combo for all hosts
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		// make sure the values are saved
		// actually not needed since this is done before in extractData
		telnetSettingsPage.saveSettings();

		String host = telnetSettings.getHost();
		IDialogSettings hostSection = settings.getSection(host);
		if (hostSection == null) {
			hostSection = settings.addNewSection(host);
		}
		hostSection.put(ITerminalsConnectorConstants.PROP_IP_HOST, telnetSettings.getHost());
		hostSection.put(ITerminalsConnectorConstants.PROP_IP_PORT, telnetSettings.getNetworkPort());
		hostSection.put(ITerminalsConnectorConstants.PROP_TIMEOUT, telnetSettings.getTimeout());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#isValid()
	 */
	@Override
    public boolean isValid(){
		return telnetSettingsPage.validateSettings();
	}
}
