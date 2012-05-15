/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.console.internal.preferences;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tcf.te.runtime.preferences.ScopedEclipsePreferences;
import org.eclipse.tcf.te.tcf.ui.console.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.console.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.ui.console.interfaces.IPreferenceKeys;
import org.eclipse.tcf.te.tcf.ui.console.nls.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Console preference page implementation.
 */
public class PreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	// The reference to the internal preference store
	private final IPreferenceStore preferenceStore;

	// Field editor references
	private BooleanFieldEditor fixedConsoleWidth;
	private IntegerFieldEditor consoleWidth;
	private BooleanFieldEditor limitConsoleOutput;
	private IntegerFieldEditor consoleBufferSize;

	/**
	 * Constructor.
	 */
	public PreferencesPage() {
		super(GRID);
		// Use a preferences store which never needs saving
		preferenceStore = new PreferenceStore() {
			@Override
			public boolean needsSaving() {
				return false;
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
    public void init(IWorkbench workbench) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#getPreferenceStore()
	 */
	@Override
	public IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		// Get the field control parent
		final Composite composite = getFieldEditorParent();
		// Set context help id
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IContextHelpIds.MONITOR_CONSOLE_PREFERENCES_PAGE);

		// Create the top label
		Label label = new Label(composite, SWT.LEAD);
		label.setText(Messages.PreferencesPage_label);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		label.setLayoutData(layoutData);

		// Get the preference store to use
		IPreferenceStore store = getPreferenceStore();

		// Create the "Fixed width console" field editor
		fixedConsoleWidth = new BooleanFieldEditor(IPreferenceKeys.PREF_CONSOLE_FIXED_WIDTH, Messages.PreferencesPage_fieldEditor_fixedWidth, composite);
		addField(fixedConsoleWidth);

		// Create the "width" field editor. Enabled if the "Fixed width console" field editor is checked
		consoleWidth = new IntegerFieldEditor(IPreferenceKeys.PREF_CONSOLE_WIDTH, Messages.PreferencesPage_fieldEditor_width, composite); //)
		consoleWidth.setValidRange(80, Integer.MAX_VALUE - 1);
		addField(consoleWidth);
		// Update enablement
		consoleWidth.setEnabled(store.getBoolean(IPreferenceKeys.PREF_CONSOLE_FIXED_WIDTH), composite);

		// Create the "Limit output" field editor
		limitConsoleOutput = new BooleanFieldEditor(IPreferenceKeys.PREF_CONSOLE_LIMIT_OUTPUT, Messages.PreferencesPage_fieldEditor_limitOutput, composite);
		addField(limitConsoleOutput);

		// Create the "buffer size" field editor
		consoleBufferSize = new IntegerFieldEditor(IPreferenceKeys.PREF_CONSOLE_BUFFER_SIZE, Messages.PreferencesPage_fieldEditor_bufferSize, composite); //)
		consoleBufferSize.setValidRange(1000, Integer.MAX_VALUE - 1);
		addField(consoleBufferSize);
		// Update enablement
		consoleBufferSize.setEnabled(store.getBoolean(IPreferenceKeys.PREF_CONSOLE_LIMIT_OUTPUT), composite);

		// Create the "Show on output" field editor
		BooleanFieldEditor booleanField = new BooleanFieldEditor(IPreferenceKeys.PREF_CONSOLE_SHOW_ON_OUTPUT, Messages.PreferencesPage_fieldEditor_showOnOutput, composite);
		addField(booleanField);

		// Create a spacer
		label = new Label(composite, SWT.LEAD);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		label.setLayoutData(layoutData);

		// Create the color field editors
		label = new Label(composite, SWT.LEAD);
		label.setText(Messages.PreferencesPage_group_colorSettings);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		label.setLayoutData(layoutData);

		ColorFieldEditor colorField = createColorFieldEditor(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND, Messages.PreferencesPage_color_command, composite);
		addField(colorField);

		colorField = createColorFieldEditor(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND_RESPONSE, Messages.PreferencesPage_color_commandResponse, composite);
		addField(colorField);

		colorField = createColorFieldEditor(IPreferenceKeys.PREF_CONSOLE_COLOR_EVENT, Messages.PreferencesPage_color_event, composite);
		addField(colorField);

		colorField = createColorFieldEditor(IPreferenceKeys.PREF_CONSOLE_COLOR_PROGRESS, Messages.PreferencesPage_color_progress, composite);
		addField(colorField);

		colorField = createColorFieldEditor(IPreferenceKeys.PREF_CONSOLE_COLOR_ERROR, Messages.PreferencesPage_color_error, composite);
		addField(colorField);

		// Apply common dialog font
		Dialog.applyDialogFont(composite);
	}

	/**
	 * Helper to create a {@link ColorFieldEditor}.
	 *
	 * @param slotId The preference slot id. Must not be <code>null</code>.
	 * @param label The field editor label. Must not be <code>null</code>.
	 * @param parent The field editor parent. Must not be <code>null</code>.
	 */
	private ColorFieldEditor createColorFieldEditor(String slotId, String label, Composite parent) {
		Assert.isNotNull(slotId);
		Assert.isNotNull(label);
		Assert.isNotNull(parent);

		// Create the color field editor
		ColorFieldEditor editor = new ColorFieldEditor(slotId, label, parent);
		// Initialize the color field editor
		editor.setPage(this);
		editor.setPreferenceStore(getPreferenceStore());

		return editor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		// Update the enablement
		if (consoleWidth != null && fixedConsoleWidth != null) {
			consoleWidth.setEnabled(fixedConsoleWidth.getBooleanValue(), getFieldEditorParent());
		}
		if (consoleBufferSize != null && limitConsoleOutput != null) {
			consoleBufferSize.setEnabled(limitConsoleOutput.getBooleanValue(), getFieldEditorParent());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
	 */
	@Override
	protected void initialize() {
		ScopedEclipsePreferences store = UIPlugin.getScopedPreferences();

		preferenceStore.setDefault(IPreferenceKeys.PREF_CONSOLE_FIXED_WIDTH, store.getDefaultBoolean(IPreferenceKeys.PREF_CONSOLE_FIXED_WIDTH));
		preferenceStore.setValue(IPreferenceKeys.PREF_CONSOLE_FIXED_WIDTH, store.getBoolean(IPreferenceKeys.PREF_CONSOLE_FIXED_WIDTH));

		preferenceStore.setDefault(IPreferenceKeys.PREF_CONSOLE_WIDTH, store.getDefaultInt(IPreferenceKeys.PREF_CONSOLE_WIDTH));
		preferenceStore.setValue(IPreferenceKeys.PREF_CONSOLE_WIDTH, store.getInt(IPreferenceKeys.PREF_CONSOLE_WIDTH));

		preferenceStore.setDefault(IPreferenceKeys.PREF_CONSOLE_LIMIT_OUTPUT, store.getDefaultBoolean(IPreferenceKeys.PREF_CONSOLE_LIMIT_OUTPUT));
		preferenceStore.setValue(IPreferenceKeys.PREF_CONSOLE_LIMIT_OUTPUT, store.getBoolean(IPreferenceKeys.PREF_CONSOLE_LIMIT_OUTPUT));

		preferenceStore.setDefault(IPreferenceKeys.PREF_CONSOLE_BUFFER_SIZE, store.getDefaultInt(IPreferenceKeys.PREF_CONSOLE_BUFFER_SIZE));
		preferenceStore.setValue(IPreferenceKeys.PREF_CONSOLE_BUFFER_SIZE, store.getInt(IPreferenceKeys.PREF_CONSOLE_BUFFER_SIZE));

		preferenceStore.setDefault(IPreferenceKeys.PREF_CONSOLE_SHOW_ON_OUTPUT, store.getDefaultBoolean(IPreferenceKeys.PREF_CONSOLE_SHOW_ON_OUTPUT));
		preferenceStore.setValue(IPreferenceKeys.PREF_CONSOLE_SHOW_ON_OUTPUT, store.getBoolean(IPreferenceKeys.PREF_CONSOLE_SHOW_ON_OUTPUT));

		preferenceStore.setDefault(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND, store.getDefaultString(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND));
		preferenceStore.setValue(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND, store.getString(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND));

		preferenceStore.setDefault(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND_RESPONSE, store.getDefaultString(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND_RESPONSE));
		preferenceStore.setValue(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND_RESPONSE, store.getString(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND_RESPONSE));

		preferenceStore.setDefault(IPreferenceKeys.PREF_CONSOLE_COLOR_EVENT, store.getDefaultString(IPreferenceKeys.PREF_CONSOLE_COLOR_EVENT));
		preferenceStore.setValue(IPreferenceKeys.PREF_CONSOLE_COLOR_EVENT, store.getString(IPreferenceKeys.PREF_CONSOLE_COLOR_EVENT));

		preferenceStore.setDefault(IPreferenceKeys.PREF_CONSOLE_COLOR_PROGRESS, store.getDefaultString(IPreferenceKeys.PREF_CONSOLE_COLOR_PROGRESS));
		preferenceStore.setValue(IPreferenceKeys.PREF_CONSOLE_COLOR_PROGRESS, store.getString(IPreferenceKeys.PREF_CONSOLE_COLOR_PROGRESS));

		preferenceStore.setDefault(IPreferenceKeys.PREF_CONSOLE_COLOR_ERROR, store.getDefaultString(IPreferenceKeys.PREF_CONSOLE_COLOR_ERROR));
		preferenceStore.setValue(IPreferenceKeys.PREF_CONSOLE_COLOR_ERROR, store.getString(IPreferenceKeys.PREF_CONSOLE_COLOR_ERROR));

		// Load values into field editors
		super.initialize();

		// Trigger an update of the enablements
		propertyChange(new PropertyChangeEvent(this, "", null, null)); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		// The super.performOk() needs to be called before
		// we can copy the values from the preference store.
		boolean success = super.performOk();

		// Get the plugin preferences manager
		ScopedEclipsePreferences store = UIPlugin.getScopedPreferences();

		// Copy from the preferences store to the preferences manager
		store.putBoolean(IPreferenceKeys.PREF_CONSOLE_FIXED_WIDTH, preferenceStore.getBoolean(IPreferenceKeys.PREF_CONSOLE_FIXED_WIDTH));
		store.putInt(IPreferenceKeys.PREF_CONSOLE_WIDTH, preferenceStore.getInt(IPreferenceKeys.PREF_CONSOLE_WIDTH));
		store.putBoolean(IPreferenceKeys.PREF_CONSOLE_LIMIT_OUTPUT, preferenceStore.getBoolean(IPreferenceKeys.PREF_CONSOLE_LIMIT_OUTPUT));
		store.putInt(IPreferenceKeys.PREF_CONSOLE_BUFFER_SIZE, preferenceStore.getInt(IPreferenceKeys.PREF_CONSOLE_BUFFER_SIZE));
		store.putBoolean(IPreferenceKeys.PREF_CONSOLE_SHOW_ON_OUTPUT, preferenceStore.getBoolean(IPreferenceKeys.PREF_CONSOLE_SHOW_ON_OUTPUT));
		store.putString(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND, preferenceStore.getString(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND));
		store.putString(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND_RESPONSE, preferenceStore.getString(IPreferenceKeys.PREF_CONSOLE_COLOR_COMMAND_RESPONSE));
		store.putString(IPreferenceKeys.PREF_CONSOLE_COLOR_EVENT, preferenceStore.getString(IPreferenceKeys.PREF_CONSOLE_COLOR_EVENT));
		store.putString(IPreferenceKeys.PREF_CONSOLE_COLOR_PROGRESS, preferenceStore.getString(IPreferenceKeys.PREF_CONSOLE_COLOR_PROGRESS));
		store.putString(IPreferenceKeys.PREF_CONSOLE_COLOR_ERROR, preferenceStore.getString(IPreferenceKeys.PREF_CONSOLE_COLOR_ERROR));

		return success;
	}
}
