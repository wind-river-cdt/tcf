/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.console.nls;

import org.eclipse.osgi.util.NLS;

/**
 * TCF UI Plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.tcf.ui.console.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String PageParticipant_command_remove_label;
	public static String PageParticipant_command_remove_tooltip;

	public static String PreferencesPage_label;
	public static String PreferencesPage_fieldEditor_fixedWidth;
	public static String PreferencesPage_fieldEditor_width;
	public static String PreferencesPage_fieldEditor_limitOutput;
	public static String PreferencesPage_fieldEditor_bufferSize;
	public static String PreferencesPage_fieldEditor_showOnOutput;

	public static String PreferencesPage_group_colorSettings;
	public static String PreferencesPage_color_command;
	public static String PreferencesPage_color_commandResponse;
	public static String PreferencesPage_color_event;
	public static String PreferencesPage_color_progress;
	public static String PreferencesPage_color_error;
}
