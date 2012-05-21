/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.internal.AbstractSearcher;
import org.eclipse.tcf.te.ui.internal.DepthFirstSearcher;
import org.eclipse.tcf.te.ui.internal.BreadthFirstSearcher;
import org.eclipse.tcf.te.ui.jface.dialogs.CustomTitleAreaDialog;
import org.eclipse.tcf.te.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * The searching dialog used to get the searching input.
 */
public class TreeViewerSearchDialog extends CustomTitleAreaDialog implements SelectionListener, ISearchMatcher {

	// A new search button's ID.
	private static final int SEARCH_ID = 31;
	
	// The viewer being searched.
	TreeViewer fViewer;
	// The input field for searching conditions.
	private Text fSearchField;
	// The text to be searched.
	private String fSearchTarget;
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
	ProgressMonitorPart fPmPart;
	
	// Whether it is case sensitive
	private boolean fCaseSensitive;
	// Whether it is wrap search.
	private boolean fWrap;
	// Whether it is precise matching.
	private boolean fMatch;
	// The last result being searched.
	private TreePath fLastResult;
	// The current starting path of the searcher engine.
	TreePath fStartPath;
	// If the search algorithm is depth preferable
	private boolean fDepthFirst;
	// The search engine used search the tree
	AbstractSearcher fSearcher;
	// The searching job.
	protected Job fSearchJob;
	// The search matcher used to match tree nodes during traversing.
	private ISearchMatcher fMatcher;

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
	 * Create a searching dialog using the default matcher.
	 * 
	 * @param viewer The tree viewer to search in.
	 * @param depthFirst if the default algorithm used is depth-first search (DFS).
	 */
	public TreeViewerSearchDialog(TreeViewer viewer, boolean depthFirst) {
		this(viewer, depthFirst, null);
	}

