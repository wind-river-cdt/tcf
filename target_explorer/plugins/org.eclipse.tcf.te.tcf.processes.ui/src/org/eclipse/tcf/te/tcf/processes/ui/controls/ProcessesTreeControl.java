/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.controls;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.ui.interfaces.IUIConstants;
import org.eclipse.tcf.te.ui.trees.AbstractTreeControl;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Processes tree control.
 */
public class ProcessesTreeControl extends AbstractTreeControl {

	/**
	 * Constructor.
	 */
	public ProcessesTreeControl() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param parentPart The parent workbench part this control is embedded in or <code>null</code>.
	 */
	public ProcessesTreeControl(IWorkbenchPart parentPart) {
		super(parentPart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#doCreateTreeViewerContentProvider(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ITreeContentProvider doCreateTreeViewerContentProvider(TreeViewer viewer) {
		return new ProcessesTreeContentProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#doCreateTreeViewerSelectionChangedListener(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ISelectionChangedListener doCreateTreeViewerSelectionChangedListener(TreeViewer viewer) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getAutoExpandLevel()
	 */
	@Override
	protected int getAutoExpandLevel() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getContextMenuId()
	 */
	@Override
	protected String getContextMenuId() {
		return IUIConstants.ID_CONTROL_MENUS_BASE + ".menu.processes"; //$NON-NLS-1$;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getHelpId()
	 */
	@Override
    protected String getHelpId() {
	    return getViewerId() + ".help"; //$NON-NLS-1$
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getViewerId()
	 */
	@Override
    protected String getViewerId() {
		return IUIConstants.ID_CONTROL_MENUS_BASE + ".viewer.processes"; //$NON-NLS-1$;
    }
}
