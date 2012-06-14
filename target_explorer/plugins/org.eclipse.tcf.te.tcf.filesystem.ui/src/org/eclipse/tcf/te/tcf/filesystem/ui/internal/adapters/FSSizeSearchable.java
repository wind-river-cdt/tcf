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

public class FSSizeSearchable extends FSBaseSearchable {
	private static final int OPTION_NOT_REMEMBER = 0;
	private static final int OPTION_SIZE_SMALL = 1;
	private static final int OPTION_SIZE_MEDIUM = 2;
	private static final int OPTION_SIZE_LARGE = 3;
	private static final int OPTION_SIZE_SPECIFIED = 4;
	
	private static final long KB = 1024;
	private static final long MB = 1024 * KB;
	
	private static final long SIZE_SMALL = 100 * KB;
	private static final long SIZE_MEDIUM = 1*MB;
	
	private int option;
	private int lowSize;
	private int topSize;

	private Button fBtnSizeNotRem;
	private Button fBtnSizeSmall;
	private Button fBtnSizeMedium;
	private Button fBtnSizeLarge;
	private Button fBtnSizeSpecified;
	private Text txtSizeFrom;
	private Text txtSizeTo;

	public FSSizeSearchable(FSTreeNode node) {
    }
	
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
		
		txtSizeFrom = new Text(sizeComp, SWT.BORDER | SWT.SINGLE);
		data = new GridData();
		data.widthHint = 50;
		txtSizeFrom.setLayoutData(data);
		txtSizeFrom.setEnabled(false);
		
		Label label = new Label(sizeComp, SWT.NONE);
		label.setText(Messages.FSSizeSearchable_ToText);
		
		txtSizeTo = new Text(sizeComp, SWT.BORDER | SWT.SINGLE);
		data = new GridData();
		data.widthHint = 50;
		txtSizeTo.setLayoutData(data);
		txtSizeTo.setEnabled(false);
		
		label = new Label(sizeComp, SWT.NONE);
		label.setText(Messages.FSSizeSearchable_KBS);
    }

	protected void optionChecked(SelectionEvent e) {
		Object src = e.getSource();
		boolean spec = false;
		if(src == fBtnSizeNotRem) {
			option = OPTION_NOT_REMEMBER;
		}
		else if(src == fBtnSizeSmall) {
			option = OPTION_SIZE_SMALL;
		}
		else if(src == fBtnSizeMedium) {
			option = OPTION_SIZE_MEDIUM;
		}
		else if(src == fBtnSizeLarge) {
			option = OPTION_SIZE_LARGE;
		}
		else if(src == fBtnSizeSpecified) {
			option = OPTION_SIZE_SPECIFIED;
			spec = true;
		}
		txtSizeFrom.setEnabled(spec);
		txtSizeTo.setEnabled(spec);
    }
	
	@Override
	public boolean match(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) element;
			switch (option) {
			case OPTION_NOT_REMEMBER:
				return true;
			case OPTION_SIZE_SMALL:
				return node.attr.size <= SIZE_SMALL;
			case OPTION_SIZE_MEDIUM:
				return node.attr.size <= SIZE_MEDIUM;
			case OPTION_SIZE_LARGE:
				return node.attr.size > SIZE_MEDIUM;
			case OPTION_SIZE_SPECIFIED:
				return node.attr.size >= lowSize && node.attr.size < topSize;
			}
		}
		return false;
	}
}
