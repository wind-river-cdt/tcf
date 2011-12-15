/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.console.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tcf.te.runtime.preferences.ScopedEclipsePreferences;
import org.eclipse.tcf.te.tcf.ui.console.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.console.interfaces.IPreferenceKeys;

/**
 * Preference initializer.
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

	/**
	 * Constructor.
	 */
	public PreferencesInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// Get the preferences store
		ScopedEclipsePreferences store = UIPlugin.getScopedPreferences();

		// Fixed console width: default off
		store.putDefaultBoolean(IPreferenceKeys.PREF_CONSOLE_FIXED_WIDTH, false);
		// Fixed console width in character: default 80
		store.putDefaultInt(IPreferenceKeys.PREF_CONSOLE_WIDTH, 80);
		// Limit console output: default on
		store.putDefaultBoolean(IPreferenceKeys.PREF_CONSOLE_LIMIT_OUTPUT, true);
		// Console buffer size in character: default 500000
		store.putDefaultInt(IPreferenceKeys.PREF_CONSOLE_BUFFER_SIZE, 500000);
		// Show console on output: default off
		store.putDefaultBoolean(IPreferenceKeys.PREF_CONSOLE_SHOW_ON_OUTPUT, false);

		// Text default color: Black
		store.putDefaultString(IPreferenceKeys.PREF_CONSOLE_COLOR_TEXT, StringConverter.asString(new RGB(0, 0, 0)));
		// Command default color: Black
		store.putDefaultString(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND, StringConverter.asString(new RGB(0, 0, 0)));
		// Command response default color: Black
		store.putDefaultString(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND_RESPONSE, StringConverter.asString(new RGB(0, 0, 0)));
		// Event default color: Blue
		store.putDefaultString(IPreferenceKeys.PREF_CONSOLE_COLOR_EVENT, StringConverter.asString(new RGB(0, 0, 255)));
		// Progress default color: Green
		store.putDefaultString(IPreferenceKeys.PREF_CONSOLE_COLOR_PROGRESS, StringConverter.asString(new RGB(0, 128, 0)));
		// Error default color: Red
		store.putDefaultString(IPreferenceKeys.PREF_CONSOLE_COLOR_ERROR, StringConverter.asString(new RGB(255, 0, 0)));
	}

}
