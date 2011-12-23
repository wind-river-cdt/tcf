/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.controls;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tcf.te.tcf.filesystem.internal.events.INodeStateListener;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The node state listener that listens to the state change event of the file system tree
 * and update the viewer.
 */
public class FSTreeNodeStateListener implements INodeStateListener {
	// The viewer to display the file system tree.
	private TreeViewer viewer;
	
	/**
	 * Create an file system tree node listener.
	 * 
	 * @param viewer The tree viewer to display the file system.
	 */
	public FSTreeNodeStateListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.events.INodeStateListener#stateChanged(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
	public void stateChanged(final FSTreeNode node) {
		// Make sure that this node is inside of this viewer.
		Tree tree = viewer.getTree();
		if (!tree.isDisposed()) {
			Display display = tree.getDisplay();
			if (display.getThread() == Thread.currentThread()) {
				if (node != null && !node.isSystemRoot()) {
					viewer.refresh(node);
				}
				else {
					// Refresh the whole tree.
					viewer.refresh();
				}
			}
			else {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						stateChanged(node);
					}
				});
			}
		}
	}
}
