/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.tcf.te.runtime.preferences.ScopedEclipsePreferences;
import org.eclipse.tcf.te.ui.views.activator.UIPlugin;


/**
 * The bundle's preference initializer implementation.
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
		ScopedEclipsePreferences store = UIPlugin.getScopedPreferences();

		// Maximum MRU list size "Filter": default IPreferenceKeys.DEFAULT_MAX_MRU
		store.putDefaultInt(IPreferenceKeys.PREF_MAX_FILTER_MRU, IPreferenceKeys.DEFAULT_MAX_MRU);
		// Maximum MRU list size "Content": default IPreferenceKeys.DEFAULT_MAX_MRU
		store.putDefaultInt(IPreferenceKeys.PREF_MAX_CONTENT_MRU, IPreferenceKeys.DEFAULT_MAX_MRU);
		// "Filter" MRU list: default empty
		store.putDefaultString(IPreferenceKeys.PREF_FILTER_MRU_LIST, ""); //$NON-NLS-1$
		// "Content" MRU list: default empty
		store.putDefaultString(IPreferenceKeys.PREF_CONTENT_MRU_LIST, ""); //$NON-NLS-1$
		// [Hidden] Hide categories navigator content extension: default on
		store.putDefaultBoolean(IPreferenceKeys.PREF_HIDE_CATEGORY_EXTENSION, true);
		// [Hidden] System management mode: default off
		store.putDefaultBoolean(IPreferenceKeys.PREF_SYSTEM_MANAGMENT_MODE, false);
	}
}
