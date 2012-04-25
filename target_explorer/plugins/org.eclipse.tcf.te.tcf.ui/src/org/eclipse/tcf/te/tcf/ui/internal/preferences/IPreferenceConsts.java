/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.internal.preferences;

/**
 * The constants for the preferences.
 */
public interface IPreferenceConsts {
	/**
	 * Common prefix for all preference keys
	 */
	public final String PREFIX = "te.tcf.ui."; //$NON-NLS-1$

	/**
	 * Preference key to access the flag to hide dynamic target discovery content extension.
	 */
	public static final String PREF_HIDE_DYNAMIC_TARGET_DISCOVERY_EXTENSION = "org.eclipse.tcf.te.tcf.ui.navigator.content.hide"; //$NON-NLS-1$

	/**
	 * Preference key to control if a node is moved or copied to the Favorites category.
	 */
	public static final String PREF_FAVORITES_CATEGORY_MODE_COPY = PREFIX + "navigator.categories.favorites.copymode"; //$NON-NLS-1$
}
