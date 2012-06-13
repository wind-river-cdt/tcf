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

import org.eclipse.osgi.util.NLS;
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
import org.eclipse.tcf.te.ui.interfaces.AbstractSearchable;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;

/**
 * The ISearchable adapter for a FSTreeNode which creates a UI for the user to 
 * input the matching condition and returns a matcher to do the matching.
 */
public class FSTreeNodeSearchable extends AbstractSearchable implements ISearchMatcher {
	// The case sensitive check box.
	Button fBtnCase;
	// The matching rule check box.
	Button fBtnMatch;
	// The types of target files.
	Combo fBtnFileOnly;
	// The input field for searching conditions.
	Text fSearchField;
	// Whether it is case sensitive
	boolean fCaseSensitive;
	// Whether it is precise matching.
	boolean fMatchPrecise;
	// The current selected target type index.
	int fTargetType;
	// The current target names.
	String fTargetName;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#createPart(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPart(Composite container) {
		Composite comp = new Composite(container, SWT.NONE);
		GridLayout glayout = new GridLayout(2, false);
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		comp.setLayout(glayout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Searching field.
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.TreeViewerSearchDialog_LblCancelText);
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

		// Search Options Group
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.TreeViewerSearchDialog_GrpOptionsText);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		group.setLayoutData(data);
		group.setLayout(new GridLayout(2, false));
		
		label = new Label(group, SWT.NONE);
		label.setText(Messages.FSTreeNodeSearchable_SearchingTargets);
		
		// Search files only
		fBtnFileOnly = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
		fBtnFileOnly.setItems(new String[]{Messages.FSTreeNodeSearchable_FilesAndFolders, Messages.FSTreeNodeSearchable_FilesOnly, Messages.FSTreeNodeSearchable_FoldersOnly});
		fBtnFileOnly.setLayoutData(new GridData());
		fBtnFileOnly.select(0);
		fBtnFileOnly.addSelectionListener(l);

		// Case sensitive
		fBtnCase = new Button(group, SWT.CHECK);
		fBtnCase.setText(Messages.TreeViewerSearchDialog_BtnCaseText);
		fBtnCase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fBtnCase.addSelectionListener(l);

		// Matching precisely
		fBtnMatch = new Button(group, SWT.CHECK);
		fBtnMatch.setText(Messages.TreeViewerSearchDialog_BtnPreciseText);
		fBtnMatch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fBtnMatch.addSelectionListener(l);
	}

	/**
	 * The text for searching is modified.
	 */
	protected void searchTextModified() {
		fireOptionChanged();
		fTargetName = fSearchField.getText().trim();
    }

	/**
	 * An option has been selected.
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
		else if (src == fBtnFileOnly) {
			fTargetType = fBtnFileOnly.getSelectionIndex();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getMatcher()
	 */
	@Override
	public ISearchMatcher getMatcher() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#isInputValid()
	 */
	@Override
	public boolean isInputValid() {
		String txt = fSearchField.getText();
		boolean valid = txt != null && txt.trim().length() > 0;
		return valid;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchMatcher#match(java.lang.Object)
	 */
	@Override
	public boolean match(Object context) {
		if (context == null) return false;
		if (context instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) context;
			if(fTargetType == 1 && !node.isFile() || fTargetType == 2 && !node.isDirectory()) return false;
			String text = node.name;
			if (text == null) return false;
			String target = fTargetName;
			if (!fCaseSensitive) {
				text = text.toLowerCase();
				target = target != null ? target.toLowerCase() : null;
			}
			if (fMatchPrecise) return text.equals(target);
			return text.indexOf(target) != -1;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getSearchTitle()
	 */
	@Override
    public String getSearchTitle() {
	    return Messages.FSTreeNodeSearchable_FindFilesAndFolders;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getSearchMessage(java.lang.Object)
	 */
	@Override
    public String getSearchMessage(Object rootElement) {
		String message = Messages.FSTreeNodeSearchable_FindMessage;
		String rootName = getElementName(rootElement);
		message = NLS.bind(message, rootName);
		return message;
    }

	/**
	 * Get a name representation for each file node.
	 * 
	 * @param rootElement The root element whose name is being retrieved.
	 * @return The node's name or an expression for the file system.
	 */
	private String getElementName(Object rootElement) {
		if(rootElement == null) {
			return Messages.FSTreeNodeSearchable_SelectedFileSystem;
		}
		FSTreeNode rootNode = (FSTreeNode) rootElement;
		if(rootNode.isSystemRoot()) {
			return Messages.FSTreeNodeSearchable_SelectedFileSystem;
		}
		return rootNode.name;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchable#getElementText(java.lang.Object)
	 */
	@Override
    public String getElementText(Object element) {
	    return getElementName(element);
    }
}
