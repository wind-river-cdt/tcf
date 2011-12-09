/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.controls.nls;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

/**
 * Common Controls plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.ui.controls.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	/**
	 * Returns if or if not this NLS manager contains a constant for
	 * the given externalized strings key.
	 *
	 * @param key The externalized strings key or <code>null</code>.
	 * @return <code>True</code> if a constant for the given key exists, <code>false</code> otherwise.
	 */
	public static boolean hasString(String key) {
		if (key != null) {
			try {
				Field field = Messages.class.getDeclaredField(key);
				return field != null;
			} catch (NoSuchFieldException e) { /* ignored on purpose */ }
		}

		return false;
	}

	/**
	 * Returns the corresponding string for the given externalized strings
	 * key or <code>null</code> if the key does not exist.
	 *
	 * @param key The externalized strings key or <code>null</code>.
	 * @return The corresponding string or <code>null</code>.
	 */
	public static String getString(String key) {
		if (key != null) {
			try {
				Field field = Messages.class.getDeclaredField(key);
				if (field != null) {
					return (String)field.get(null);
				}
			} catch (Exception e) { /* ignored on purpose */ }
		}

		return null;
	}

	// **** Declare externalized string id's down here *****

	public static String BaseEditBrowseTextControl_button_label;
	public static String BaseEditBrowseTextControl_validationJob_name;

	public static String NameControl_label;
	public static String NameControl_info_missingValue;

	public static String DirectorySelectionControl_title;
	public static String DirectorySelectionControl_group_label;
	public static String DirectorySelectionControl_editfield_label;

	public static String FileSelectionControl_title_open;
	public static String FileSelectionControl_title_save;
	public static String FileSelectionControl_group_label;
	public static String FileSelectionControl_editfield_label;

	public static String RemoteHostAddressControl_label;
	public static String RemoteHostAddressControl_button_label;
	public static String RemoteHostAddressControl_information_checkNameAddressUserInformation;
	public static String RemoteHostAddressControl_information_checkNameAddressField;
	public static String RemoteHostAddressControl_information_checkNameAddressFieldOk;
	public static String RemoteHostAddressControl_information_missingTargetNameAddress;
	public static String RemoteHostAddressControl_error_invalidTargetNameAddress;
	public static String RemoteHostAddressControl_error_invalidTargetIpAddress;
	public static String RemoteHostAddressControl_error_targetNameNotResolveable;

	public static String RemoteHostPortControl_label;

	public static String WireTypeControl_label;
	public static String WireTypeControl_networkType_label;
	public static String WireTypeControl_serialType_label;

	public static String NetworkCablePanel_section;
	public static String NetworkCablePanel_addressControl_label;

	public static String SerialLinePanel_section;
	public static String SerialLinePanel_hostTTYDevice_label;
	public static String SerialLinePanel_hostTTYSpeed_label;
	public static String SerialLinePanel_hostTTYDatabits_label;
	public static String SerialLinePanel_hostTTYParity_label;
	public static String SerialLinePanel_hostTTYStopbits_label;
	public static String SerialLinePanel_hostTTYFlowControl_label;
	public static String SerialLinePanel_customSerialDevice_title;
	public static String SerialLinePanel_customSerialDevice_message;
	public static String SerialLinePanel_customSerialBaudRate_title;
	public static String SerialLinePanel_customSerialBaudRate_message;
	public static String SerialLinePanel_error_invalidCharactes;
	public static String SerialLinePanel_error_invalidCharactesBaudRate;
	public static String SerialLinePanel_error_emptyHostTTYDevice;
	public static String SerialLinePanel_error_emptyHostTTYSpeedRate;
	public static String SerialLinePanel_error_emptyHostTTYDatabits;
	public static String SerialLinePanel_error_emptyHostTTYParity;
	public static String SerialLinePanel_error_emptyHostTTYStopbits;
	public static String SerialLinePanel_error_emptyHostTTYFlowControl;
	public static String SerialLinePanel_info_editableTTYDeviceSelected;
	public static String SerialLinePanel_info_editableTTYBaudRateSelected;
	public static String SerialLinePanel_warning_FailedToLoadSerialPorts;
	public static String SerialLinePanel_task_queryAvailableSerialDevices;

	public static String SerialPortAddressDialog_port;
	public static String SerialPortAddressDialog_Information_MissingPort;
	public static String SerialPortAddressDialog_Error_InvalidPort;
	public static String SerialPortAddressDialog_Error_InvalidPortRange;

	public static String NameOrIPValidator_Information_MissingNameOrIP;
	public static String NameOrIPValidator_Information_MissingName;
	public static String NameOrIPValidator_Information_MissingIP;
	public static String NameOrIPValidator_Information_CheckName;
	public static String NameOrIPValidator_Error_InvalidNameOrIP;
	public static String NameOrIPValidator_Error_InvalidName;
	public static String NameOrIPValidator_Error_InvalidIP;

	public static String PortNumberValidator_Information_MissingPortNumber;
	public static String PortNumberValidator_Error_InvalidPortNumber;
	public static String PortNumberValidator_Error_PortNumberNotInRange;

	public static String FileNameValidator_Information_MissingName;
	public static String FileNameValidator_Error_InvalidName;
	public static String FileNameValidator_Error_IsDirectory;
	public static String FileNameValidator_Error_MustExist;
	public static String FileNameValidator_Error_ReadOnly;
	public static String FileNameValidator_Error_NoAccess;
	public static String FileNameValidator_Error_IsRelativ;
	public static String FileNameValidator_Error_IsAbsolut;
	public static String FileNameValidator_Error_HasSpaces;

	public static String DirectoryNameValidator_Information_MissingName;
	public static String DirectoryNameValidator_Error_IsFile;
	public static String DirectoryNameValidator_Error_MustExist;
	public static String DirectoryNameValidator_Error_ReadOnly;
	public static String DirectoryNameValidator_Error_NoAccess;
	public static String DirectoryNameValidator_Error_IsRelativ;
	public static String DirectoryNameValidator_Error_IsAbsolut;

	public static String RegexValidator_Information_MissingValue;
	public static String RegexValidator_Error_InvalidValue;

	public static String HexValidator_Error_InvalidValueRange;

	public static String WorkspaceContainerValidator_Information_MissingValue;
	public static String WorkspaceContainerValidator_Error_InvalidValue;

	public static String NumberValidator_Error_InvalidRange;
}
