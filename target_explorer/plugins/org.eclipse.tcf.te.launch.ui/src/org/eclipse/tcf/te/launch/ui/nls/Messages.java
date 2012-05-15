/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.nls;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

/**
 * Launch UI Plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.launch.ui.nls.Messages"; //$NON-NLS-1$

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

	public static String LaunchSelectionManager_error_failedToDetermineElfType;

	public static String ContextSelectorControl_toolbar_refresh_tooltip;

	public static String LaunchConfigType_title;
	public static String LaunchConfigType_label;

	public static String LaunchContextSelectorTab_name;

	public static String ContextSelectorSection_title;
	public static String ContextSelectorSection_label;

	public static String RemoteContextSelectorControl_error_noContextSelected_single;
	public static String RemoteContextSelectorControl_error_noContextSelected_multi;

	public static String ReferencedProjectsTab_name;

	public static String ReferencedProjectsSection_title;
	public static String ReferencedProjectsSection_description;
	public static String ReferencedProjectsSection_name_column;
	public static String ReferencedProjectsSection_add_button;
	public static String ReferencedProjectsSection_delete_button;
	public static String ReferencedProjectsSection_up_button;
	public static String ReferencedProjectsSection_down_button;
	public static String ReferencedProjectsSection_addDialog_message;

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

	public static String LaunchEditorPage_title;

	public static String DeleteHandlerDelegate_question_title;
	public static String DeleteHandlerDelegate_question_message;

	public static String LaunchDialogHandler_dialog_title;
	public static String LaunchDialogHandler_dialog_message;
}
