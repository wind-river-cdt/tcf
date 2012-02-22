/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.serial.types;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.ui.terminals.internal.SettingsStore;
import org.eclipse.tcf.te.ui.terminals.types.AbstractConnectorType;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.internal.terminal.serial.SerialSettings;

/**
 * Serial terminal connector type implementation.
 */
@SuppressWarnings("restriction")
public class SerialConnectorType extends AbstractConnectorType {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConnectorType#createTerminalConnector(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
    @Override
	public ITerminalConnector createTerminalConnector(IPropertiesContainer properties) {
    	Assert.isNotNull(properties);

    	// Check for the terminal connector id
    	String connectorId = properties.getStringProperty(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);

		String port = properties.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_DEVICE);
		String baud = properties.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_BAUD_RATE);
		String timeout = properties.getStringProperty(ITerminalsConnectorConstants.PROP_TIMEOUT);
		String databits = properties.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_DATA_BITS);
		String stopbits = properties.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_STOP_BITS);
		String parity = properties.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_PARITY);
		String flowcontrol = properties.getStringProperty(ITerminalsConnectorConstants.PROP_SERIAL_FLOW_CONTROL);

		return createSerialConnector(connectorId, new String[] { port, baud, timeout, databits, stopbits, parity, flowcontrol }, 0);
	}

	/**
	 * Creates a terminal connector object based on the given serial attributes.
	 * <p>
	 *
	 * @param connectorId The terminal connector id or <code>null</code>.
	 * @param attributes The serial attributes. Must not be <code>null</code> and must have at least two elements.
	 * @param portOffset Offset to add to the port.
	 *
	 * @return The terminal connector object instance or <code>null</code>.
	 */
	protected ITerminalConnector createSerialConnector(String connectorId, String[] attributes, int portOffset) {
		Assert.isNotNull(attributes);

		if (connectorId == null) connectorId = "org.eclipse.tm.internal.terminal.serial.SerialConnector"; //$NON-NLS-1$

		final String port = attributes[0];
		final String baud = attributes[1];
		final String timeout = attributes[2];
		final String databits = attributes[3];
		final String stopbits = attributes[4];
		final String parity = attributes[5];
		final String flowcontrol = attributes[6];

		// Construct the terminal settings store
		ISettingsStore store = new SettingsStore();

		// Construct the serial settings
		SerialSettings serialSettings = new SerialSettings();
		serialSettings.setSerialPort(port);
		serialSettings.setBaudRate(baud);
		serialSettings.setTimeout(timeout);
		serialSettings.setDataBits(databits);
		serialSettings.setStopBits(stopbits);
		serialSettings.setParity(parity);
		serialSettings.setFlowControl(flowcontrol);

		// And save the settings to the store
		serialSettings.save(store);

		// Construct the terminal connector instance
		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector(connectorId);
		if (connector != null) {
			// Apply default settings
			connector.makeSettingsPage();
			// And load the real settings
			connector.load(store);
		}

		return connector;
	}
}
