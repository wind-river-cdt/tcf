/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.preferences;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tcf.te.runtime.preferences.ScopedEclipsePreferences;
import org.eclipse.tcf.te.tcf.log.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.log.core.interfaces.IPreferenceKeys;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Agent communication logging preference page.
 */
@SuppressWarnings("restriction")
public class LoggingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	// References to the field editors
    private BooleanFieldEditor enabled;
	private BooleanFieldEditor monitorEnabled;
	private BooleanFieldEditor showHeartbeats;
	private BooleanFieldEditor showFrameworkEvents;
	private StringFieldEditor logfileSize;
	private IntegerFieldEditor filesInCycle;

	// The preference store used internally for the field editors
	private IPreferenceStore store;

	/**
	 * Log file size field editor implementation.
	 */
	private class LogfileSizeFieldEditor extends StringFieldEditor {
		private Pattern valid = Pattern.compile("0|[1-9][0-9]*[KMG]?"); //$NON-NLS-1$

		/**
		 * Constructor.
		 *
		 * @param name The name of the preference this field editor works on.
		 * @param labelText The label text.
		 * @param parent the parent of the field editor's control
		 */
		public LogfileSizeFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
			this.setEmptyStringAllowed(false);
			this.setTextLimit(6);
			this.setErrorMessage(Messages.LoggingPreferencePage_maxFileSize_error);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.preference.StringFieldEditor#doCheckState()
		 */
		@Override
		protected boolean doCheckState() {
			if (valid.matcher(getStringValue()).matches()) {
				return true;
			}
			return false;
		}
	}


	/**
     * Constructor.
     */
    public LoggingPreferencePage() {
    	super(FieldEditorPreferencePage.GRID);

    	// The internal preference store never needs saving
    	store = new PreferenceStore() {
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
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		((GridLayout)parent.getLayout()).makeColumnsEqualWidth = false;

		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0; layout.marginWidth = 0;
		panel.setLayout(layout);
		panel.setFont(parent.getFont());

		Label label = new Label(panel, SWT.HORIZONTAL);
		label.setText(Messages.LoggingPreferencePage_label);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.horizontalSpan = 2;
		label.setLayoutData(layoutData);

		createSpacer(panel, 2);

		enabled = new BooleanFieldEditor(IPreferenceKeys.PREF_LOGGING_ENABLED,
										 Messages.LoggingPreferencePage_enabled_label, panel);
		addField(enabled);

		monitorEnabled = new BooleanFieldEditor(IPreferenceKeys.PREF_MONITOR_ENABLED,
												Messages.LoggingPreferencePage_monitorEnabled_label, panel);
		addField(monitorEnabled);

		createSpacer(panel, 2);

		Group filterGroup = new Group(panel, SWT.NONE);
		filterGroup.setText(Messages.LoggingPreferencePage_filterGroup_label);
		filterGroup.setLayout(new GridLayout(2, false));
		filterGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// The composite is necessary to get the margins within the group right!
		Composite filterPanel = new Composite(filterGroup, SWT.NONE);
		filterPanel.setLayout(new GridLayout());
		filterPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		showHeartbeats = new BooleanFieldEditor(IPreferenceKeys.PREF_SHOW_HEARTBEATS,
						 				 		Messages.LoggingPreferencePage_showHeartbeats_label, filterPanel);
		addField(showHeartbeats);

		showFrameworkEvents = new BooleanFieldEditor(IPreferenceKeys.PREF_SHOW_FRAMEWORK_EVENTS,
													 Messages.LoggingPreferencePage_showFrameworkEvents_label, filterPanel);
		addField(showFrameworkEvents);

		createSpacer(panel, 2);

		Group logfileGroup = new Group(panel, SWT.NONE);
		logfileGroup.setText(Messages.LoggingPreferencePage_logfileGroup_label);
		logfileGroup.setLayout(new GridLayout(2, false));
		logfileGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// The composite is necessary to get the margins within the group right!
		Composite logfilePanel = new Composite(logfileGroup, SWT.NONE);
		logfilePanel.setLayout(new GridLayout());
		logfilePanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		logfileSize = new LogfileSizeFieldEditor(IPreferenceKeys.PREF_MAX_FILE_SIZE,
												 Messages.LoggingPreferencePage_maxFileSize_label, logfilePanel);
		addField(logfileSize);

		filesInCycle = new IntegerFieldEditor(IPreferenceKeys.PREF_MAX_FILES_IN_CYCLE,
											  Messages.LoggingPreferencePage_maxFilesInCycle_label, logfilePanel);
		addField(filesInCycle);
	}

	/**
	 * Creates a empty space, 1/4 as high as a line.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param columnSpan The horizontal span.
	 */
	protected void createSpacer(Composite parent, int columnSpan) {
		Assert.isNotNull(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		gd.heightHint = SWTControlUtil.convertHeightInCharsToPixels(parent, 1) / 4;
		new Label(parent, SWT.NONE).setLayoutData(gd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#getPreferenceStore()
	 */
	@Override
	public IPreferenceStore getPreferenceStore() {
	    return store;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
	 */
	@Override
	protected void initialize() {
        ScopedEclipsePreferences prefs = CoreBundleActivator.getScopedPreferences();

		store.setDefault(IPreferenceKeys.PREF_LOGGING_ENABLED, prefs.getDefaultBoolean(IPreferenceKeys.PREF_LOGGING_ENABLED));
		store.setValue(IPreferenceKeys.PREF_LOGGING_ENABLED, prefs.getBoolean(IPreferenceKeys.PREF_LOGGING_ENABLED));

		store.setDefault(IPreferenceKeys.PREF_MONITOR_ENABLED, prefs.getDefaultBoolean(IPreferenceKeys.PREF_MONITOR_ENABLED));
		store.setValue(IPreferenceKeys.PREF_MONITOR_ENABLED, prefs.getBoolean(IPreferenceKeys.PREF_MONITOR_ENABLED));

		store.setDefault(IPreferenceKeys.PREF_SHOW_HEARTBEATS, prefs.getDefaultBoolean(IPreferenceKeys.PREF_SHOW_HEARTBEATS));
		store.setValue(IPreferenceKeys.PREF_SHOW_HEARTBEATS, prefs.getBoolean(IPreferenceKeys.PREF_SHOW_HEARTBEATS));

		store.setDefault(IPreferenceKeys.PREF_SHOW_FRAMEWORK_EVENTS, prefs.getDefaultBoolean(IPreferenceKeys.PREF_SHOW_FRAMEWORK_EVENTS));
		store.setValue(IPreferenceKeys.PREF_SHOW_FRAMEWORK_EVENTS, prefs.getBoolean(IPreferenceKeys.PREF_SHOW_FRAMEWORK_EVENTS));

		store.setDefault(IPreferenceKeys.PREF_MAX_FILE_SIZE, prefs.getDefaultString(IPreferenceKeys.PREF_MAX_FILE_SIZE));
		store.setValue(IPreferenceKeys.PREF_MAX_FILE_SIZE, prefs.getString(IPreferenceKeys.PREF_MAX_FILE_SIZE));

		store.setDefault(IPreferenceKeys.PREF_MAX_FILES_IN_CYCLE, prefs.getDefaultInt(IPreferenceKeys.PREF_MAX_FILES_IN_CYCLE));
		store.setValue(IPreferenceKeys.PREF_MAX_FILES_IN_CYCLE, prefs.getInt(IPreferenceKeys.PREF_MAX_FILES_IN_CYCLE));

		// Load values into field editors
	    super.initialize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
	    boolean success = super.performOk();

	    if (success) {
	        ScopedEclipsePreferences prefs = CoreBundleActivator.getScopedPreferences();

	        prefs.putBoolean(IPreferenceKeys.PREF_LOGGING_ENABLED, store.getBoolean(IPreferenceKeys.PREF_LOGGING_ENABLED));
	        prefs.putBoolean(IPreferenceKeys.PREF_MONITOR_ENABLED, store.getBoolean(IPreferenceKeys.PREF_MONITOR_ENABLED));

	        prefs.putBoolean(IPreferenceKeys.PREF_SHOW_HEARTBEATS, store.getBoolean(IPreferenceKeys.PREF_SHOW_HEARTBEATS));
	        prefs.putBoolean(IPreferenceKeys.PREF_SHOW_FRAMEWORK_EVENTS, store.getBoolean(IPreferenceKeys.PREF_SHOW_FRAMEWORK_EVENTS));

	        prefs.putString(IPreferenceKeys.PREF_MAX_FILE_SIZE, store.getString(IPreferenceKeys.PREF_MAX_FILE_SIZE));
	        prefs.putInt(IPreferenceKeys.PREF_MAX_FILES_IN_CYCLE, store.getInt(IPreferenceKeys.PREF_MAX_FILES_IN_CYCLE));
	    }

	    return success;
	}
}
