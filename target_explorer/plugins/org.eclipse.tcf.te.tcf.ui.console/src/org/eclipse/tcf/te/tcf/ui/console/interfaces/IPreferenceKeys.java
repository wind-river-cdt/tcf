/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.console.interfaces;

/**
 * Console preference key identifiers.
 */
public interface IPreferenceKeys {
	/**
	 * Common prefix for all UI preference keys
	 */
	public final String PREFIX = "te.tcf.ui.console."; //$NON-NLS-1$

	/**
	 * Limit console output to a fixed width.
	 */
	public final String PREF_CONSOLE_FIXED_WIDTH = PREFIX + "pref.fixedWidth"; //$NON-NLS-1$

	/**
	 * The fixed width size in characters.
	 */
	public final String PREF_CONSOLE_WIDTH = PREFIX + "pref.width"; //$NON-NLS-1$

	/**
	 * Limit console output to a maximum content.
	 */
	public final String PREF_CONSOLE_LIMIT_OUTPUT = PREFIX + "pref.limitOutput"; //$NON-NLS-1$

	/**
	 * The maximum content size in characters.
	 */
	public final String PREF_CONSOLE_BUFFER_SIZE = PREFIX + "pref.bufferSize"; //$NON-NLS-1$

	/**
	 * Show the console on output.
	 */
	public final String PREF_CONSOLE_SHOW_ON_OUTPUT = PREFIX + "pref.showOnOutput"; //$NON-NLS-1$

	// The preference id for the console font needs to be kept in sync with the plugin.xml!
	public final String PREF_CONSOLE_FONT = PREFIX + "pref.font"; //$NON-NLS-1$

	// The TCF communication monitor console color preferences
	public final String PREF_CONSOLE_COLOR_TEXT = PREFIX + "pref.color.text"; //$NON-NLS-1$
	public final String PREF_CONSOLE_COLOR_COMMAND = PREFIX + "pref.color.command"; //$NON-NLS-1$
	public final String PREF_CONSOLE_COLOR_COMMAND_RESPONSE = PREFIX + "pref.color.commandResponse"; //$NON-NLS-1$
	public final String PREF_CONSOLE_COLOR_EVENT = PREFIX + "pref.color.event"; //$NON-NLS-1$
	public final String PREF_CONSOLE_COLOR_PROGRESS = PREFIX + "pref.color.progress"; //$NON-NLS-1$
	public final String PREF_CONSOLE_COLOR_ERROR = PREFIX + "pref.color.error"; //$NON-NLS-1$
}

