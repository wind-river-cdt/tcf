/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.adapters;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl;

/**
 * The searchable that provides a UI to collect and test
 * the last modified time of a file during searching.
 */
public class FSModifiedSearchable extends FSBaseSearchable {
	// Constant values of last modified options
	private static final int OPTION_NOT_REMEMBER = 0;
	private static final int OPTION_LAST_WEEK = 1;
	private static final int OPTION_LAST_MONTH = 2;
	private static final int OPTION_LAST_YEAR = 3;
	private static final int OPTION_SPECIFIED = 4;
	
	// Constant values of different time unit, used for matching purpose.
	private static final long SECOND = 1000L;
	private static final long MINUTE = 60 * SECOND;
	private static final long HOUR = 60 * MINUTE;
	private static final long DAY = 24 * HOUR;
	private static final long WEEK = 7 * DAY;
	private static final long MONTH = 30 * DAY;
	private static final long YEAR = 365 * DAY;
	
	// The choice selected
	private int choice;
	// The specified "from" date
	private long fromTime;
	// The specified "to" date
	private long toTime;

	// UI elements for input
	private Button fBtnLmNotRem;
	private Button fBtnLmLastWeek;
	private Button fBtnLmPastMonth;
	private Button fBtnLmPastYear;
	private Button fBtnLmSpecified;
	private BaseEditBrowseTextControl txtLmFrom;
	private BaseEditBrowseTextControl txtLmTo;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.utils.AbstractSearchable#createAdvancedPart(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    public void createAdvancedPart(Composite parent) {
		SelectionListener l = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				optionChecked(e);
			}
		};
		Composite modifiedComp = createSection(parent, Messages.FSModifiedSearchable_WhenModified);
		modifiedComp.setLayout(new GridLayout(4, false));
		
		fBtnLmNotRem = new Button(modifiedComp, SWT.RADIO);
		fBtnLmNotRem.setText(Messages.FSModifiedSearchable_DontRemember);
		fBtnLmNotRem.setSelection(true);
		GridData data = new GridData();
		data.horizontalSpan = 4;
		fBtnLmNotRem.setLayoutData(data);
		fBtnLmNotRem.addSelectionListener(l);

		fBtnLmLastWeek = new Button(modifiedComp, SWT.RADIO);
		fBtnLmLastWeek.setText(Messages.FSModifiedSearchable_LastWeek);
		data = new GridData();
		data.horizontalSpan = 4;
		fBtnLmLastWeek.setLayoutData(data);
		fBtnLmLastWeek.addSelectionListener(l);
		
		fBtnLmPastMonth = new Button(modifiedComp, SWT.RADIO);
		fBtnLmPastMonth.setText(Messages.FSModifiedSearchable_PastMonth);
		data = new GridData();
		data.horizontalSpan = 4;
		fBtnLmPastMonth.setLayoutData(data);
		fBtnLmPastMonth.addSelectionListener(l);
		
		fBtnLmPastYear = new Button(modifiedComp, SWT.RADIO);
		fBtnLmPastYear.setText(Messages.FSModifiedSearchable_PastYear);
		data = new GridData();
		data.horizontalSpan = 4;
		fBtnLmPastYear.setLayoutData(data);
		fBtnLmPastYear.addSelectionListener(l);
		
		fBtnLmSpecified = new Button(modifiedComp, SWT.RADIO);
		fBtnLmSpecified.setText(Messages.FSModifiedSearchable_SpecifyDates);
		data = new GridData();
		fBtnLmSpecified.setLayoutData(data);
		fBtnLmSpecified.addSelectionListener(l);
		
		Composite cmpFrom = new Composite(modifiedComp, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		cmpFrom.setLayout(layout);
		data = new GridData();
		cmpFrom.setLayoutData(data);
		
		txtLmFrom = new BaseEditBrowseTextControl(null);
		txtLmFrom.setIsGroup(false);
		txtLmFrom.setHasHistory(false);
		txtLmFrom.setHideBrowseButton(true);
		txtLmFrom.setParentControlIsInnerPanel(true);
		txtLmFrom.setupPanel(cmpFrom);
		txtLmFrom.setEnabled(false);
		txtLmFrom.setEditFieldValidator(new DateValidator());
		Text text = (Text) txtLmFrom.getEditFieldControl();
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				datesModified();
			}
		});
		
		Label label = new Label(modifiedComp, SWT.NONE);
		label.setText(Messages.FSModifiedSearchable_ToDate);
		
		Composite cmpTo = new Composite(modifiedComp, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		cmpTo.setLayout(layout);
		data = new GridData();
		cmpTo.setLayoutData(data);

		txtLmTo = new BaseEditBrowseTextControl(null);
		txtLmTo.setIsGroup(false);
		txtLmTo.setHasHistory(false);
		txtLmTo.setHideBrowseButton(true);
		txtLmTo.setParentControlIsInnerPanel(true);
		txtLmTo.setupPanel(cmpTo);
		txtLmTo.setEnabled(false);
		txtLmTo.setEditFieldValidator(new DateValidator());
		text = (Text) txtLmTo.getEditFieldControl();
		text.addModifyListener(new ModifyListener() {
            @Override
			public void modifyText(ModifyEvent e) {
				datesModified();
			}
		});
    }

	/**
	 * The modified event of the date fields.
	 */
	protected void datesModified() {
		fireOptionChanged();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.utils.AbstractSearchable#isInputValid()
	 */
	@Override
    public boolean isInputValid() {
		if(choice == OPTION_SPECIFIED) {
			boolean vFrom = txtLmFrom.isValid();
			boolean vTo = txtLmTo.isValid();
			if(vFrom) {
				String fromText = txtLmFrom.getEditFieldControlText().trim();
				this.fromTime = DateValidator.parseTimeInMillis(fromText);
			}
			if(vTo) {
				String toText = txtLmTo.getEditFieldControlText().trim();
				this.toTime = DateValidator.parseTimeInMillis(toText);
			}
			return vFrom && vTo;
		}
	    return true;
    }

	/**
	 * The method handling the selection event.
	 * 
	 * @param e The selection event.
	 */
	protected void optionChecked(SelectionEvent e) {
		Object src = e.getSource();
		boolean spec = false;
		if(src == fBtnLmNotRem) {
			choice = OPTION_NOT_REMEMBER;
		}
		else if(src == fBtnLmLastWeek) {
			choice = OPTION_LAST_WEEK;
		}
		else if(src == fBtnLmPastMonth) {
			choice = OPTION_LAST_MONTH;
		}
		else if(src == fBtnLmPastYear) {
			choice = OPTION_LAST_YEAR;
		}
		else if(src == fBtnLmSpecified) {
			choice = OPTION_SPECIFIED;
			spec = true;
		}
		txtLmFrom.setEnabled(spec);
		txtLmTo.setEnabled(spec);
		fireOptionChanged();
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchMatcher#match(java.lang.Object)
	 */
	@Override
    public boolean match(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) element;
			long now = System.currentTimeMillis();
			switch (choice) {
			case OPTION_NOT_REMEMBER:
				return true;
			case OPTION_LAST_WEEK:
				return node.attr.mtime > now - WEEK;
			case OPTION_LAST_MONTH:
				return node.attr.mtime > now - MONTH;
			case OPTION_LAST_YEAR:
				return node.attr.mtime > now - YEAR;
			case OPTION_SPECIFIED:
				return node.attr.mtime >= fromTime && node.attr.mtime < toTime;
			}
		}
		return false;
    }
}
