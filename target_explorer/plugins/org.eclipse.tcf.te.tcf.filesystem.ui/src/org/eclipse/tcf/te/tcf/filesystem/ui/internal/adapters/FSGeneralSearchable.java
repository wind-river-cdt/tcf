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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.utils.AbstractSearchable;

public class FSGeneralSearchable extends AbstractSearchable {
	// The case sensitive check box.
	Button fBtnCase;
	// The matching rule check box.
	Button fBtnMatch;
	// The input field for searching conditions.
	protected Text fSearchField;
	// The current target names.
	protected String fTargetName;
	// Whether it is case sensitive
	boolean fCaseSensitive;
	// Whether it is precise matching.
	boolean fMatchPrecise;
	// 
	boolean fIncludeSystem = true;
	boolean fIncludeHidden = true;
	// The types of target files.
	Combo fCmbTypes;
	// The current selected target type index.
	int fTargetType;

	// The root directory node.
	FSTreeNode rootNode;
	private Button fBtnSystem;
	private Button fBtnHidden;
	public FSGeneralSearchable(FSTreeNode node) {
		rootNode = node;
	}
	@Override
	public void createCommonPart(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout glayout = new GridLayout(2, false);
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		comp.setLayout(glayout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Searching field.
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.FSGeneralSearchable_Find);
		fSearchField = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fSearchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fSearchField.addModifyListener(new ModifyListener() {
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
		group.setText(Messages.FSGeneralSearchable_GeneralOptionText);
		group.setLayout(new GridLayout(2, true));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite cmpType = new Composite(group, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		cmpType.setLayoutData(data);
		cmpType.setLayout(new GridLayout(2, false));
		
		label = new Label(cmpType, SWT.NONE);
		label.setText(Messages.FSGeneralSearchable_FileType);
		
		// Search files only
		fCmbTypes = new Combo(cmpType, SWT.BORDER | SWT.READ_ONLY);
		fCmbTypes.setItems(new String[]{Messages.FSTreeNodeSearchable_FilesAndFolders, Messages.FSTreeNodeSearchable_FilesOnly, Messages.FSTreeNodeSearchable_FoldersOnly});
		fCmbTypes.setLayoutData(new GridData());
		fCmbTypes.select(0);
		fCmbTypes.addSelectionListener(l);
		
		// Case sensitive
		fBtnCase = new Button(group, SWT.CHECK);
		fBtnCase.setText(Messages.TreeViewerSearchDialog_BtnCaseText);
		data = new GridData();
		fBtnCase.setLayoutData(data);
		fBtnCase.addSelectionListener(l);

		// Matching precisely
		fBtnMatch = new Button(group, SWT.CHECK);
		fBtnMatch.setText(Messages.TreeViewerSearchDialog_BtnPreciseText);
		data = new GridData();
		fBtnMatch.setLayoutData(data);
		fBtnMatch.addSelectionListener(l);
		
		if(rootNode.isWindowsNode()) {
			fBtnSystem = new Button(group, SWT.CHECK);
			fBtnSystem.setText(Messages.FSGeneralSearchable_SearchSystemFiles);
			fBtnSystem.setSelection(true);
			data = new GridData();
			fBtnSystem.setLayoutData(data);
			fBtnSystem.addSelectionListener(l);
			
			fBtnHidden = new Button(group, SWT.CHECK);
			fBtnHidden.setText(Messages.FSGeneralSearchable_SearchHiddenFiles);
			fBtnHidden.setSelection(true);
			data = new GridData();
			fBtnHidden.setLayoutData(data);
			fBtnHidden.addSelectionListener(l);
		}
	}
	/**
	 * The text for searching is modified.
	 */
	protected void searchTextModified() {
		fireOptionChanged();
		fTargetName = fSearchField.getText().trim();
    }
	
	protected void optionChecked(SelectionEvent e) {
		Object src = e.getSource();
		if (src == fBtnCase) {
			fCaseSensitive = fBtnCase.getSelection();
		}
		else if (src == fBtnMatch) {
			fMatchPrecise = fBtnMatch.getSelection();
		}
		else if (src == fCmbTypes) {
			fTargetType = fCmbTypes.getSelectionIndex();
		}
		else if (src == fBtnSystem) {
			fIncludeSystem = fBtnSystem.getSelection();
		}
		else if (src == fBtnHidden) {
			fIncludeHidden = fBtnHidden.getSelection();
		}
    }

	@Override
	public ISearchMatcher getMatcher() {
		return new FSTreeNodeMatcher(fCaseSensitive, fMatchPrecise, fTargetType, fTargetName, fIncludeSystem, fIncludeHidden);
	}

	@Override
	public boolean isInputValid() {
		String txt = fSearchField.getText();
		boolean valid = txt != null && txt.trim().length() > 0;
		return valid;
	}
}
