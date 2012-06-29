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

import java.util.EventObject;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.interfaces.IOptionListener;
import org.eclipse.tcf.te.ui.interfaces.IPreferenceKeys;
import org.eclipse.tcf.te.ui.interfaces.ISearchCallback;
import org.eclipse.tcf.te.ui.interfaces.ISearchable;
import org.eclipse.tcf.te.ui.jface.dialogs.CustomTitleAreaDialog;
import org.eclipse.tcf.te.ui.nls.Messages;

/**
 * The searching dialog used to get the searching input.
 */
public class TreeViewerSearchDialog extends CustomTitleAreaDialog implements ISearchCallback, IOptionListener, IPreferenceKeys {
	// The context help id for this dialog.
	private static final String SEARCH_HELP_ID = "org.eclipse.tcf.te.ui.utils.TreeViewerSearchDialog.help"; //$NON-NLS-1$
	// A new search button's ID.
	private static final int SEARCH_ID = 31;
	private static final int DEFAULT_WIDTH_TRIM = 20;
	private static final int DEFAULT_HEIGHT_TRIM = 160;
	
	// The dropdown combo box to select an algorithm
	private Combo fCmbAlg;
	// The searching orientation check box.
	private Button fBtnBackward;
	// The wrap search check box.
	private Button fBtnWrap;
	// The progress monitor part that controls the searching job.
	private ProgressMonitorPart fPmPart;
	// The search engine used to do the searching.
	SearchEngine fSearcher;
	// The tree viewer to be searched.
	TreeViewer fViewer;
	// The searchable of the currently selected element.
	ISearchable fSearchable;

