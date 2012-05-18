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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.internal.DepthTreeSearcher;
import org.eclipse.tcf.te.ui.internal.ISearchCallback;
import org.eclipse.tcf.te.ui.internal.ITreeSearcher;
import org.eclipse.tcf.te.ui.internal.WidthTreeSearcher;
import org.eclipse.tcf.te.ui.nls.Messages;

/**
 * The searching dialog used to get the searching input.
 */
public class TreeViewerSearchDialog extends Dialog implements
		SelectionListener, ISearchMatcher {

	//A new search button's ID.
	private static final int SEARCH_ID = 31;
	//The viewer being searched.
	private TreeViewer fViewer;
	//The progress indicator on the bottom to report searching progress.
	private ProgressIndicator fProgress;
	//The starting path.
	private TreePath fStart;
	//If the starting path has been set.
	private boolean fSet;
	//The input field for searching conditions.
	private Text fSearchField;
	//The text to be searched.
	private String fSearchTarget;

	//The case sensitive check box.
	private Button fBtnCase;
	//The wrap search check box.
	private Button fBtnWrap;
	//The matching rule check box.
	private Button fBtnMatch;
	//The searching orientation check box.
	private Button fBtnBackward;
	//Whether it is case sensitive
	private boolean fCaseSensitive;
	//Whether it is wrap search.
	private boolean fWrap;
	//Whether it is precise matching.
	private boolean fMatch;
	//Whether it is backward searching.
	private boolean fBackward;
	//The last result being searched.
	private TreePath lastResult;
	//The message label using to show "no records found".
	private Label fMessage;
	// The search engine used search the tree
	private ITreeSearcher fSearcher;
	// The search matcher
	private ISearchMatcher matcher;
	// If the search algorithm is depth preferable
	private boolean fDeep;
	
	/**
	 * Create a searching dialog using the specified parent and viewer.
	 * @param parent The parent shell.
	 * @param viewer The execution context viewer.
	 */
	public TreeViewerSearchDialog(TreeViewer viewer) {
		this(viewer, false);
	}
	
	/**
	 * Create a searching dialog using the specified parent and viewer.
	 * @param parent The parent shell.
	 * @param viewer The execution context viewer.
	 */
	public TreeViewerSearchDialog(TreeViewer viewer, boolean deep) {
		this(viewer, deep, null);
	}
	
	/**
	 * Create a searching dialog using the specified parent and viewer.
	 * @param parent The parent shell.
	 * @param viewer The execution context viewer.
	 */
	protected TreeViewerSearchDialog(TreeViewer viewer, boolean deep, ISearchMatcher matcher) {
		super(viewer.getTree().getShell());
		fDeep = deep;
		fViewer = viewer;
		fViewer.getTree().addSelectionListener(this);
		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);
		fSearcher = deep ? new DepthTreeSearcher(fViewer) : new WidthTreeSearcher(fViewer);
		if (matcher == null) this.matcher = this;
		else this.matcher = matcher;
	}

	public void setStartPath(TreePath path) {
		fStart = path;
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
		fProgress.done();
		fProgress.setVisible(false);
		fSearcher.endSearch();
		super.cancelPressed();
	}

	private void searchNext() {
		if (!fSet) {
			fSearcher.startSearch(fStart);
			lastResult = null;
			fSet = true;
		}
		fSearchTarget = fSearchField.getText().trim();
		getButton(SEARCH_ID).setEnabled(false);
		fSearcher.searchNext(!fBackward, matcher, new ISearchCallback() {
			@Override
            public void callback(IStatus status, TreePath path) {
				safeSearchDone(status, path);
			}
		});
		fProgress.setVisible(true);
		fProgress.beginAnimatedTask();
	}

	/*
	 * (non-Javadoc)
	 * @see com.windriver.scopetools.common.ui.phoenix.interfaces.ISearchMatcher#match(com.windriver.scopetools.common.core.phoenix.interfaces.IExecutionContext)
	 */
	@Override
    public boolean match(Object context) {
		if (context == null)
			return false;
		String text = getTreeColumnText(context);
		if (text == null)
			return false;
		String target = fSearchTarget;
		if (!fCaseSensitive) {
			text = text.toLowerCase();
			target = fSearchTarget.toLowerCase();
		}
		if (fMatch)
			return text.equals(target);
		return text.indexOf(target) != -1;
	}

	String getTreeColumnText(final Object context) {
		if (fViewer.getTree().getDisplay().getThread() == Thread
				.currentThread()) {
			ITableLabelProvider labelProvider = (ITableLabelProvider) fViewer
					.getLabelProvider();
			return labelProvider != null ? labelProvider.getColumnText(context,
					0) : context.toString();
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
		if (status.isOK()) {
			if (path == null) {
				if (fWrap) {
					if (lastResult == null) {
						getButton(SEARCH_ID).setEnabled(true);
						fMessage.setText(Messages.TreeViewerSearchDialog_NoSuchNode);
					} else {
						fSearcher.endSearch();
						fSearcher.startSearch(null);
						lastResult = null;
						getButton(SEARCH_ID).setEnabled(true);
						buttonPressed(SEARCH_ID);
						return;
					}
				} else {
					getButton(SEARCH_ID).setEnabled(true);
					fMessage.setText(Messages.TreeViewerSearchDialog_NoSuchNode1);
				}
			} else {
				fMessage.setText(""); //$NON-NLS-1$
				lastResult = path;
				fViewer.expandToLevel(path, 0);
				fViewer.setSelection(new StructuredSelection(
						new Object[] { path }), true);
				getButton(SEARCH_ID).setEnabled(true);
				getButton(SEARCH_ID).setFocus();
			}
		} else {
			if (getButton(SEARCH_ID) != null)
				getButton(SEARCH_ID).setEnabled(true);
		}
		if (!fProgress.isDisposed()) {
			fProgress.done();
			fProgress.setVisible(false);
		}
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
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns++;
		fMessage = new Label(parent, SWT.NONE);
		fMessage.setText(""); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.widthHint = 50;
		fMessage.setLayoutData(data);
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
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout glayout = new GridLayout(2, false);
		glayout.marginHeight = 10;
		glayout.marginWidth = 10;
		glayout.verticalSpacing = 10;
		glayout.horizontalSpacing = 10;
		composite.setLayout(glayout);
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.TreeViewerSearchDialog_LblCancelText);
		fSearchField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		fSearchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		fSearchField.addKeyListener(new KeyListener() {
			@Override
            public void keyPressed(KeyEvent e) {
				updateButton();
			}

			@Override
            public void keyReleased(KeyEvent e) {
			}
		});
		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setText(Messages.TreeViewerSearchDialog_GrpOptionsText);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		group.setLayout(new GridLayout(2, false));
		OptionsListener l = new OptionsListener();
		fBtnCase = new Button(group, SWT.CHECK);
		fBtnCase.setText(Messages.TreeViewerSearchDialog_BtnCaseText);
		fBtnCase.addSelectionListener(l);
		fBtnMatch = new Button(group, SWT.CHECK);
		fBtnMatch.setText(Messages.TreeViewerSearchDialog_BtnPreciseText);
		fBtnMatch.addSelectionListener(l);
		fBtnWrap = new Button(group, SWT.CHECK);
		fBtnWrap.setText(Messages.TreeViewerSearchDialog_BtnWrapText);
		fBtnWrap.addSelectionListener(l);
		if (fDeep) {
			fBtnBackward = new Button(group, SWT.CHECK);
			fBtnBackward.setText(Messages.TreeViewerSearchDialog_BtnBackText);
			fBtnBackward.addSelectionListener(l);
		}
		fProgress = new ProgressIndicator(composite);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		data.heightHint = 15;
		fProgress.setLayoutData(data);
		fProgress.setVisible(false);
		return composite;
	}

	void optionChecked(SelectionEvent e) {
		Object src = e.getSource();
		if (src == fBtnCase) {
			fCaseSensitive = fBtnCase.getSelection();
		} else if (src == fBtnWrap) {
			fWrap = fBtnWrap.getSelection();
		} else if (src == fBtnMatch) {
			fMatch = fBtnMatch.getSelection();
		} else if (src == fBtnBackward) {
			fSearcher.endSearch();
			fSearcher.startSearch(lastResult);
			fBackward = fBtnBackward.getSelection();
		}
	}

	class OptionsListener implements SelectionListener {

		@Override
        public void widgetSelected(SelectionEvent e) {
			optionChecked(e);
		}

		@Override
        public void widgetDefaultSelected(SelectionEvent e) {
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
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
    public void widgetSelected(SelectionEvent e) {
		ISelection sel = fViewer.getSelection();
		fSearcher.endSearch();
		if (sel == null || sel.isEmpty()) {
			fStart = null;
		} else {
			TreeSelection iss = (TreeSelection) sel;
			TreePath[] paths = iss.getPaths();
			if (paths == null || paths.length == 0) {
				fStart = null;
			} else {
				fStart = paths[0];
			}
		}
		fSet = false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
    public void widgetDefaultSelected(SelectionEvent e) {
	}
}
