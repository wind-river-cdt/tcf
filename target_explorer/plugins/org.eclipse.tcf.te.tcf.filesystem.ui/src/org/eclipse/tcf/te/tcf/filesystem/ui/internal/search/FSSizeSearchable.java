/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.search;

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
 * the size of a file during searching.
 */
public class FSSizeSearchable extends FSBaseSearchable {
	// Constant values of size options
	private static final int OPTION_NOT_REMEMBER = 0;
	private static final int OPTION_SIZE_SMALL = 1;
	private static final int OPTION_SIZE_MEDIUM = 2;
	private static final int OPTION_SIZE_LARGE = 3;
	private static final int OPTION_SIZE_SPECIFIED = 4;
	
	// Constant values of different size unit, used for matching purpose.
	private static final long KB = 1024;
	private static final long MB = 1024 * KB;
	
	private static final long SIZE_SMALL = 100 * KB;
	private static final long SIZE_MEDIUM = 1*MB;
	
	// The choice selected
	private int choice;
	// The lower bound of size
	private int lowerSize;
	// The upper bound of size
	private int upperSize;

	// UI elements for input
	private Button fBtnSizeNotRem;
	private Button fBtnSizeSmall;
	private Button fBtnSizeMedium;
	private Button fBtnSizeLarge;
	private Button fBtnSizeSpecified;
	private BaseEditBrowseTextControl txtSizeFrom;
	private BaseEditBrowseTextControl txtSizeTo;
	
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
		
		Composite sizeComp = createSection(parent, Messages.FSSizeSearchable_WhatSize);
		sizeComp.setLayout(new GridLayout(5, false));

		fBtnSizeNotRem = new Button(sizeComp, SWT.RADIO);
		fBtnSizeNotRem.setText(Messages.FSSizeSearchable_DontRemember);
		fBtnSizeNotRem.setSelection(true);
		GridData data = new GridData();
		data.horizontalSpan = 5;
		fBtnSizeNotRem.setLayoutData(data);
		fBtnSizeNotRem.addSelectionListener(l);

		fBtnSizeSmall = new Button(sizeComp, SWT.RADIO);
		fBtnSizeSmall.setText(Messages.FSSizeSearchable_Small);
		data = new GridData();
		data.horizontalSpan = 5;
		fBtnSizeSmall.setLayoutData(data);
		fBtnSizeSmall.addSelectionListener(l);
		
		fBtnSizeMedium = new Button(sizeComp, SWT.RADIO);
		fBtnSizeMedium.setText(Messages.FSSizeSearchable_Medium);
		data = new GridData();
		data.horizontalSpan = 5;
		fBtnSizeMedium.setLayoutData(data);
		fBtnSizeMedium.addSelectionListener(l);
		
		fBtnSizeLarge = new Button(sizeComp, SWT.RADIO);
		fBtnSizeLarge.setText(Messages.FSSizeSearchable_Large);
		data = new GridData();
		data.horizontalSpan = 5;
		fBtnSizeLarge.setLayoutData(data);
		fBtnSizeLarge.addSelectionListener(l);
		
		fBtnSizeSpecified = new Button(sizeComp, SWT.RADIO);
		fBtnSizeSpecified.setText(Messages.FSSizeSearchable_SpecifySize);
		data = new GridData();
		fBtnSizeSpecified.setLayoutData(data);
		fBtnSizeSpecified.addSelectionListener(l);

		Composite cmpFrom = new Composite(sizeComp, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		cmpFrom.setLayout(layout);
		data = new GridData();
		cmpFrom.setLayoutData(data);

		txtSizeFrom = new BaseEditBrowseTextControl(null);
		txtSizeFrom.setIsGroup(false);
		txtSizeFrom.setHasHistory(false);
		txtSizeFrom.setHideBrowseButton(true);
		txtSizeFrom.setParentControlIsInnerPanel(true);
		txtSizeFrom.setupPanel(cmpFrom);
		txtSizeFrom.setEnabled(false);
		txtSizeFrom.setEditFieldValidator(new SizeValidator());
		Text text = (Text) txtSizeFrom.getEditFieldControl();
		text.addModifyListener(new ModifyListener() {
            @Override
			public void modifyText(ModifyEvent e) {
				sizeModified();
			}
		});
		
		
		Label label = new Label(sizeComp, SWT.NONE);
		label.setText(Messages.FSSizeSearchable_ToText);
		
		Composite cmpTo = new Composite(sizeComp, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		cmpTo.setLayout(layout);
		data = new GridData();
		cmpTo.setLayoutData(data);

		txtSizeTo = new BaseEditBrowseTextControl(null);
		txtSizeTo.setIsGroup(false);
		txtSizeTo.setHasHistory(false);
		txtSizeTo.setHideBrowseButton(true);
		txtSizeTo.setParentControlIsInnerPanel(true);
		txtSizeTo.setupPanel(cmpTo);
		txtSizeTo.setEnabled(false);
		txtSizeTo.setEditFieldValidator(new SizeValidator());
		text = (Text) txtSizeTo.getEditFieldControl();
		text.addModifyListener(new ModifyListener() {
            @Override
			public void modifyText(ModifyEvent e) {
				sizeModified();
			}
		});
		
		label = new Label(sizeComp, SWT.NONE);
		label.setText(Messages.FSSizeSearchable_KBS);
    }

	/**
	 * The modified event of the size fields.
	 */
	protected void sizeModified() {
		fireOptionChanged();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.utils.AbstractSearchable#isInputValid()
	 */
	@Override
    public boolean isInputValid() {
		if(choice == OPTION_SIZE_SPECIFIED) {
			boolean vFrom = txtSizeFrom.isValid();
			boolean vTo = txtSizeTo.isValid();
			if(vFrom) {
				String fromText = txtSizeFrom.getEditFieldControlText();
				this.lowerSize = Integer.parseInt(fromText);
			}
			if(vTo) {
				String toText = txtSizeTo.getEditFieldControlText();
				this.upperSize = Integer.parseInt(toText);
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
		if(src == fBtnSizeNotRem) {
			choice = OPTION_NOT_REMEMBER;
		}
		else if(src == fBtnSizeSmall) {
			choice = OPTION_SIZE_SMALL;
		}
		else if(src == fBtnSizeMedium) {
			choice = OPTION_SIZE_MEDIUM;
		}
		else if(src == fBtnSizeLarge) {
			choice = OPTION_SIZE_LARGE;
		}
		else if(src == fBtnSizeSpecified) {
			choice = OPTION_SIZE_SPECIFIED;
			spec = true;
		}
		txtSizeFrom.setEnabled(spec);
		txtSizeTo.setEnabled(spec);
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
			switch (choice) {
			case OPTION_NOT_REMEMBER:
				return true;
			case OPTION_SIZE_SMALL:
				return node.attr.size <= SIZE_SMALL;
			case OPTION_SIZE_MEDIUM:
				return node.attr.size <= SIZE_MEDIUM;
			case OPTION_SIZE_LARGE:
				return node.attr.size > SIZE_MEDIUM;
			case OPTION_SIZE_SPECIFIED:
				return node.attr.size >= lowerSize && node.attr.size < upperSize;
			}
		}
		return false;
	}
}
