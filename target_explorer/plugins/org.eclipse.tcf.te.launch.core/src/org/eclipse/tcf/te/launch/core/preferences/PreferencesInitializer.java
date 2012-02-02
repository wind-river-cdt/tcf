/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.tcf.te.launch.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.runtime.preferences.ScopedEclipsePreferences;

/**
 * Launch core framework preferences initializer implementation.
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// Get the preferences store
		ScopedEclipsePreferences store = CoreBundleActivator.getScopedPreferences();

		/**
		 * Launch configuration find/create mode: default <code>MODE_FIRST_MATCHING</code>.
		 */
		store.putDefaultInt(IPreferenceKeys.PREF_LAUNCH_CONFIG_FIND_CREATE_MODE, IPreferenceKeys.MODE_FIRST_MATCHING);

		/**
		 * Add project references to newly created launch configurations: default <code>true</code>.
		 */
		store.putDefaultBoolean(IPreferenceKeys.PREF_ADD_PROJECT_TO_NEW_LAUNCH_CONFIG, true);

		/**
		 * Sort launches by their last launched time stamp: default <code>true</code>.
		 */
		store.putDefaultBoolean(IPreferenceKeys.PREF_SORT_LAUNCHES_BY_LAST_LAUNCHED, true);
	}
}
