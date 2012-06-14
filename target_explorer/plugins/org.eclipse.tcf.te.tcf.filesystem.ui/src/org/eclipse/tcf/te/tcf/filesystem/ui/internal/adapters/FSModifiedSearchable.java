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
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.adapters;

import org.eclipse.swt.SWT;
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

public class FSModifiedSearchable extends FSBaseSearchable {
	private static final int OPTION_NOT_REMEMBER = 0;
	private static final int OPTION_LAST_WEEK = 1;
	private static final int OPTION_LAST_MONTH = 2;
	private static final int OPTION_LAST_YEAR = 3;
	private static final int OPTION_SPECIFIED = 4;
	
	private static final long SECOND = 1000L;
	private static final long MINUTE = 60 * SECOND;
	private static final long HOUR = 60 * MINUTE;
	private static final long DAY = 24 * HOUR;
	private static final long WEEK = 7 * DAY;
	private static final long MONTH = 30 * DAY;
	private static final long YEAR = 365 * DAY;
	
	private int option;
	private long lowDate;
	private long topDate;

	private Button fBtnLmNotRem;
	private Button fBtnLmLastWeek;
	private Button fBtnLmPastMonth;
	private Button fBtnLmPastYear;
	private Button fBtnLmSpecified;
	private Text txtLmFrom;
	private Text txtLmTo;

	public FSModifiedSearchable(FSTreeNode node) {
    }
	
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
		
		txtLmFrom = new Text(modifiedComp, SWT.BORDER | SWT.SINGLE);
		data = new GridData();
		data.widthHint = 50;
		txtLmFrom.setLayoutData(data);
		txtLmFrom.setEnabled(false);
		
		Label label = new Label(modifiedComp, SWT.NONE);
		label.setText(Messages.FSModifiedSearchable_ToDate);
		
		txtLmTo = new Text(modifiedComp, SWT.BORDER | SWT.SINGLE);
		data = new GridData();
		data.widthHint = 50;
		txtLmTo.setLayoutData(data);
		txtLmTo.setEnabled(false);
    }

	protected void optionChecked(SelectionEvent e) {
		Object src = e.getSource();
		boolean spec = false;
		if(src == fBtnLmNotRem) {
			option = OPTION_NOT_REMEMBER;
		}
		else if(src == fBtnLmLastWeek) {
			option = OPTION_LAST_WEEK;
		}
		else if(src == fBtnLmPastMonth) {
			option = OPTION_LAST_MONTH;
		}
		else if(src == fBtnLmPastYear) {
			option = OPTION_LAST_YEAR;
		}
		else if(src == fBtnLmSpecified) {
			option = OPTION_SPECIFIED;
			spec = true;
		}
		txtLmFrom.setEnabled(spec);
		txtLmTo.setEnabled(spec);
    }
	
	@Override
    public boolean match(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) element;
			long now = System.currentTimeMillis();
			switch (option) {
			case OPTION_NOT_REMEMBER:
				return true;
			case OPTION_LAST_WEEK:
				return node.attr.mtime > now - WEEK;
			case OPTION_LAST_MONTH:
				return node.attr.mtime > now - MONTH;
			case OPTION_LAST_YEAR:
				return node.attr.mtime > now - YEAR;
			case OPTION_SPECIFIED:
				return node.attr.mtime >= lowDate && node.attr.mtime < topDate;
			}
		}
		return false;
    }
}
