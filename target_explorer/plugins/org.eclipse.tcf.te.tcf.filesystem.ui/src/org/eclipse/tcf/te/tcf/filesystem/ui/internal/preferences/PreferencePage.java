/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.ui.interfaces.preferences.IPreferenceKeys;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The preference page for configuring the preference options for
 * the TCF File System Explorer.
 *
 */
public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IPreferenceKeys {

	/***
	 * Create a preference page for Target Explorer File System Explorer.
	 */
	public PreferencePage() {
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
		BooleanFieldEditor autoSaving = new BooleanFieldEditor(PREF_AUTOSAVING, Messages.PreferencePage_AutoSavingText, getFieldEditorParent());
		addField(autoSaving);
		BooleanFieldEditor renamingOption = new BooleanFieldEditor(PREF_RENAMING_IN_PLACE_EDITOR, Messages.PreferencePage_RenamingOptionText, getFieldEditorParent());
		addField(renamingOption);
		BooleanFieldEditor copyPermission = new BooleanFieldEditor(PREF_COPY_PERMISSION, Messages.PreferencePage_CopyPermissionText, getFieldEditorParent());
		addField(copyPermission);
		BooleanFieldEditor copyOwnership = new BooleanFieldEditor(PREF_COPY_OWNERSHIP, Messages.PreferencePage_CopyOwnershipText, getFieldEditorParent());
		addField(copyOwnership);
		BooleanFieldEditor persistExpanded = new BooleanFieldEditor(PREF_EXPANDED_PERSISTED, Messages.PreferencePage_PersistExpanded, getFieldEditorParent());
		addField(persistExpanded);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// do nothing
	}
}
