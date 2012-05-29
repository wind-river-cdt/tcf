/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.internal.utils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.ProgressMonitorPart;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.ui.interfaces.ISearchCallback;
import org.eclipse.tcf.te.ui.jface.dialogs.CustomTitleAreaDialog;
import org.eclipse.tcf.te.ui.nls.Messages;
import org.eclipse.tcf.te.ui.utils.TreeViewerUtil;

/**
 * The searching dialog used to get the searching input.
 */
public class TreeViewerSearchDialog extends CustomTitleAreaDialog implements SelectionListener, ISearchCallback {

	// A new search button's ID.
	private static final int SEARCH_ID = 31;
	
	// The input field for searching conditions.
	private Text fSearchField;
	// The radio button of depth-first algorithm.
	private Button fBtnDepth;
	// The radio button of breadth-first algorithm.
	private Button fBtnBreadth;
	// The case sensitive check box.
	private Button fBtnCase;
	// The wrap search check box.
	private Button fBtnWrap;
	// The matching rule check box.
	private Button fBtnMatch;
	// The searching orientation check box.
	private Button fBtnBackward;
	// The progress monitor part that controls the searching job.
	private ProgressMonitorPart fPmPart;
	
	// The search engine used to do the searching.
	SearchEngine fSearcher;
	// The tree viewer to be searched.
	TreeViewer fViewer;

	// The scope all button
	private Button fBtnScpAll;
	// The scope selected button
	private Button fBtnScpSel;

	/**
	 * Create a searching dialog using the default algorithm and 
	 * the default matcher.
	 * 
	 * @param viewer The tree viewer to search in.
	 */
	public TreeViewerSearchDialog(TreeViewer viewer) {
		this(viewer, false);
	}

