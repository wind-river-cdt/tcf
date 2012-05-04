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

/**
 * The constants for the preferences used in the main view.
 */
public interface IPreferenceKeys {
	/**
	 * Common prefix for all preference keys
	 */
	public final String PREFIX = "te.ui."; //$NON-NLS-1$

	/**
	 * The default size of MRU list.
	 */
	public static final int DEFAULT_MAX_MRU = 3;

	/**
	 * Preference key to access MRU filter list.
	 */
	public static final String PREF_FILTER_MRU_LIST = PREFIX + "PrefFilterMRUs"; //$NON-NLS-1$

	/**
	 * Preference key to access max MRU filter size.
	 */
	public static final String PREF_MAX_FILTER_MRU = PREFIX + "PrefFilterMRUs.max"; //$NON-NLS-1$

	/**
	 * Preference key to access MRU content list.
	 */
	public static final String PREF_CONTENT_MRU_LIST = PREFIX + "PrefContentMRUs"; //$NON-NLS-1$

	/**
	 * Preference key to access max MRU content size.
	 */
	public static final String PREF_MAX_CONTENT_MRU = PREFIX + "PrefContentMRUs.max"; //$NON-NLS-1$

	/**
	 * Preference key to access the flag to hide category content extension.
	 */
	public static final String PREF_HIDE_CATEGORY_EXTENSION = "org.eclipse.tcf.te.ui.views.navigator.content.hide"; //$NON-NLS-1$

	/**
	 * Preference key to access the flag that controls if running in "System Management" mode.
	 */
	public static final String PREF_SYSTEM_MANAGMENT_MODE = PREFIX + "mode.systemmanagement"; //$NON-NLS-1$
}
