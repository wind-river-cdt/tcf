/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.log.core.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.tcf.te.runtime.preferences.ScopedEclipsePreferences;
import org.eclipse.tcf.te.tcf.log.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.log.core.interfaces.IPreferenceKeys;


/**
 * TCF logging bundle preference initializer.
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
		// Get the bundles scoped preferences store
		ScopedEclipsePreferences prefs = CoreBundleActivator.getScopedPreferences();
		if (prefs != null) {
			// Enable back-end communication logging: default on
			prefs.putDefaultBoolean(IPreferenceKeys.PREF_LOGGING_ENABLED, true);
			// Enable back-end communication monitor: default off
			prefs.putDefaultBoolean(IPreferenceKeys.PREF_MONITOR_ENABLED, false);
			// Heat beat events: default off
			prefs.putDefaultBoolean(IPreferenceKeys.PREF_SHOW_HEARTBEATS, false);
			// Framework events: default off
			prefs.putDefaultBoolean(IPreferenceKeys.PREF_SHOW_FRAMEWORK_EVENTS, false);
			// Maximum log file size in bytes: default 5M
			prefs.putDefaultString(IPreferenceKeys.PREF_MAX_FILE_SIZE, "5M"); //$NON-NLS-1$
			// Maximum number of log files in cycle: default 5
			prefs.putDefaultInt(IPreferenceKeys.PREF_MAX_FILES_IN_CYCLE, 5);
		}
	}
}
