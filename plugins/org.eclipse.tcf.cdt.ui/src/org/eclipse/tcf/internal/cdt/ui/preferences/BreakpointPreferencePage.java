/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.cdt.ui.preferences;

import java.util.Set;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.tcf.internal.cdt.ui.Activator;
import org.eclipse.tcf.internal.cdt.ui.breakpoints.Messages;
import org.eclipse.tcf.internal.cdt.ui.breakpoints.TCFThreadFilterEditor;
import org.eclipse.tcf.internal.cdt.ui.breakpoints.TCFToggleBreakpointsTargetFactory;

public class BreakpointPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String PLUGIN_ID="org.eclipse.tcf.cdt.ui.preferences.BreakpointPreferencePage";

    BooleanFieldEditor setDefaultTriggerCheckbox;
    ComboFieldEditor defaultTriggerExpressoinCombo;
    public BreakpointPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.TCFBreakpointPreferencesDescription);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        Object button = event.getSource();
        if (button instanceof BooleanFieldEditor) {
            BooleanFieldEditor fe = (BooleanFieldEditor)button;
            defaultTriggerExpressoinCombo.setEnabled(fe.getBooleanValue(), getFieldEditorParent());
        }
    }

    public void createFieldEditors() {
        setDefaultTriggerCheckbox = new BooleanFieldEditor(
                PreferenceConstants.PREF_DEFAULT_TRIGGER_SCOPE_ENABLED,
                Messages.TCFBreakpointPreferencesEnableDefaultTriggerScope,
                getFieldEditorParent());

        addField(setDefaultTriggerCheckbox);
        String [] expressionList = getTriggerExpressions();

        defaultTriggerExpressoinCombo = new ComboFieldEditor(
                PreferenceConstants.PREF_DEFAULT_TRIGGER_SCOPE,
                Messages.TCFBreakpointPreferencesTriggerScopeExpression,
                joinToArray2D(expressionList,expressionList),
                getFieldEditorParent());

        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        defaultTriggerExpressoinCombo.setEnabled(
                store.getBoolean(PreferenceConstants.PREF_DEFAULT_TRIGGER_SCOPE_ENABLED),
                getFieldEditorParent());

        addField(defaultTriggerExpressoinCombo);

        if (!checkTCFToggleBreakpointAdapter()) {
            setMessage(Messages.TCFBreakpointPrefrencesError, WARNING);
            setValid(false);
            setDefaultTriggerCheckbox.setEnabled(false, getFieldEditorParent());
            defaultTriggerExpressoinCombo.setEnabled(false, getFieldEditorParent());
        }
        else {
            setErrorMessage(null);
            setValid(true);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

    private String[] getTriggerExpressions() {
        IDialogSettings dialogSettings = getBreakpointScopeDialogSettings();
        String[] returnList = null;
        String[] expressionList = null;
        int index = 0;

        if ( dialogSettings != null ) {
            expressionList = dialogSettings.getArray(Messages.TCFThreadFilterQueryExpressionStore);
            // Find if there is a null entry.
            if ( expressionList != null ) {
                for(index = 0; index < expressionList.length; index++) {
                    String member = expressionList[index];
                    if (member == null || member.length() == 0) {
                        break;
                    }
                }
            }
            returnList = new String[index+1];
            returnList[0]="";
            for (int loop = 0; loop < index; loop++) {
                returnList[loop+1] = expressionList[loop];
            }
        }
        else
            returnList = new String[index];

        return returnList;
    }

    private IDialogSettings getBreakpointScopeDialogSettings() {
        String component = TCFThreadFilterEditor.PLUGIN_ID;
        IDialogSettings settings = Activator.getDefault()
                .getDialogSettings();
        IDialogSettings section = settings.getSection(component);
        return section;
    }

    private String[][] joinToArray2D(String[] labels, String[] values) {
        String[][] array2d = new String[labels.length][];
        for (int i = 0; i < labels.length; i++) {
            array2d[i] = new String[2];
            array2d[i][0] = labels[i];
            array2d[i][1] = values[i];
        }
        return array2d;
    }

    private boolean checkTCFToggleBreakpointAdapter() {
        IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (part == null) {
            part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
        }
        if (part != null) {
            ISelection selection = part.getSite().getSelectionProvider().getSelection();
            Set<?> enablers = DebugUITools.getToggleBreakpointsTargetManager().getEnabledToggleBreakpointsTargetIDs(part, selection);

            if (enablers != null &&
                !enablers.contains(TCFToggleBreakpointsTargetFactory.ID_TCF_BREAKPOINT_TOGGLE_TARGET)) {
                return true;
            }

            String preferred = DebugUITools.getToggleBreakpointsTargetManager().getPreferredToggleBreakpointsTargetID(part, selection);
            if (preferred != null &&
                !preferred.equals(TCFToggleBreakpointsTargetFactory.ID_TCF_BREAKPOINT_TOGGLE_TARGET)) {
                return false;
            }
        }
        return true;
    }
}
