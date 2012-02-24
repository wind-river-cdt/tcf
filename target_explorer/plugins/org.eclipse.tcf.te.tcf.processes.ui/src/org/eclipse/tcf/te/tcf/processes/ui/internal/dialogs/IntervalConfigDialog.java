/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.processes.ui.internal.preferences.IPreferenceConsts;
import org.eclipse.tcf.te.tcf.processes.ui.model.IntervalGrade;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;

/**
 * The dialog to configure the refreshing interval of the process list.
 */
public class IntervalConfigDialog extends StatusDialog implements ModifyListener, IPreferenceConsts {
	// The text field to enter the interval value.
	private Text text;
	// The entered result
	private int result;

	/**
	 * Constructor
	 */
	public IntervalConfigDialog(Shell parent) {
	    super(parent);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.StatusDialog#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
    protected void configureShell(Shell shell) {
		shell.setText(Messages.IntervalConfigDialog_DialogTitle);
		super.configureShell(shell);
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    protected Control createDialogArea(Composite parent) {
	    Composite composite = (Composite) super.createDialogArea(parent);
	    
	    Composite comp1 = new Composite(composite, SWT.NONE);
	    GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
	    comp1.setLayoutData(data);
	    GridLayout layout = new GridLayout(3, false);
	    layout.horizontalSpacing = 0;
	    comp1.setLayout(layout);
	    
	    Label label = new Label(comp1, SWT.RADIO);
	    label.setText(Messages.IntervalConfigDialog_ChoiceOneLabel);
	    
	    text = new Text(comp1, SWT.SINGLE | SWT.BORDER);
	    text.setTextLimit(Text.LIMIT);
	    data = new GridData();
	    data.widthHint = 70;
	    text.setLayoutData(data);
	    text.setText(""+result); //$NON-NLS-1$
	    text.selectAll();
	    text.setFocus();
	    text.addModifyListener(this);
	    
	    label = new Label(comp1, SWT.NONE);
	    label.setText(Messages.IntervalConfigDialog_SECONDS);
    	
	    return composite;
    }
	
	/**
	 * Check if the current input is valid and return an IStatus object to return
	 * the checking result, containing the message and the validating code.
	 * 
	 * @return A status to indicate if the input is valid.
	 */
	private IStatus isInputValid() {
		String pluginId = UIPlugin.getUniqueIdentifier();
		String txt = text.getText();
		if (txt == null || txt.trim().length() == 0) {
			return new Status(IStatus.ERROR, pluginId, null);
		}
		try {
			int interval = Integer.parseInt(txt.trim());
			if (interval < 0) return new Status(IStatus.ERROR, pluginId, Messages.IntervalConfigDialog_BiggerThanZero);
			if(interval == 0) return new Status(IStatus.WARNING, pluginId, Messages.IntervalConfigDialog_ZeroWarning);
		}
		catch (NumberFormatException e) {
			return new Status(IStatus.ERROR, pluginId, Messages.IntervalConfigDialog_InvalidNumber);
		}
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
    protected void okPressed() {
		String txt = text.getText().trim();
		result = Integer.parseInt(txt);
	    super.okPressed();
    }
	
	/**
	 * Get the input result, a time interval.
	 * 
	 * @return The input result.
	 */
	public int getResult() {
		return result;
	}

	/**
	 * Validate the current input and update the button and the error message.
	 */
	private void validateInput() {
		IStatus status = isInputValid();
		updateStatus(status);
	}

	/**
	 * The label provider used to display the speed grades in the combo viewer.
	 */
	static class GradeLabelProvider extends LabelProvider {
		@Override
        public String getText(Object element) {
			if(element instanceof IntervalGrade) {
				IntervalGrade grade = (IntervalGrade) element;
				return grade.getName() + " ("+grade.getValue()+" "+Messages.IntervalConfigDialog_SECOND_ABBR+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
	        return super.getText(element);
        }
	}

	/**
	 * Get the current the speed grades in an array of Grade.
	 * 
	 * @return The current speed grades.
	 */
	IntervalGrade[] getGrades(){
		List<IntervalGrade> gradeList = new ArrayList<IntervalGrade>();
        IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
		String grades = prefStore.getString(PREF_INTERVAL_GRADES);
		Assert.isNotNull(grades);
		StringTokenizer st = new StringTokenizer(grades, "|"); //$NON-NLS-1$
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(token, ":"); //$NON-NLS-1$
			String name = st2.nextToken();
			String value = st2.nextToken();
			try{
				int seconds = Integer.parseInt(value);
				if(seconds > 0) {
					gradeList.add(new IntervalGrade(name, seconds));
				}
			}
			catch (NumberFormatException nfe) {
			}
		}
		return gradeList.toArray(new IntervalGrade[gradeList.size()]);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
    public void modifyText(ModifyEvent e) {
		validateInput();
    }

	/**
	 * Set the current interval to the text field.
	 * 
	 * @param interval The current interval.
	 */
	public void setResult(int interval) {
		this.result = interval;
    }
}
