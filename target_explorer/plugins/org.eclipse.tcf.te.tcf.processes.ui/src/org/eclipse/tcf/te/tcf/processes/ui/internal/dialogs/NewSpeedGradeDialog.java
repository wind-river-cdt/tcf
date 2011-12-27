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

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.util.Util;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.tcf.te.tcf.processes.ui.model.IntervalGrade;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;

/**
 * The dialog to create a new grade for refreshing the process list.
 */
public class NewSpeedGradeDialog extends StatusDialog implements ModifyListener {
	// The text field to enter the name.
	private Text nameText;
	// The text field to enter the interval value.
	private Text valueText;
	// The grade that is currently edited.
	private IntervalGrade intervalGrade;
	// The grade list currently edited.
	private List<IntervalGrade> grades;
	

	/**
	 * Constructor
	 */
	public NewSpeedGradeDialog(Shell parent) {
	    super(parent);
	    updateStatus(new Status(IStatus.INFO, UIPlugin.getUniqueIdentifier(), Util.ZERO_LENGTH_STRING));
    }
	
	/**
	 * Set the currently edited grade list for validation purpose.
	 * 
	 * @param grades The grade list.
	 */
	public void setGrades(List<IntervalGrade> grades) {
		this.grades = grades;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.StatusDialog#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
    protected void configureShell(Shell shell) {
		shell.setText(Messages.NewSpeedGradeDialog_DialogTitle);
		super.configureShell(shell);
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    protected Control createDialogArea(Composite parent) {
		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		//Create the message area.
		Label label = new Label(composite, SWT.NONE);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(Messages.NewSpeedGradeDialog_DialogMessage);

	    label = new Label(composite, SWT.NONE);
	    label.setText(Messages.NewSpeedGradeDialog_NameLabel);
	    
	    nameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
	    nameText.setTextLimit(Text.LIMIT);
	    nameText.addModifyListener(this);
	    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
	    data.widthHint = 250;
	    nameText.setLayoutData(data);
	    
	    label = new Label(composite, SWT.NONE);
	    label.setText(Messages.NewSpeedGradeDialog_ValueLabel);
	    
	    valueText = new Text(composite, SWT.SINGLE | SWT.BORDER);
	    valueText.setTextLimit(Text.LIMIT);
	    valueText.addModifyListener(this);
	    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
	    valueText.setLayoutData(data);
	    
		applyDialogFont(composite);
		
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
		String name = nameText.getText();
		if (name == null || name.trim().length() == 0) {
			return new Status(IStatus.CANCEL, pluginId, null);
		}
		if (nameExists(name)) {
			return new Status(IStatus.ERROR, pluginId, NLS.bind(Messages.NewSpeedGradeDialog_GradeExists, name));
		}
		String txt = valueText.getText();
		if (txt == null || txt.trim().length() == 0) {
			return new Status(IStatus.CANCEL, pluginId, null);
		}
		try {
			int interval = Integer.parseInt(txt.trim());
			if (interval <= 0) return new Status(IStatus.ERROR, pluginId, Messages.IntervalConfigDialog_BiggerThanZero);
			name = valueExists(interval);
			if(name != null) return new Status(IStatus.WARNING, pluginId, NLS.bind(Messages.NewSpeedGradeDialog_GradeSameValue, name));
		}
		catch (NumberFormatException e) {
			return new Status(IStatus.ERROR, pluginId, Messages.IntervalConfigDialog_InvalidNumber);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Check if the grade with the same value exists. Return its
	 * name if it exists.
	 * 
	 * @param value The value to be checked.
	 * @return The grade's name or null if no such grade is found.
	 */
	private String valueExists(int interval) {
		for(IntervalGrade grade:grades) {
			if(grade != intervalGrade && grade.getValue() == interval)
				return grade.getName();
		}
	    return null;
    }

	/**
	 * Check if the grade with the same name exists. Return true
	 * if it exists.
	 * 
	 * @param name The name to be checked.
	 * @return true if there exists a grade with the same name.
	 */
	private boolean nameExists(String name) {
		for(IntervalGrade grade:grades) {
			if(name.equals(grade.getName()))
				return true;
		}
	    return false;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
    protected void okPressed() {
		String name = nameText.getText().trim();
		int value = Integer.parseInt(valueText.getText().trim());
		intervalGrade = new IntervalGrade(name, value);
	    super.okPressed();
    }
	
	/**
	 * Get the input result, a time interval.
	 * 
	 * @return The input result.
	 */
	public IntervalGrade getResult() {
		return intervalGrade;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.StatusDialog#updateButtonsEnableState(org.eclipse.core.runtime.IStatus)
	 */
	@Override
    protected void updateButtonsEnableState(IStatus status) {
		if (getButton(IDialogConstants.OK_ID) != null && !getButton(IDialogConstants.OK_ID).isDisposed()) {
			getButton(IDialogConstants.OK_ID).setEnabled(status.isOK());
		}
	}

	/**
	 * Validate the current input and update the button and the error message.
	 */
	private void validateInput() {
		IStatus status = isInputValid();
		updateStatus(status);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
    public void modifyText(ModifyEvent e) {
		validateInput();
    }
}
