/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.interfaces.preferences;

/**
 * The bundle's preference key identifiers.
 */
public interface IPreferenceKeys {
	/**
	 * Common prefix for all core preference keys
	 */
	public final String PREFIX = "te.tcf.filesystem.core."; //$NON-NLS-1$

	/**
	 * If set to <code>true</code>, the file system content contribution to the target
	 * explorer details editor will be activated and visible to the user.
	 */
	public static final String PREF_FEATURE_ENABLE_EDITOR_CONTENT_CONTRIBUTION = PREFIX + "feature.editor.content.enable"; //$NON-NLS-1$
	// The default value for editor content contribution
	public static final boolean DEFAULT_FEATURE_ENABLE_EDITOR_CONTENT_CONTRIBUTION = true; 
	// The preference key to access the option of auto saving
	public static final String PREF_AUTOSAVING = "PrefAutoSaving"; //$NON-NLS-1$
	// The default value of the option of auto saving.
	public static final boolean DEFAULT_AUTOSAVING = true;
	// The preference key to access the option using in-place editor during renaming.
	public static final String PREF_RENAMING_IN_PLACE_EDITOR = "PrefRenamingInPlaceEditor"; //$NON-NLS-1$
	// The default value of the option using in-place editor during renaming.
	public static final boolean DEFAULT_RENAMING_IN_PLACE_EDITOR = true;
	// The preference key to access the option of copy permission when copying files.
	public static final String PREF_COPY_PERMISSION = "PrefCopyPermission"; //$NON-NLS-1$
	// The default value of the option of copy permission.
	public static final boolean DEFAULT_COPY_PERMISSION = true;
	// The preference key to access the option of copy ownership when copying files.
	public static final String PREF_COPY_OWNERSHIP = "PrefCopyOwnership"; //$NON-NLS-1$
	// The default value of the option of copy ownership
	public static final boolean DEFAULT_COPY_OWNERSHIP = true;
	// The preference key to access the option that if expanded nodes should be persisted
	public static final String PREF_EXPANDED_PERSISTED = "PrefExpandedPersisted"; //$NON-NLS-1$
	// The default value of the option that if expanded nodes should be persisted
	public static final boolean DEFAULT_EXPANDED_PERSISTED = false;
}
