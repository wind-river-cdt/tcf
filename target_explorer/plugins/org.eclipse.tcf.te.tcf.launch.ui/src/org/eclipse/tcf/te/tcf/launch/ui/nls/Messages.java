/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.ui.nls;

import org.eclipse.osgi.util.NLS;

/**
 * TCF Launch UI Plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.tcf.launch.ui.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String LaunchConfigurationMainTabSection_title;
	public static String LaunchConfigurationMainTabSection_processArguments_label;

	public static String ProcessImageSelectorControl_label;
	public static String ProcessImageSelectorControl_error_missingProcessImage;

	public static String FileTransferTab_name;

	public static String FileTransferSection_title;
	public static String FileTransferSection_description;
	public static String FileTransferSection_host_column;
	public static String FileTransferSection_target_column;
	public static String FileTransferSection_options_column;
	public static String FileTransferSection_add_button;
	public static String FileTransferSection_edit_button;
	public static String FileTransferSection_delete_button;
	public static String FileTransferSection_up_button;
	public static String FileTransferSection_down_button;
	public static String FileTransferSection_toHost_text;
	public static String FileTransferSection_toTarget_text;
	public static String FileTransferSection_toHost_tooltip;
	public static String FileTransferSection_toTarget_tooltip;

	public static String ReferencedProjectsTab_name;

	public static String ReferencedProjectsSection_title;
	public static String ReferencedProjectsSection_description;
	public static String ReferencedProjectsSection_project_column;
	public static String ReferencedProjectsSection_add_button;
	public static String ReferencedProjectsSection_delete_button;
	public static String ReferencedProjectsSection_up_button;
	public static String ReferencedProjectsSection_down_button;
	public static String ReferencedProjectsSection_addDialog_message;

	public static String AddEditFileTransferDialog_add_dialogTitle;
	public static String AddEditFileTransferDialog_edit_dialogTitle;
	public static String AddEditFileTransferDialog_add_title;
	public static String AddEditFileTransferDialog_edit_title;
	public static String AddEditFileTransferDialog_add_message;
	public static String AddEditFileTransferDialog_edit_message;
	public static String AddEditFileTransferDialog_target_label;
	public static String AddEditFileTransferDialog_host_label;
	public static String AddEditFileTransferDialog_options_label;
	public static String AddEditFileTransferDialog_toHost_checkbox;
	public static String AddEditFileTransferDialog_toTarget_checkbox;
}
