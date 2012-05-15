/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.interfaces.ImageConsts;
import org.eclipse.tcf.te.ui.nls.Messages;

/**
 * The action to collapse all the expanded branches in the tree control.
 */
public class CollapseAllAction extends Action {
	// The tree control whose expanded branches are to be collapsed.
	private AbstractTreeControl treeControl;

	/**
	 * Create an instance for the specified tree control.
	 * 
	 * @param treeControl The tree control to be collapsed.
	 */
	public CollapseAllAction(AbstractTreeControl treeControl) {
		super(null, AS_PUSH_BUTTON);
		this.treeControl = treeControl;
		this.setToolTipText(Messages.CollapseAllAction_Tooltip);
		ImageDescriptor image = UIPlugin.getImageDescriptor(ImageConsts.VIEWER_COLLAPSE_ALL);
		setImageDescriptor(image);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		TreeViewer viewer = (TreeViewer) treeControl.getViewer();
		viewer.collapseAll();
	}
}