	/**
	 * Create a searching dialog using the default algorithm and 
	 * the default matcher.
	 * 
	 * @param viewer The tree viewer to search in.
	 */
	public TreeViewerSearchDialog(TreeViewer viewer) {
		super(viewer.getTree().getShell());
		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);
		setHelpAvailable(true);
		setContextHelpId(SEARCH_HELP_ID);
		fViewer = viewer;
		boolean depthFirst = isDepthFirst();
		fSearcher = getSearchEngine(fViewer, depthFirst);
	}

	/**
	 * If the algorithm of the search is DFS.
	 * 
	 * @return true if it is a DFS search or else false if it is BFS
	 */
	protected boolean isDepthFirst() {
		return UIPlugin.getDefault().getPreferenceStore().getBoolean(PREF_DEPTH_FIRST_SEARCH);
    }

	/**
	 * Get a singleton search engine for a tree viewer. If
	 * it does not exist then create one and store it.
	 * 
	 * @param viewer The tree viewer.
	 * @return A search engine.
	 */
	private SearchEngine getSearchEngine(TreeViewer viewer, boolean depthFirst) {
		SearchEngine searcher = (SearchEngine) viewer.getData("search.engine"); //$NON-NLS-1$
		if (searcher == null) {
			searcher = new SearchEngine(viewer, depthFirst);
			viewer.setData("search.engine", searcher); //$NON-NLS-1$
		}
		else {
			searcher.setDepthFirst(depthFirst);
		}
		return searcher;
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
		if(fSearchable != null) {
			fSearchable.removeOptionListener(this);
			fSearchable.persistValues(getDialogSettings());
		}
		fSearcher.endSearch();
		setReturnCode(OK);
		close();
	}

	/**
	 * Called when search button is pressed to start a new search.
	 */
	private void searchButtonPressed() {
		getButton(SEARCH_ID).setEnabled(false);
		fSearcher.startSearch(this, fPmPart);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, SEARCH_ID, Messages.TreeViewerSearchDialog_BtnSearchText, true);
		createButton(parent, IDialogConstants.CLOSE_ID, Messages.TreeViewerSearchDialog_BtnCloseText, false);
		if (fSearchable != null) {
			fSearchable.restoreValues(getDialogSettings());
		}
		updateButtonState();
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
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if(fSearchable != null) {
			fSearchable.createCommonPart(container);
			fSearchable.createAdvancedPart(container);
		}
		
		// Progress monitor part to display or cancel searching process.
		fPmPart = new ProgressMonitorPart(container, null, true);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		fPmPart.setLayoutData(data);
		fPmPart.setVisible(false);
		
		if(fSearchable != null) {
			String title = fSearchable.getSearchTitle();
			getShell().setText(title);
			this.setTitle(title);
		}
		return composite;
	}

    /**
     * Create the part to input the general search options.
     * 
     * @param container The parent container
     */
	protected void createGeneralOptions(Composite container) {
	    SelectionListener l = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectionChanged(e);
			}
		};
		
		Group generalGroup = new Group(container, SWT.NONE);
		generalGroup.setText(Messages.TreeViewerSearchDialog_GeneralOptions);
		generalGroup.setLayout(new GridLayout(3, false));
		generalGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label label = new Label(generalGroup, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Messages.TreeViewerSearchDialog_SearchNodesUsing);
		
		fCmbAlg = new Combo(generalGroup, SWT.BORDER | SWT.READ_ONLY);
		fCmbAlg.setLayoutData(new GridData());
		fCmbAlg.setItems(new String[]{Messages.TreeViewerSearchDialog_BFS, Messages.TreeViewerSearchDialog_DFS});
		fCmbAlg.select(0); 
		fCmbAlg.addSelectionListener(l);
		
		label = new Label(generalGroup, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		label.setLayoutData(data);
		label.setText(Messages.TreeViewerSearchDialog_UseOptions);
		
		// Wrap search
		fBtnWrap = new Button(generalGroup, SWT.CHECK);
		fBtnWrap.setText(Messages.TreeViewerSearchDialog_BtnWrapText);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 3;
		data.horizontalIndent = 10;
		fBtnWrap.setLayoutData(data);
		fBtnWrap.addSelectionListener(l);
		
		// Search backward.
		fBtnBackward = new Button(generalGroup, SWT.CHECK);
		fBtnBackward.setText(Messages.TreeViewerSearchDialog_BtnBackText);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 3;
		data.horizontalIndent = 10;
		fBtnBackward.setLayoutData(data);
		fBtnBackward.addSelectionListener(l);
		// Hidden if it is breadth-first search
		fBtnBackward.setEnabled(fSearcher.isDepthFirst());
    }

	/**
	 * Get the searchable of the current selected path.
	 * 
	 * @return A searchable object or null if null if cannot be adapted to a searchable.
	 */
	private ISearchable getSearchable() {
		TreePath path = fSearcher.getStartPath();
		if(path != null) {
			Object element = path.getLastSegment();
			if(element != null) {
				if(element instanceof ISearchable) {
					return (ISearchable) element;
				}
				ISearchable searchable = null;
				if(element instanceof IAdaptable) {
					searchable = (ISearchable)((IAdaptable)element).getAdapter(ISearchable.class);
				}
				if(searchable == null) {
					searchable = (ISearchable)Platform.getAdapterManager().getAdapter(element, ISearchable.class);
				}
				return searchable;
			}
		}
		return null;
	}

	/**
	 * Event handler to process a button selection event.
	 * 
	 * @param e The selection event.
	 */
	void selectionChanged(SelectionEvent e) {
		Object src = e.getSource();
		if (src == fBtnBackward) {
			fSearcher.endSearch();
			fSearcher.setStartPath(fSearcher.getLastResult());
			fSearcher.setForeward(!fBtnBackward.getSelection());
		}
		else if (src == fBtnWrap) {
			fSearcher.setWrap(fBtnWrap.getSelection());
		}
		else if (src == fCmbAlg) {
			int index = fCmbAlg.getSelectionIndex();
			fSearcher.endSearch();
			boolean selection = index == 1;
			fSearcher.setDepthFirst(selection);
			fBtnBackward.setEnabled(selection);
			fSearcher.resetPath();
			fSearcher.setForeward(!fBtnBackward.getSelection());
		}
	}

	/**
	 * Set the start searching path.
	 * 
	 * @param rootPath The path where to start searching.
	 */
	public void setStartPath(TreePath rootPath) {
		fSearcher.setStartPath(rootPath);
		fSearchable = getSearchable();
		if (fSearchable != null) {
			fSearchable.addOptionListener(this);
			fSearcher.setSearchable(fSearchable);
			Object element = rootPath.getLastSegment();
			String text = fSearchable.getSearchMessage(element);
			if (text != null) {
				setDefaultMessage(text, NONE);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#getInitialSize()
	 */
	@Override
    protected Point getInitialSize() {
		Point size = fSearchable.getPreferredSize();
		if(size != null) {
			int width = size.x + DEFAULT_WIDTH_TRIM;
			int height = size.y + DEFAULT_HEIGHT_TRIM;
			return new Point(width, height);
		}
	    return super.getInitialSize();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.IOptionListener#optionChanged(java.util.EventObject)
	 */
	@Override
    public void optionChanged(EventObject event) {
		fSearcher.resetPath();
		updateButtonState();
    }

	/**
	 * Update the button's state according to 
	 */
	protected void updateButtonState() {
		Button button = getButton(SEARCH_ID);
		if (button != null) {
			button.setEnabled(fSearchable != null && fSearchable.isInputValid());
		}
    }
}