	/**
	 * Create a searching dialog.
	 * 
	 * @param viewer The tree viewer to search in.
	 * @param depthFirst if the default algorithm used is depth-first search (DFS).
	 * @param matcher the search matcher used to matching each tree node during searching, or null 
	 * 	        if the default matcher should be used.
	 */
	protected TreeViewerSearchDialog(TreeViewer viewer, boolean depthFirst) {
		super(viewer.getTree().getShell());
		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);
		fViewer = viewer;
		fSearcher = TreeViewerUtil.getSearchEngine(fViewer);
		fSearcher.setDepthFirst(depthFirst);
		fViewer.getTree().addSelectionListener(this);
		setTitle(Messages.TreeViewerSearchDialog_DialogTitleMessage);
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.jface.dialogs.CustomTitleAreaDialog#close()
	 */
	@Override
	public boolean close() {
		fViewer.getTree().removeSelectionListener(this);
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case SEARCH_ID:
			searchButtonPressed();
			break;
		case IDialogConstants.CLOSE_ID:
			closePressed();
			break;
		default:
			super.buttonPressed(buttonId);
		}
	}

	/**
	 * Invoked when button "Close" is pressed.
	 */
	protected void closePressed() {
		fSearcher.endSearch();
		setReturnCode(OK);
		close();
	}

	/**
	 * Called when search button is pressed to start a new search.
	 */
	private void searchButtonPressed() {
		fSearcher.getMatcher().setMatchTarget(fSearchField.getText().trim());
		getButton(SEARCH_ID).setEnabled(false);
		fSearcher.startSearch(this, fPmPart);
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.TreeViewerSearchDialog_DialogTitle);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, SEARCH_ID, Messages.TreeViewerSearchDialog_BtnSearchText, true);
		createButton(parent, IDialogConstants.CLOSE_ID, Messages.TreeViewerSearchDialog_BtnCloseText, false);
		getButton(SEARCH_ID).setEnabled(false);
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.dialogs.ISearchCallback#searchDone(org.eclipse.core.runtime.IStatus, org.eclipse.jface.viewers.TreePath)
	 */
	@Override
	public void searchDone(IStatus status, TreePath path) {
		Button btn = getButton(SEARCH_ID);
		if (btn != null && !btn.isDisposed()) {
			btn.setEnabled(true);
			btn.setFocus();
		}
		if (status.isOK()) {
			if (path == null) {
				if (fSearcher.isWrap()) {
					if (fSearcher.getLastResult() == null) {
						setMessage(Messages.TreeViewerSearchDialog_NoSuchNode, IMessageProvider.WARNING);
					}
				}
				else {
					setMessage(Messages.TreeViewerSearchDialog_NoMoreNodeFound, IMessageProvider.WARNING);
				}
			}
			else {
				this.setErrorMessage(null);
				setMessage(null);
			}
		}
		else {
			this.setErrorMessage(null);
			setMessage(null);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.jface.dialogs.CustomTitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// Create the main container
		Composite composite = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(composite, SWT.NONE);
		GridLayout glayout = new GridLayout(2, false);
		glayout.marginHeight = 10;
		glayout.marginWidth = 10;
		glayout.verticalSpacing = 10;
		glayout.horizontalSpacing = 10;
		container.setLayout(glayout);
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Searching field.
		Label label = new Label(container, SWT.NONE);
		label.setText(Messages.TreeViewerSearchDialog_LblCancelText);
		fSearchField = new Text(container, SWT.SINGLE | SWT.BORDER);
		fSearchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fSearchField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateSearchButton();
				fSearcher.resetPath();
			}
		});
		
		SelectionListener l = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				optionChecked(e);
			}
		};
		
		// Search Algoritm Selection Group.
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.TreeViewerSearchDialog_Scope);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		group.setLayout(new GridLayout(2, false));
		
		fBtnScpAll = new Button(group, SWT.RADIO);
		fBtnScpAll.setText(Messages.TreeViewerSearchDialog_All);
		fBtnScpAll.setSelection(fSearcher.isScopeAll());
		fBtnScpAll.addSelectionListener(l);
		fBtnScpAll.setLayoutData(new GridData());
		
		fBtnScpSel = new Button(group, SWT.RADIO);
		fBtnScpSel.setText(Messages.TreeViewerSearchDialog_Selected);
		fBtnScpSel.setSelection(!fSearcher.isScopeAll());
		fBtnScpSel.addSelectionListener(l);
		fBtnScpSel.setLayoutData(new GridData());
		
		// Search Algoritm Selection Group.
		group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.TreeViewerSearchDialog_SearchAlgorithm);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		group.setLayout(new GridLayout(2, false));
		
		// Breadth-first search
		fBtnBreadth = new Button(group, SWT.RADIO);
		fBtnBreadth.setText(Messages.TreeViewerSearchDialog_BreadthFirst);
		fBtnBreadth.setSelection(!fSearcher.isDepthFirst());
		fBtnBreadth.addSelectionListener(l);
		fBtnBreadth.setLayoutData(new GridData());
		
		// Depth-first search
		fBtnDepth = new Button(group, SWT.RADIO);
		fBtnDepth.setText(Messages.TreeViewerSearchDialog_DepthFirst);
		fBtnDepth.setSelection(fSearcher.isDepthFirst());
		fBtnDepth.addSelectionListener(l);
		fBtnDepth.setLayoutData(new GridData());
		
		// Search Options Group
		group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.TreeViewerSearchDialog_GrpOptionsText);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		group.setLayout(new GridLayout(4, false));

		// Case sensitive
		fBtnCase = new Button(group, SWT.CHECK);
		fBtnCase.setText(Messages.TreeViewerSearchDialog_BtnCaseText);
		fBtnCase.addSelectionListener(l);
		
		// Matching precisely
		fBtnMatch = new Button(group, SWT.CHECK);
		fBtnMatch.setText(Messages.TreeViewerSearchDialog_BtnPreciseText);
		fBtnMatch.addSelectionListener(l);
		
		// Wrap search
		fBtnWrap = new Button(group, SWT.CHECK);
		fBtnWrap.setText(Messages.TreeViewerSearchDialog_BtnWrapText);
		fBtnWrap.addSelectionListener(l);
		
		// Search backward.
		fBtnBackward = new Button(group, SWT.CHECK);
		fBtnBackward.setText(Messages.TreeViewerSearchDialog_BtnBackText);
		fBtnBackward.addSelectionListener(l);
		// Hidden if it is breadth-first search
		fBtnBackward.setVisible(fSearcher.isDepthFirst());
		
		// Progress monitor part to display or cancel searching process.
		fPmPart = new ProgressMonitorPart(container, null, true);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		fPmPart.setLayoutData(data);
		fPmPart.setVisible(false);
		
		return composite;
	}

	/**
	 * Event handler to process a button selection event.
	 * 
	 * @param e The selection event.
	 */
	void optionChecked(SelectionEvent e) {
		Object src = e.getSource();
		if (src == fBtnCase) {
			fSearcher.getMatcher().setCaseSensitive(fBtnCase.getSelection());
		}
		else if (src == fBtnWrap) {
			fSearcher.setWrap(fBtnWrap.getSelection());
		}
		else if (src == fBtnMatch) {
			fSearcher.getMatcher().setMatchPrecise(fBtnMatch.getSelection());
		}
		else if (src == fBtnBackward) {
			fSearcher.endSearch();
			fSearcher.setStartPath(fSearcher.getLastResult());
			fSearcher.setForeward(!fBtnBackward.getSelection());
		}
		else if (src == fBtnDepth || src == fBtnBreadth) {
			if (src == fBtnDepth) {
				fBtnDepth.setSelection(true);
				fBtnBreadth.setSelection(false);
			}
			else if (src == fBtnBreadth) {
				fBtnBreadth.setSelection(true);
				fBtnDepth.setSelection(false);
			}
			fSearcher.endSearch();
			boolean selection = fBtnDepth.getSelection();
			fSearcher.setDepthFirst(selection);
			fBtnBackward.setVisible(selection);
			fSearcher.resetPath();
			fSearcher.setForeward(!fBtnBackward.getSelection());
		}
		else if (src == fBtnScpAll || src == fBtnScpSel) {
			if(src == fBtnScpAll) {
				fBtnScpAll.setSelection(true);
				fBtnScpSel.setSelection(false);
			}
			else {
				fBtnScpAll.setSelection(false);
				fBtnScpSel.setSelection(true);
			}
			fSearcher.endSearch();
			boolean scpAll = fBtnScpAll.getSelection();
			if(scpAll) {
				setStartPath(new TreePath(new Object[]{fViewer.getInput()}));
			}
			else {
				treeSelected();
			}
		}
	}

	/**
	 * Update the enablement of search button.
	 */
	void updateSearchButton() {
		String txt = fSearchField.getText();
		boolean valid = txt != null && txt.trim().length() > 0;
		getButton(SEARCH_ID).setEnabled(valid);
	}

	/**
	 * Set the start searching path.
	 * 
	 * @param rootPath The path where to start searching.
	 */
	public void setStartPath(TreePath rootPath) {
		fSearcher.setStartPath(rootPath);
		String text = fSearcher.getMatcher().getElementText(rootPath.getLastSegment());
		if(text != null) {
			this.setDefaultMessage(NLS.bind(Messages.TreeViewerSearchDialog_DialogPromptMessage, text), NONE);
		}
		else {
			this.setDefaultMessage(Messages.TreeViewerSearchDialog_RootMsg, NONE);
		}
		updateScope();
    }
	
	/**
	 * Update the state of the scope buttons
	 */
	private void updateScope() {
		if (fBtnScpAll != null && fBtnScpSel != null && fSearcher != null) {
			fBtnScpAll.setSelection(fSearcher.isScopeAll());
			fBtnScpSel.setSelection(!fSearcher.isScopeAll());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
    public void widgetSelected(SelectionEvent e) {
		treeSelected();
	}

	/**
	 * Invoked while a tree node is selected.
	 */
	private void treeSelected() {
	    fSearcher.endSearch();
		ISelection sel = fViewer.getSelection();
		if (sel == null || sel.isEmpty()) {
			fSearcher.resetPath();
			updateScope();
		}
		else {
			TreeSelection iss = (TreeSelection) sel;
			TreePath[] paths = iss.getPaths();
			if (paths == null || paths.length == 0) {
				fSearcher.resetPath();
				updateScope();
			}
			else {
				setStartPath(paths[0]);
			}
		}
		fSearcher.setLastResult(null);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }
}
