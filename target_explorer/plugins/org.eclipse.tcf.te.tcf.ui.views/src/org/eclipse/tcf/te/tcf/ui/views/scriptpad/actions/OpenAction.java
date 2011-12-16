/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.views.scriptpad.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.tcf.te.tcf.ui.views.scriptpad.ScriptPad;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Open script action implementation.
 */
public class OpenAction extends Action implements IViewActionDelegate, IActionDelegate2 {
	// Reference to the action proxy
	/* default */ IAction actionProxy;
	// Parent view part
	/* default */ IViewPart view;

	// Remember the last file path opened
	private String lastFile = null;

	/**
     * Constructor.
     */
    public OpenAction() {
    	super();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
    	this.view = view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init(IAction action) {
    	this.actionProxy = action;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		// Open a file open dialog
		FileDialog dialog = new FileDialog(view.getViewSite().getShell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[] { "*.tcf", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.setFilterPath(lastFile != null ? lastFile : System.getProperty("user.home")); //$NON-NLS-1$
		String file = dialog.open();
		if (file != null && view instanceof ScriptPad) {
			lastFile = file;
			((ScriptPad)view).openFile(file);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	@Override
	public void dispose() {
	}
}
