/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River) - [345384] Provide property pages for remote file system nodes
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.controls;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.tcf.filesystem.interfaces.IUIConstants;
import org.eclipse.tcf.te.tcf.filesystem.internal.dnd.FSDragSourceListener;
import org.eclipse.tcf.te.tcf.filesystem.internal.dnd.FSDropTargetListener;
import org.eclipse.tcf.te.ui.trees.AbstractTreeControl;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.ICommonActionConstants;


/**
 * File system browser control.
 */
public class FSTreeControl extends AbstractTreeControl {

	/**
	 * Constructor.
	 */
	public FSTreeControl() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param parentPart The parent workbench part this control is embedded in or <code>null</code>.
	 */
	public FSTreeControl(IWorkbenchPart parentPart) {
		super(parentPart);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#doCreateTreeViewer(org.eclipse.swt.widgets.Composite)
	 */
	@Override
    protected TreeViewer doCreateTreeViewer(Composite parent) {
		Assert.isNotNull(parent);
		// Override the parent method to create a multiple-selection tree.
		return new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#configureTreeViewer(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		super.configureTreeViewer(viewer);

		//Add DnD support.
	    int operations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
		Transfer[] transferTypes = {LocalSelectionTransfer.getTransfer()};
		viewer.addDragSupport(operations, transferTypes, new FSDragSourceListener(viewer));
		viewer.addDropSupport(operations, transferTypes, new FSDropTargetListener(viewer));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#doCreateTreeViewerContentProvider(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected ITreeContentProvider doCreateTreeViewerContentProvider(TreeViewer viewer) {
		return new FSTreeContentProvider(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#getAutoExpandLevel()
	 */
	@Override
	protected int getAutoExpandLevel() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.trees.AbstractTreeControl#getContextMenuId()
	 */
	@Override
	protected String getContextMenuId() {
		return IUIConstants.ID_TREE_VIEWER_FS_CONTEXT_MENU;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#doCreateTreeViewerSelectionChangedListener(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
    protected ISelectionChangedListener doCreateTreeViewerSelectionChangedListener(TreeViewer viewer) {
		return  null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getDoubleClickCommandId()
	 */
	@Override
    protected String getDoubleClickCommandId() {
	    return ICommonActionConstants.OPEN;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getHelpId()
	 */
	@Override
    protected String getHelpId() {
		return IUIConstants.ID_TREE_VIEWER_FS_HELP;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.AbstractTreeControl#getViewerId()
	 */
	@Override
    protected String getViewerId() {
	    return IUIConstants.ID_TREE_VIEWER_FS;
    }
}
