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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.internal.AbstractSearcher;
import org.eclipse.tcf.te.ui.internal.DepthTreeSearcher;
import org.eclipse.tcf.te.ui.internal.WidthTreeSearcher;
import org.eclipse.tcf.te.ui.jface.dialogs.CustomTitleAreaDialog;
import org.eclipse.tcf.te.ui.nls.Messages;

/**
 * The searching dialog used to get the searching input.
 */
public class TreeViewerSearchDialog extends CustomTitleAreaDialog implements SelectionListener, ISearchMatcher {

	// A new search button's ID.
	private static final int SEARCH_ID = 31;
	// The viewer being searched.
	private TreeViewer fViewer;
	// The input field for searching conditions.
	private Text fSearchField;
	// The text to be searched.
	private String fSearchTarget;

	private Button fBtnDeep;
	// The case sensitive check box.
	private Button fBtnWidth;
	private Button fBtnCase;
	// The wrap search check box.
	private Button fBtnWrap;
	// The matching rule check box.
	private Button fBtnMatch;
	// The searching orientation check box.
	private Button fBtnBackward;
	ProgressMonitorPart fPmPart;
	// Whether it is case sensitive
	private boolean fCaseSensitive;
	// Whether it is wrap search.
	private boolean fWrap;
	// Whether it is precise matching.
	private boolean fMatch;
	// The last result being searched.
	private TreePath fLastResult;
	// The search engine used search the tree
	AbstractSearcher fSearcher;
	// If the search algorithm is depth preferable
	private boolean fDeep;
	// The searching job.
	protected Job fSearchJob;
	private ISearchMatcher fMatcher;
	TreePath fStartPath;

	/**
	 * Create a searching dialog using the specified parent and viewer.
	 * 
	 * @param parent The parent shell.
	 * @param viewer The execution context viewer.
	 */
	public TreeViewerSearchDialog(TreeViewer viewer) {
		this(viewer, false);
	}

	/**
	 * ow Create a searching dialog using the specified parent and viewer.
	 * 
	 * @param parent The parent shell.
	 * @param viewer The execution context viewer.
	 */
	public TreeViewerSearchDialog(TreeViewer viewer, boolean deep) {
		this(viewer, deep, null);
	}