	/**
	 * Create a searching dialog.
	 * 
	 * @param viewer The tree viewer to search in.
	 * @param depthFirst if the default algorithm used is depth-first search (DFS).
	 * @param matcher the search matcher used to matching each tree node during searching, or null 
	 * 	        if the default matcher should be used.
	 */
	protected TreeViewerSearchDialog(TreeViewer viewer, boolean depthFirst, ISearchMatcher matcher) {
		super(viewer.getTree().getShell());
		fDepthFirst = depthFirst;
		fViewer = viewer;
		fViewer.getTree().addSelectionListener(this);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);
		if (matcher == null) matcher = this;
		fMatcher = matcher;
		fSearcher = fDepthFirst ? new DepthFirstSearcher(fViewer, fMatcher) : new BreadthFirstSearcher(fViewer, fMatcher);
		this.setTitle(Messages.TreeViewerSearchDialog_DialogTitleMessage);
	}

	/**
	 * Set the initial path to search from.
	 * 
	 * @param path The path from which to start the search.
	 */
	public void setStartPath(TreePath path) {
		fStartPath = path;
		fSearcher.setStartPath(path);
		if(fSearcher != null) {
			String text = getElementText(path.getLastSegment());
			this.setDefaultMessage(NLS.bind(Messages.TreeViewerSearchDialog_DialogPromptMessage, text), NONE);
		}
	}
	
	/**
	 * Get the text representation of a element using the label provider
	 * of the tree viewer. 
	 * Note: this method could be called at any thread.
	 * 
	 * @param element The element.
	 * @return The text representation.
	 */
	String getElementText(final Object element) {
		if (Display.getCurrent() != null) {
			if (element == fViewer.getInput()) return "the root"; //$NON-NLS-1$
			ILabelProvider labelProvider = (ILabelProvider) fViewer.getLabelProvider();
			if (labelProvider != null) {
				return labelProvider.getText(element);
			}
			return element == null ? "" : element.toString(); //$NON-NLS-1$
		}
		final String[] result = new String[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				result[0] = getElementText(element);
			}
		});
		return result[0];
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
		default:
			super.buttonPressed(buttonId);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		if (fSearchJob != null) {
			fSearchJob.cancel();
			fSearchJob = null;
		}
		super.cancelPressed();
	}

	/**
	 * Called when search button is pressed to start a new search.
	 */
	private void searchButtonPressed() {
		fSearchTarget = fSearchField.getText().trim();
		getButton(SEARCH_ID).setEnabled(false);
		final TreePath[] result = new TreePath[1];
		fSearchJob = new Job(Messages.TreeViewerSearchDialog_JobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor = new DelegateProgressMonitor(monitor, fPmPart);
				monitor.beginTask(Messages.TreeViewerSearchDialog_MainTaskName, IProgressMonitor.UNKNOWN);
				try {
					result[0] = fSearcher.searchNext(monitor);
				}
				catch (InvocationTargetException e) {
					Status status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), e.getMessage(), e);
					return status;
				}
				catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
				finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		fSearchJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				fViewer.getTree().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						searchDone(event.getResult(), result[0]);
					}
				});
			}
		});
		fSearchJob.schedule();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchMatcher#match(java.lang.Object)
	 */
	@Override
	public boolean match(Object context) {
		if (context == null) return false;
		String text = getElementText(context);
		if (text == null) return false;
		String target = fSearchTarget;
		if (!fCaseSensitive) {
			text = text.toLowerCase();
			target = fSearchTarget.toLowerCase();
		}
		if (fMatch) return text.equals(target);
		return text.indexOf(target) != -1;
	}

	/**
	 * The callback invoked when the searching job is done, to process
	 * the path found.
	 * 
	 * @param status The searching resulting status.
	 * @param path The tree path found or null if no appropriate node is found.
	 */
	void searchDone(IStatus status, TreePath path) {
		Button btn = getButton(SEARCH_ID);
		if (btn != null && !btn.isDisposed()) {
			btn.setEnabled(true);
			btn.setFocus();
		}
		if (status.isOK()) {
			if (path == null) {
				if (fWrap) {
					if (fLastResult == null) {
						setErrorMessage(Messages.TreeViewerSearchDialog_NoSuchNode);
					}
					else {
						setStartPath(fLastResult);
						fLastResult = null;
						buttonPressed(SEARCH_ID);
						return;
					}
				}
				else {
					setErrorMessage(Messages.TreeViewerSearchDialog_NoSuchNode);
				}
			}
			else {
				setMessage(null);
				fLastResult = path;
				fViewer.expandToLevel(path, 0);
				fViewer.setSelection(new StructuredSelection(new Object[] { path }), true);
			}
		}
		else {
			setMessage(null);
		}
		fSearchJob = null;
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
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.TreeViewerSearchDialog_BtnCancelText, false);
		getButton(SEARCH_ID).setEnabled(false);
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
				setStartPath(fStartPath);
			}
		});
		
		// Search Algoritm Selection Group.
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.TreeViewerSearchDialog_SearchAlgorithm);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		group.setLayout(new GridLayout(2, false));
		
		SelectionListener l = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				optionChecked(e);
			}
		};
		
		// Breadth-first search
		fBtnBreadth = new Button(group, SWT.RADIO);
		fBtnBreadth.setText(Messages.TreeViewerSearchDialog_BreadthFirst);
		fBtnBreadth.setSelection(!fDepthFirst);
		fBtnBreadth.addSelectionListener(l);
		fBtnBreadth.setLayoutData(new GridData());
		
		// Depth-first search
		fBtnDepth = new Button(group, SWT.RADIO);
		fBtnDepth.setText(Messages.TreeViewerSearchDialog_DepthFirst);
		fBtnDepth.setSelection(fDepthFirst);
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
		fBtnBackward.setVisible(fDepthFirst);
		
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
			fCaseSensitive = fBtnCase.getSelection();
		}
		else if (src == fBtnWrap) {
			fWrap = fBtnWrap.getSelection();
		}
		else if (src == fBtnMatch) {
			fMatch = fBtnMatch.getSelection();
		}
		else if (src == fBtnBackward) {
			clearJob();
			setStartPath(fLastResult);
			if(fDepthFirst) {
				((DepthFirstSearcher) fSearcher).setForeward(!fBtnBackward.getSelection());
			}
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
			clearJob();
			fDepthFirst = fBtnDepth.getSelection();
			fBtnBackward.setVisible(fDepthFirst);
			fSearcher = fDepthFirst ? new DepthFirstSearcher(fViewer, fMatcher) : new BreadthFirstSearcher(fViewer, fMatcher);
			setStartPath(fStartPath);
			if (fDepthFirst) {
				((DepthFirstSearcher) fSearcher).setForeward(!fBtnBackward.getSelection());
			}
		}
	}

	/**
	 * Clear searching job.
	 */
	void clearJob() {
		if (fSearchJob != null) {
			fSearchJob.cancel();
			fSearchJob = null;
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		clearJob();
		ISelection sel = fViewer.getSelection();
		if (sel == null || sel.isEmpty()) {
			setStartPath(fStartPath);
		}
		else {
			TreeSelection iss = (TreeSelection) sel;
			TreePath[] paths = iss.getPaths();
			if (paths == null || paths.length == 0) {
				setStartPath(fStartPath);
			}
			else {
				setStartPath(paths[0]);
			}
		}
		fLastResult = null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}
}
