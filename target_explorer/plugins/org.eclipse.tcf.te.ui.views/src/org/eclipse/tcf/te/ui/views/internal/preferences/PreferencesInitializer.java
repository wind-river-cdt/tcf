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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tcf.te.ui.views.activator.UIPlugin;


/**
 * The bundle's preference initializer implementation.
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer implements IPreferenceConsts {
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
		IPreferenceStore preferenceStore = UIPlugin.getDefault().getPreferenceStore();
		preferenceStore.setDefault(PREF_MAX_FILTER_MRU, DEFAULT_MAX_MRU);
		preferenceStore.setDefault(PREF_MAX_CONTENT_MRU, DEFAULT_MAX_MRU);
		preferenceStore.setDefault(PREF_FILTER_MRU_LIST, ""); //$NON-NLS-1$
		preferenceStore.setDefault(PREF_CONTENT_MRU_LIST, ""); //$NON-NLS-1$
		preferenceStore.setDefault(PREF_HIDE_CATEGORY_EXTENSION, true);
	}
}