	/**
	 * Create a searching dialog using the specified parent and viewer.
	 * 
	 * @param parent The parent shell.
	 * @param viewer The execution context viewer.
	 */
	protected TreeViewerSearchDialog(TreeViewer viewer, boolean deep, ISearchMatcher matcher) {
		super(viewer.getTree().getShell());
		fDeep = deep;
		fViewer = viewer;
		fViewer.getTree().addSelectionListener(this);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);
		if (matcher == null) matcher = this;
		fMatcher = matcher;
		fSearcher = fDeep ? new DepthTreeSearcher(fViewer, fMatcher) : new WidthTreeSearcher(fViewer, fMatcher);
		this.setTitle(Messages.TreeViewerSearchDialog_DialogTitleMessage);
	}

	public void setStartPath(TreePath path) {
		fStartPath = path;
		fSearcher.setStartPath(path);
		if(fSearcher != null) {
			String text = fSearcher.getElementText(path.getLastSegment());
			this.setDefaultMessage(NLS.bind(Messages.TreeViewerSearchDialog_DialogPromptMessage, text), NONE);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#close()
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
			searchNext();
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

	private void searchNext() {
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
			public void done(IJobChangeEvent event) {
				safeSearchDone(event.getResult(), result[0]);
			}
		});
		fSearchJob.schedule();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.windriver.scopetools.common.ui.phoenix.interfaces.ISearchMatcher#match(com.windriver.
	 * scopetools.common.core.phoenix.interfaces.IExecutionContext)
	 */
	@Override
	public boolean match(Object context) {
		if (context == null) return false;
		String text = getTreeColumnText(context);
		if (text == null) return false;
		String target = fSearchTarget;
		if (!fCaseSensitive) {
			text = text.toLowerCase();
			target = fSearchTarget.toLowerCase();
		}
		if (fMatch) return text.equals(target);
		return text.indexOf(target) != -1;
	}

	String getTreeColumnText(final Object context) {
		if (fViewer.getTree().getDisplay().getThread() == Thread.currentThread()) {
			ITableLabelProvider labelProvider = (ITableLabelProvider) fViewer.getLabelProvider();
			return labelProvider != null ? labelProvider.getColumnText(context, 0) : context
			                .toString();
		}
		final String[] result = new String[1];
		fViewer.getTree().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				result[0] = getTreeColumnText(context);
			}
		});
		return result[0];
	}

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
					setErrorMessage(Messages.TreeViewerSearchDialog_NoSuchNode1);
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

	void safeSearchDone(final IStatus status, final TreePath path) {
		fViewer.getTree().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				searchDone(status, path);
			}
		});
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
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, SEARCH_ID, Messages.TreeViewerSearchDialog_BtnSearchText, true);
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.TreeViewerSearchDialog_BtnCancelText, false);
		getButton(SEARCH_ID).setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// set margins of dialog and apply dialog font
		Composite container = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(container, SWT.NONE);
		GridLayout glayout = new GridLayout(2, false);
		glayout.marginHeight = 10;
		glayout.marginWidth = 10;
		glayout.verticalSpacing = 10;
		glayout.horizontalSpacing = 10;
		composite.setLayout(glayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.TreeViewerSearchDialog_LblCancelText);
		fSearchField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		fSearchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fSearchField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateButton();
				setStartPath(fStartPath);
			}
		});
		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
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
		
		fBtnWidth = new Button(group, SWT.RADIO);
		fBtnWidth.setText(Messages.TreeViewerSearchDialog_BreadthFirst);
		fBtnWidth.setSelection(true);
		fBtnWidth.addSelectionListener(l);
		fBtnWidth.setLayoutData(new GridData());
		
		fBtnDeep = new Button(group, SWT.RADIO);
		fBtnDeep.setText(Messages.TreeViewerSearchDialog_DepthFirst);
		fBtnDeep.addSelectionListener(l);
		fBtnDeep.setLayoutData(new GridData());
		
		group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.TreeViewerSearchDialog_GrpOptionsText);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		group.setLayout(new GridLayout(4, false));

		fBtnCase = new Button(group, SWT.CHECK);
		fBtnCase.setText(Messages.TreeViewerSearchDialog_BtnCaseText);
		fBtnCase.addSelectionListener(l);
		fBtnMatch = new Button(group, SWT.CHECK);
		fBtnMatch.setText(Messages.TreeViewerSearchDialog_BtnPreciseText);
		fBtnMatch.addSelectionListener(l);
		fBtnWrap = new Button(group, SWT.CHECK);
		fBtnWrap.setText(Messages.TreeViewerSearchDialog_BtnWrapText);
		fBtnWrap.addSelectionListener(l);
		fBtnBackward = new Button(group, SWT.CHECK);
		fBtnBackward.setText(Messages.TreeViewerSearchDialog_BtnBackText);
		fBtnBackward.addSelectionListener(l);
		fBtnBackward.setVisible(fDeep);
		fPmPart = new ProgressMonitorPart(composite, null, true);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		fPmPart.setLayoutData(data);
		fPmPart.setVisible(false);
		return container;
	}

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
			endSearch();
			setStartPath(fLastResult);
			if(fDeep) {
				((DepthTreeSearcher) fSearcher).setForeward(!fBtnBackward.getSelection());
			}
		}
		else if (src == fBtnDeep || src == fBtnWidth) {
			if (src == fBtnDeep) {
				fBtnDeep.setSelection(true);
				fBtnWidth.setSelection(false);
			}
			else if (src == fBtnWidth) {
				fBtnWidth.setSelection(true);
				fBtnDeep.setSelection(false);
			}
			endSearch();
			fDeep = fBtnDeep.getSelection();
			fBtnBackward.setVisible(fDeep);
			fSearcher = fDeep ? new DepthTreeSearcher(fViewer, fMatcher) : new WidthTreeSearcher(fViewer, fMatcher);
			setStartPath(fStartPath);
			if (fDeep) {
				((DepthTreeSearcher) fSearcher).setForeward(!fBtnBackward.getSelection());
			}
		}
	}

	void endSearch() {
		if (fSearchJob != null) {
			fSearchJob.cancel();
			fSearchJob = null;
		}
	}

	void updateButton() {
		boolean valid = isInputValid();
		getButton(SEARCH_ID).setEnabled(valid);
	}

	private boolean isInputValid() {
		String txt = fSearchField.getText();
		return txt != null && txt.trim().length() > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent
	 * )
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		endSearch();
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
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.
	 * SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}
}
