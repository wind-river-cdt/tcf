/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.adapters;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.utils.AbstractSearchable;

/**
 * The searchable that provides a UI to collect and test
 * the general operations of a process search.
 */
public class GeneralSearchable extends AbstractSearchable {
	// The case sensitive check box.
	private Button fBtnCase;
	// The matching rule check box.
	private Button fBtnMatch;
	// The input field for searching conditions.
	private BaseEditBrowseTextControl fSearchField;
	// The current target names.
	private String fTargetName;
	// Whether it is case sensitive
	private boolean fCaseSensitive;
	// Whether it is precise matching.
	private boolean fMatchPrecise;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.utils.AbstractSearchable#createCommonPart(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    public void createCommonPart(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout glayout = new GridLayout(3, false);
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		composite.setLayout(glayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Searching field.
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.GeneralSearchable_FindLabel);

		fSearchField = new BaseEditBrowseTextControl(null);
		fSearchField.setIsGroup(false);
		fSearchField.setHasHistory(false);
		fSearchField.setHideBrowseButton(true);
		fSearchField.setParentControlIsInnerPanel(true);
		fSearchField.setupPanel(composite);
		fSearchField.setEditFieldValidator(new NameValidator());
		//fSearchField.setEditFieldValidator(new FolderValidator(this));
		Text text = (Text) fSearchField.getEditFieldControl();
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				searchTextModified();
			}
		});
		
		SelectionListener l = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				optionChecked(e);
			}
		};
		
		Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.GeneralSearchable_GeneralOptions);
		group.setLayout(new GridLayout(2, true));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Case sensitive
		fBtnCase = new Button(group, SWT.CHECK);
		fBtnCase.setText(Messages.GeneralSearchable_CaseSensitive);
		GridData data = new GridData();
		fBtnCase.setLayoutData(data);
		fBtnCase.addSelectionListener(l);

		// Matching precisely
		fBtnMatch = new Button(group, SWT.CHECK);
		fBtnMatch.setText(Messages.GeneralSearchable_PreciseMatching);
		data = new GridData();
		fBtnMatch.setLayoutData(data);
		fBtnMatch.addSelectionListener(l);
    }
	
	/**
	 * The text for searching is modified.
	 */
	protected void searchTextModified() {
		fireOptionChanged();
		fTargetName = fSearchField.getEditFieldControlText().trim();
    }
	
	/**
	 * Handling the event that a button is selected and checked.
	 * 
	 * @param e The selection event.
	 */
	protected void optionChecked(SelectionEvent e) {
		Object src = e.getSource();
		if (src == fBtnCase) {
			fCaseSensitive = fBtnCase.getSelection();
		}
		else if (src == fBtnMatch) {
			fMatchPrecise = fBtnMatch.getSelection();
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.utils.AbstractSearchable#isInputValid()
	 */
	@Override
    public boolean isInputValid() {
		return fSearchField.isValid();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.utils.AbstractSearchable#restoreValues(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	@Override
    public void restoreValues(IDialogSettings settings) {
		if(settings != null) {
			fCaseSensitive = settings.getBoolean("CaseSensitive"); //$NON-NLS-1$
			fBtnCase.setSelection(fCaseSensitive);
			fMatchPrecise = settings.getBoolean("MatchPrecise"); //$NON-NLS-1$
			fBtnMatch.setSelection(fMatchPrecise);
			fTargetName = settings.get("TargetName"); //$NON-NLS-1$
			if (fTargetName != null) {
				fSearchField.setEditFieldControlText(fTargetName);
			}
		}
		else {
			fCaseSensitive = false;
			fMatchPrecise = false;
			fTargetName = null;
		}
		fBtnCase.setSelection(fCaseSensitive);
		fBtnMatch.setSelection(fMatchPrecise);
		if (fTargetName != null) {
			fSearchField.setEditFieldControlText(fTargetName);
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.utils.AbstractSearchable#persistValues(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	@Override
    public void persistValues(IDialogSettings settings) {
		if(settings != null) {
			settings.put("CaseSensitive", fCaseSensitive); //$NON-NLS-1$
			settings.put("MatchPrecise", fMatchPrecise); //$NON-NLS-1$
			settings.put("TargetName", fTargetName); //$NON-NLS-1$
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getMatcher()
	 */
	@Override
	public ISearchMatcher getMatcher() {
		return new ProcessNodeGeneralMatcher(fCaseSensitive, fMatchPrecise, fTargetName);
	}
}

