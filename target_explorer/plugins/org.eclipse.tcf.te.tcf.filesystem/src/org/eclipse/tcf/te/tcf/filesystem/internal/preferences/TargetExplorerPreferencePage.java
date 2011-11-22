/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 * William Chen (Wind River) - [361324] Add more file operations in the file system
 * 												of Target Explorer.
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The preference page for configuring the preference options for Target
 * Explorer File System Explorer.
 *
 */
public class TargetExplorerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
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

	/***
	 * Create a preference page for Target Explorer File System Explorer.
	 */
	public TargetExplorerPreferencePage() {
		super(GRID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		UIPlugin plugin = UIPlugin.getDefault();
		IPreferenceStore preferenceStore = plugin.getPreferenceStore();
		setPreferenceStore(preferenceStore);
		BooleanFieldEditor autoSaving = new BooleanFieldEditor(PREF_AUTOSAVING, Messages.TargetExplorerPreferencePage_AutoSavingText, getFieldEditorParent());
		addField(autoSaving);
		BooleanFieldEditor renamingOption = new BooleanFieldEditor(PREF_RENAMING_IN_PLACE_EDITOR, Messages.TargetExplorerPreferencePage_RenamingOptionText, getFieldEditorParent());
		addField(renamingOption);
		BooleanFieldEditor copyPermission = new BooleanFieldEditor(PREF_COPY_PERMISSION, Messages.TargetExplorerPreferencePage_CopyPermissionText, getFieldEditorParent());
		addField(copyPermission);
		BooleanFieldEditor copyOwnership = new BooleanFieldEditor(PREF_COPY_OWNERSHIP, Messages.TargetExplorerPreferencePage_CopyOwnershipText, getFieldEditorParent());
		addField(copyOwnership);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// do nothing
	}
}
